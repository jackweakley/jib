/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.builder.steps;

import com.google.cloud.tools.jib.api.Credential;
import com.google.cloud.tools.jib.blob.BlobDescriptor;
import com.google.cloud.tools.jib.builder.ProgressEventDispatcher;
import com.google.cloud.tools.jib.builder.steps.PullBaseImageStep.ImageAndAuthorization;
import com.google.cloud.tools.jib.configuration.BuildConfiguration;
import com.google.cloud.tools.jib.docker.DockerClient;
import com.google.cloud.tools.jib.global.JibSystemProperties;
import com.google.cloud.tools.jib.http.Authorization;
import com.google.cloud.tools.jib.image.Image;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Runs steps for building an image.
 *
 * <p>Use by first calling {@link #begin} and then calling the individual step running methods. Note
 * that order matters, so make sure that steps are run before other steps that depend on them. Wait
 * on the last step by calling the respective {@code wait...} methods.
 */
public class StepsRunner {

  /** Holds the individual step results. */
  private static class StepResults {

    private static <E> Future<E> failedFuture() {
      return Futures.immediateFailedFuture(
          new IllegalStateException("invalid usage; required step not configured"));
    }

    private Future<ImageAndAuthorization> baseImageAndAuth = failedFuture();
    private Future<List<Future<CachedLayerAndName>>> baseImageLayers = failedFuture();
    private List<Future<CachedLayerAndName>> applicationLayers = new ArrayList<>();
    private Future<Image> builtImage = failedFuture();
    private Future<Credential> targetRegistryCredentials = failedFuture();
    private Future<Authorization> pushAuthorization = failedFuture();
    private Future<List<Future<BlobDescriptor>>> baseImageLayerPushResults = failedFuture();
    private Future<List<Future<BlobDescriptor>>> applicationLayerPushResults = failedFuture();
    private Future<BlobDescriptor> containerConfigurationPushResult = failedFuture();
    private Future<BuildResult> buildResult = failedFuture();
  }

  /**
   * Starts building the steps to run.
   *
   * @param buildConfiguration the {@link BuildConfiguration}
   * @return a new {@link StepsRunner}
   */
  public static StepsRunner begin(BuildConfiguration buildConfiguration) {
    ExecutorService executorService =
        JibSystemProperties.isSerializedExecutionEnabled()
            ? MoreExecutors.newDirectExecutorService()
            : buildConfiguration.getExecutorService();

    return new StepsRunner(MoreExecutors.listeningDecorator(executorService), buildConfiguration);
  }

  private final StepResults results = new StepResults();

  // TODO: use plain ExecutorService; requires refactoring PushImageStep.
  private final ListeningExecutorService executorService;
  private final BuildConfiguration buildConfiguration;

  private final List<Runnable> stepsToRun = new ArrayList<>();

  @Nullable private String rootProgressDescription;
  private Supplier<ProgressEventDispatcher.Factory> childProgressDispatcherFactorySupplier =
      () -> {
        throw new IllegalStateException("root progress dispatcher uninstantiated");
      };

  private StepsRunner(
      ListeningExecutorService executorService, BuildConfiguration buildConfiguration) {
    this.executorService = executorService;
    this.buildConfiguration = buildConfiguration;
  }

  private void retrieveTargetRegistryCredentials() {
    results.targetRegistryCredentials =
        executorService.submit(
            RetrieveRegistryCredentialsStep.forTargetImage(
                buildConfiguration, childProgressDispatcherFactorySupplier.get()));
  }

  private void authenticatePush() {
    results.pushAuthorization =
        executorService.submit(
            () ->
                new AuthenticatePushStep(
                        buildConfiguration,
                        childProgressDispatcherFactorySupplier.get(),
                        Preconditions.checkNotNull(results.targetRegistryCredentials).get())
                    .call());
  }

  private void pullBaseImage() {
    results.baseImageAndAuth =
        executorService.submit(
            new PullBaseImageStep(
                buildConfiguration, childProgressDispatcherFactorySupplier.get()));
  }

  private void pullAndCacheBaseImageLayers() {
    results.baseImageLayers =
        executorService.submit(
            () ->
                scheduleCallables(
                    PullAndCacheBaseImageLayersStep.makeList(
                        buildConfiguration,
                        childProgressDispatcherFactorySupplier.get(),
                        results.baseImageAndAuth.get())));
  }

  private void pushBaseImageLayers() {
    results.baseImageLayerPushResults =
        executorService.submit(
            () ->
                scheduleCallables(
                    PushLayerStep.makeList(
                        buildConfiguration,
                        childProgressDispatcherFactorySupplier.get(),
                        results.pushAuthorization.get(),
                        results.baseImageLayers.get())));
  }

  private void buildAndCacheApplicationLayers() {
    results.applicationLayers =
        scheduleCallables(
            BuildAndCacheApplicationLayerStep.makeList(
                buildConfiguration, childProgressDispatcherFactorySupplier.get()));
  }

  private void buildImage() {
    results.builtImage =
        executorService.submit(
            () ->
                new BuildImageStep(
                        buildConfiguration,
                        childProgressDispatcherFactorySupplier.get(),
                        Preconditions.checkNotNull(results.baseImageAndAuth).get().getImage(),
                        realizeFutures(results.baseImageLayers.get()),
                        realizeFutures(results.applicationLayers))
                    .call());
  }

  private void pushContainerConfiguration() {
    results.containerConfigurationPushResult =
        executorService.submit(
            () ->
                new PushContainerConfigurationStep(
                        buildConfiguration,
                        childProgressDispatcherFactorySupplier.get(),
                        results.pushAuthorization.get(),
                        results.builtImage.get())
                    .call());
  }

  private void pushApplicationLayers() {
    results.applicationLayerPushResults =
        executorService.submit(
            () ->
                scheduleCallables(
                    PushLayerStep.makeList(
                        buildConfiguration,
                        childProgressDispatcherFactorySupplier.get(),
                        results.pushAuthorization.get(),
                        results.applicationLayers)));
  }

  private void pushImage() {
    results.buildResult =
        executorService.submit(
            () -> {
              realizeFutures(results.baseImageLayerPushResults.get());
              realizeFutures(results.applicationLayerPushResults.get());

              return new PushImageStep(
                      executorService,
                      buildConfiguration,
                      childProgressDispatcherFactorySupplier.get(),
                      results.pushAuthorization.get(),
                      results.containerConfigurationPushResult.get(),
                      results.builtImage.get())
                  .call();
            });
  }

  private void loadDocker(DockerClient dockerClient) {
    results.buildResult =
        executorService.submit(
            () ->
                new LoadDockerStep(
                        buildConfiguration,
                        childProgressDispatcherFactorySupplier.get(),
                        dockerClient,
                        results.builtImage.get())
                    .call());
  }

  private void writeTarFile(Path outputPath) {
    results.buildResult =
        executorService.submit(
            () ->
                new WriteTarFileStep(
                        buildConfiguration,
                        childProgressDispatcherFactorySupplier.get(),
                        outputPath,
                        results.builtImage.get())
                    .call());
  }

  public BuildResult run() throws ExecutionException, InterruptedException {
    Preconditions.checkNotNull(rootProgressDescription);

    try (ProgressEventDispatcher progressEventDispatcher =
        ProgressEventDispatcher.newRoot(
            buildConfiguration.getEventHandlers(), rootProgressDescription, stepsToRun.size())) {
      childProgressDispatcherFactorySupplier = progressEventDispatcher::newChildProducer;
      stepsToRun.forEach(Runnable::run);
      return results.buildResult.get();
    }
  }

  private static <E> List<E> realizeFutures(List<Future<E>> futures)
      throws InterruptedException, ExecutionException {
    List<E> values = new ArrayList<>();
    for (Future<E> future : futures) {
      values.add(future.get());
    }
    return values;
  }

  private <E> List<Future<E>> scheduleCallables(ImmutableList<? extends Callable<E>> callables) {
    return callables.stream().map(executorService::submit).collect(Collectors.toList());
  }

  public StepsRunner pullBaseImageStep() {
    stepsToRun.add(this::pullBaseImage);
    return this;
  }

  public StepsRunner pullAndCacheBaseImageLayersStep() {
    stepsToRun.add(this::pullAndCacheBaseImageLayers);
    return this;
  }

  public StepsRunner buildAndCacheApplicationLayersStep() {
    stepsToRun.add(this::buildAndCacheApplicationLayers);
    return this;
  }

  public StepsRunner buildImageStep() {
    stepsToRun.add(this::buildImage);
    return this;
  }

  public StepsRunner loadDockerStep(DockerClient dockerClient) {
    rootProgressDescription = "building image to Docker daemon";
    stepsToRun.add(() -> loadDocker(dockerClient));
    return this;
  }

  public StepsRunner writeTarFileStep(Path outputPath) {
    rootProgressDescription = "building image to tar file";
    stepsToRun.add(() -> writeTarFile(outputPath));
    return this;
  }

  public StepsRunner retrieveTargetRegistryCredentialsStep() {
    stepsToRun.add(this::retrieveTargetRegistryCredentials);
    return this;
  }

  public StepsRunner authenticatePushStep() {
    stepsToRun.add(this::authenticatePush);
    return this;
  }

  public StepsRunner pushBaseImageLayersStep() {
    stepsToRun.add(this::pushBaseImageLayers);
    return this;
  }

  public StepsRunner pushApplicationLayersStep() {
    stepsToRun.add(this::pushApplicationLayers);
    return this;
  }

  public StepsRunner pushContainerConfigurationStep() {
    stepsToRun.add(this::pushContainerConfiguration);
    return this;
  }

  public StepsRunner pushImageStep() {
    rootProgressDescription = "building image to registry";
    stepsToRun.add(this::pushImage);
    return this;
  }
}
