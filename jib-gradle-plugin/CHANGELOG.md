# Change Log
All notable changes to this project will be documented in this file.

## [unreleased]

### Added

### Changed

- Local base image layers are now processed in parallel, speeding up builds using large local base images. ([#1913](https://github.com/GoogleContainerTools/jib/issues/1913))

### Fixed

- Fixed temporary directory cleanup during builds using local base images. ([#2016](https://github.com/GoogleContainerTools/jib/issues/2016))

## 1.6.1

### Fixed

- Fixed an issue with using custom base images in Java 12+ projects. ([#1995](https://github.com/GoogleContainerTools/jib/issues/1995))

## 1.6.0

### Added

- Support for local base images by prefixing `jib.from.image` with `docker://` to build from a docker daemon image, or `tar://` to build from a tarball image. ([#1468](https://github.com/GoogleContainerTools/jib/issues/1468), [#1905](https://github.com/GoogleContainerTools/jib/issues/1905))

### Changed

- To disable parallel execution, the property `jib.serialize` should be used instead of `jibSerialize`. ([#1968](https://github.com/GoogleContainerTools/jib/issues/1968))
- For retrieving credentials from Docker config (`~/.docker/config.json`), `credHelpers` now takes precedence over `credsStore`, followed by `auths`. ([#1958](https://github.com/GoogleContainerTools/jib/pull/1958))
- The legacy `credsStore` no longer requires defining empty registry entries in `auths` to be used. This now means that if `credsStore` is defined, `auths` will be completely ignored. ([#1958](https://github.com/GoogleContainerTools/jib/pull/1958))
- `jib.dockerClient` is now configurable on all tasks, not just `jibDockerBuild`. ([#1932](https://github.com/GoogleContainerTools/jib/issues/1932))
- `jibDockerBuild.dockerClient` is deprecated in favor of `jib.dockerClient`.

### Fixed

- Fixed the regression of slow network operations introduced at 1.5.0. ([#1980](https://github.com/GoogleContainerTools/jib/pull/1980))
- Fixed an issue where connection timeout sometimes fell back to attempting plain HTTP (non-HTTPS) requests when `allowInsecureRegistries` is set. ([#1949](https://github.com/GoogleContainerTools/jib/pull/1949))

## 1.5.1

### Fixed

- Fixed an issue interacting with certain registries due to changes to URL handling in the underlying Apache HttpClient library. ([#1924](https://github.com/GoogleContainerTools/jib/issues/1924))

## 1.5.0

### Added

- Can now set timestamps (last modified time) of the files in the built image with `jib.container.filesModificationTime`. The value should either be `EPOCH_PLUS_SECOND` to set the timestamps to Epoch + 1 second (default behavior), or an ISO 8601 date time parsable with [`DateTimeFormatter.ISO_DATE_TIME`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html) such as `2019-07-15T10:15:30+09:00` or `2011-12-03T22:42:05Z`. ([#1818](https://github.com/GoogleContainerTools/jib/pull/1818))
- Can now set container creation timestamp with `jib.container.creationTime`. The value should be `EPOCH`, `USE_CURRENT_TIMESTAMP`, or an ISO 8601 date time. ([#1609](https://github.com/GoogleContainerTools/jib/issues/1609))
- For Google Container Registry (gcr.io), Jib now tries [Google Application Default Credentials](https://developers.google.com/identity/protocols/application-default-credentials) (ADC) last when no credentials can be retrieved. ADC are available on many Google Cloud Platform (GCP) environments (such as Google Cloud Build, Google Compute Engine, Google Kubernetes Engine, and Google App Engine). Application Default Credentials can also be configured with `gcloud auth application-default login` locally or through the `GOOGLE_APPLICATION_CREDENTIALS` environment variable. ([#1902](https://github.com/GoogleContainerTools/jib/pull/1902))

### Changed

- When building to a registry, Jib now skips downloading and caching base image layers that already exist in the target registry. This feature will be particularly useful in CI/CD environments. However, if you want to force caching base image layers locally, set the system property `-Djib.alwaysCacheBaseImage=true`. ([#1840](https://github.com/GoogleContainerTools/jib/pull/1840))
- `jib.container.useCurrentTimestamp` has been deprecated in favor of `jib.container.creationTime` with `USE_CURRENT_TIMESTAMP`. ([#1609](https://github.com/GoogleContainerTools/jib/issues/1609))

## 1.4.0

### Added

- Can now containerize a JAR artifact instead of putting individual `.class` and resource files with `jib.containerizingMode = 'packaged'`. ([#1760](https://github.com/GoogleContainerTools/jib/pull/1760/files))
- Now automatically supports WAR created by the Spring Boot Gradle Plugin via the `bootWar` task. ([#1786](https://github.com/GoogleContainerTools/jib/issues/1786))
- Can now use `jib.from.image = 'scratch'` to use the scratch (empty) base image for builds. ([#1794](https://github.com/GoogleContainerTools/jib/pull/1794/files))

### Changed

- Dependencies are now split into three layers: dependencies, snapshots dependencies, project dependencies. ([#1724](https://github.com/GoogleContainerTools/jib/pull/1724))

### Fixed

- Re-enabled cross-repository blob mounts. ([#1793](https://github.com/GoogleContainerTools/jib/pull/1793))
- Manifest lists referenced directly by sha256 are automatically parsed and the first `linux/amd64` manifest is used. ([#1811](https://github.com/GoogleContainerTools/jib/issues/1811))

## 1.3.0

### Changed

- Docker credentials (`~/.docker/config.json`) are now given priority over registry-based inferred credential helpers. ([#1704](https://github.com/GoogleContainerTools/jib/pulls/1704))

### Fixed

- Fixed an issue with `jibBuildTar` where `UP-TO-DATE` checks were incorrect. ([#1757](https://github.com/GoogleContainerTools/jib/issues/1757))

## 1.2.0

### Added

- Container configurations in the base image are now propagated when registry uses the old V2 image manifest, schema version 1 (such as Quay). ([#1641](https://github.com/GoogleContainerTools/jib/issues/1641))
- Can now prepend paths in the container to the computed classpath with `jib.container.extraClasspath`. ([#1642](https://github.com/GoogleContainerTools/jib/pull/1642))
- Can now build in offline mode using `--offline`. ([#718](https://github.com/GoogleContainerTools/jib/issues/718))
- Now supports multiple extra directories with `jib.extraDirectories.{paths|.permissions}`. ([#1020](https://github.com/GoogleContainerTools/jib/issues/1020))

### Changed

- `jib.extraDirectory({.path|.permissions})` are deprecated in favor of the new `jib.extraDirectories.{paths|.permissions}` configurations. ([#1671](https://github.com/GoogleContainerTools/jib/pull/1671))

### Fixed

- Labels in the base image are now propagated. ([#1643](https://github.com/GoogleContainerTools/jib/issues/1643))
- Fixed an issue with using OCI base images. ([#1683](https://github.com/GoogleContainerTools/jib/issues/1683))

## 1.1.2

### Fixed

- Fixed an issue where automatically generated parent directories in a layer did not get their timestamp configured correctly to epoch + 1s. ([#1648](https://github.com/GoogleContainerTools/jib/issues/1648))

## 1.1.1

### Fixed

- Fixed an issue where the plugin creates wrong images by adding base image layers in reverse order when registry uses the old V2 image manifest, schema version 1 (such as Quay). ([#1627](https://github.com/GoogleContainerTools/jib/issues/1627))

## 1.1.0

### Changed

- `os` and `architecture` are taken from base image. ([#1564](https://github.com/GoogleContainerTools/jib/pull/1564))

### Fixed

- Fixed an issue where pushing to Docker Hub fails when the host part of an image reference is `docker.io`. ([#1549](https://github.com/GoogleContainerTools/jib/issues/1549))

## 1.0.2

### Added

- Java 9+ WAR projects are now supported and run on the distroless Jetty Java 11 image (https://github.com/GoogleContainerTools/distroless) by default. Java 8 projects remain on the distroless Jetty Java 8 image. ([#1510](https://github.com/GoogleContainerTools/jib/issues/1510))
- Now supports authentication against Azure Container Registry using `docker-credential-acr-*` credential helpers. ([#1490](https://github.com/GoogleContainerTools/jib/issues/1490))

### Fixed

- Fixed an issue where setting `allowInsecureRegistries` may fail to try HTTP. ([#1517](https://github.com/GoogleContainerTools/jib/issues/1517))
- Crash on talking to servers that do not set the `Content-Length` HTTP header or send an incorrect value. ([#1512](https://github.com/GoogleContainerTools/jib/issues/1512))

## 1.0.1

### Added

- Java 9+ projects are now supported and run on the distroless Java 11 image (https://github.com/GoogleContainerTools/distroless) by default. Java 8 projects remain on the distroless Java 8 image. ([#1279](https://github.com/GoogleContainerTools/jib/issues/1279))

### Fixed

- Failure to infer main class when main method is defined using varargs (i.e. `public static void main(String... args)`). ([#1456](https://github.com/GoogleContainerTools/jib/issues/1456))

## 1.0.0

### Changed

- Shortened progress bar display - make sure console window is at least 50 characters wide or progress bar display can be messy. ([#1361](https://github.com/GoogleContainerTools/jib/issues/1361))

## 1.0.0-rc2

### Added

- Setting proxy credentials (via system properties `http(s).proxyUser` and `http(s).proxyPassword`) is now supported.

### Changed

- Java 9+ projects using the default distroless Java 8 base image will now fail to build. ([#1143](https://github.com/GoogleContainerTools/jib/issues/1143))

## 1.0.0-rc1

### Added

- `jib.baseImageCache` and `jib.applicationCache` system properties for setting cache directories. ([#1238](https://github.com/GoogleContainerTools/jib/issues/1238))
- Build progress shown via a progress bar - set `-Djib.console=plain` to show progress as log messages. ([#1297](https://github.com/GoogleContainerTools/jib/issues/1297))

### Changed

- Removed `jib.useOnlyProjectCache` parameter in favor of the `jib.useOnlyProjectCache` system property. ([#1308](https://github.com/GoogleContainerTools/jib/issues/1308))

### Fixed

- Builds failing due to dependency JARs with the same name. ([#810](https://github.com/GoogleContainerTools/jib/issues/810))

## 0.10.1

### Added

- Image ID is now written to `build/jib-image.id`. ([#1204](https://github.com/GoogleContainerTools/jib/issues/1204))
- `jib.container.entrypoint = 'INHERIT'` allows inheriting `ENTRYPOINT` and `CMD` from the base image. While inheriting `ENTRYPOINT`, you can also override `CMD` using `jib.container.args`.
- `container.workingDirectory` configuration parameter to set the working directory. ([#1225](https://github.com/GoogleContainerTools/jib/issues/1225))
- Adds support for configuring volumes. ([#1121](https://github.com/GoogleContainerTools/jib/issues/1121))
- Exposed ports are now propagated from the base image. ([#595](https://github.com/GoogleContainerTools/jib/issues/595))
- Docker health check is now propagated from the base image. ([#595](https://github.com/GoogleContainerTools/jib/issues/595))

### Changed

- Removed `jibExportDockerContext` task. ([#1219](https://github.com/GoogleContainerTools/jib/issues/1219))

### Fixed

- NullPointerException thrown with incomplete `auth` configuration. ([#1177](https://github.com/GoogleContainerTools/jib/issues/1177))

## 0.10.0

### Added

- Properties for each configuration parameter, allowing any parameter to be set via commandline. ([#1083](https://github.com/GoogleContainerTools/jib/issues/1083))
- `jib.to.credHelper` and `jib.from.credHelper` can be used to specify a credential helper suffix or a full path to a credential helper executable. ([#925](https://github.com/GoogleContainerTools/jib/issues/925))
- `container.user` configuration parameter to configure the user and group to run the container as. ([#1029](https://github.com/GoogleContainerTools/jib/issues/1029))
- Preliminary support for building images for WAR projects. ([#431](https://github.com/GoogleContainerTools/jib/issues/431))
- `jib.extraDirectory` closure with a `path` and `permissions` field. ([#794](https://github.com/GoogleContainerTools/jib/issues/794))
  - `jib.extraDirectory.path` configures the extra layer directory (still also configurable via `jib.extraDirectory = file(...)`)
  - `jib.extraDirectory.permissions` is a map from absolute path on container to the file's permission bits (represented as an octal string).
- Image digest is now written to `build/jib-image.digest`. ([#933](https://github.com/GoogleContainerTools/jib/issues/933))
- Adds the layer type to the layer history as comments. ([#1198](https://github.com/GoogleContainerTools/jib/issues/1198))
- `jibDockerBuild.dockerClient.executable` and `jibDockerBuild.dockerClient.environment` to set Docker client binary path (defaulting to `docker`) and additional environment variables to apply when running the binary. ([#1214](https://github.com/GoogleContainerTools/jib/pull/1214))

### Changed

- Removed deprecated `jib.jvmFlags`, `jib.mainClass`, `jib.args`, and `jib.format` in favor of the equivalents under `jib.container`. ([#461](https://github.com/GoogleContainerTools/jib/issues/461))
- `jibExportDockerContext` generates different directory layout and `Dockerfile` to enable WAR support. ([#1007](https://github.com/GoogleContainerTools/jib/pull/1007))
- File timestamps in the built image are set to 1 second since the epoch (hence 1970-01-01T00:00:01Z) to resolve compatibility with applications on Java 6 or below where the epoch means nonexistent or I/O errors; previously they were set to the epoch. ([#1079](https://github.com/GoogleContainerTools/jib/issues/1079))
- Sets tag to "latest" instead of "unspecified" if `jib.to.image` and project version are both unspecified when running `jibDockerBuild` or `jibBuildTar`. ([#1096](https://github.com/GoogleContainerTools/jib/issues/1096))

## 0.9.13

### Fixed

- Adds environment variable configuration to Docker context generator. ([#890 (comment)](https://github.com/GoogleContainerTools/jib/issues/890#issuecomment-430227555))

## 0.9.12

### Fixed

- `Cannot access 'image': it is public in <anonymous>` error. ([#1060](https://github.com/GoogleContainerTools/jib/issues/1060))

## 0.9.11

### Added

- `container.environment` configuration parameter to configure environment variables. ([#890](https://github.com/GoogleContainerTools/jib/issues/890))
- `container.appRoot` configuration parameter to configure app root in the image. ([#984](https://github.com/GoogleContainerTools/jib/pull/984))
- `jib.to.tags` (list) defines additional tags to push to. ([#978](https://github.com/GoogleContainerTools/jib/pull/978))

### Fixed

- Keep duplicate layers to match container history. ([#1017](https://github.com/GoogleContainerTools/jib/pull/1017))

## 0.9.10

### Added

- `container.labels` configuration parameter for configuring labels. ([#751](https://github.com/GoogleContainerTools/jib/issues/751))
- `container.entrypoint` configuration parameter to set the entrypoint. ([#579](https://github.com/GoogleContainerTools/jib/issues/579))
- `history` to layer metadata. ([#875](https://github.com/GoogleContainerTools/jib/issues/875))
- Propagates working directory from the base image. ([#902](https://github.com/GoogleContainerTools/jib/pull/902))

### Fixed

- Corrects permissions for directories in the container filesystem. ([#772](https://github.com/GoogleContainerTools/jib/pull/772))

## 0.9.9

### Added

- Passthrough labels from base image. ([#750](https://github.com/GoogleContainerTools/jib/pull/750/files))

### Changed

- Reordered classpath in entrypoint to use _resources_, _classes_, and then _dependencies_, to allow dependency patching.
 . ([#777](https://github.com/GoogleContainerTools/jib/issues/777)).  Note that this classpath ordering differs from that used by Gradle's `run` task.
- Changed logging level of missing build output directory message. ([#677](https://github.com/GoogleContainerTools/jib/issues/677))

### Fixed

- Gradle project dependencies have their `assemble` task run before running a jib task. ([#815](https://github.com/GoogleContainerTools/jib/issues/815))

## 0.9.8

### Added

- Docker context generation now includes snapshot dependencies and extra files. ([#516](https://github.com/GoogleContainerTools/jib/pull/516/files))
- Disable parallel operation by setting the `jibSerialize` system property to `true`. ([#682](https://github.com/GoogleContainerTools/jib/pull/682))

### Changed

- Propagates environment variables from the base image. ([#716](https://github.com/GoogleContainerTools/jib/pull/716))
- `allowInsecureRegistries` allows connecting to insecure HTTPS registries (for example, registries using self-signed certificates). ([#733](https://github.com/GoogleContainerTools/jib/pull/733))

### Fixed

- Slow image reference parsing. ([#680](https://github.com/GoogleContainerTools/jib/pull/680))
- Building empty layers. ([#516](https://github.com/GoogleContainerTools/jib/pull/516/files))
- Duplicate layer entries causing unbounded cache growth. ([#721](https://github.com/GoogleContainerTools/jib/issues/721))
- Incorrect authentication error message when target and base registry are the same. ([#758](https://github.com/GoogleContainerTools/jib/issues/758))

## 0.9.7

### Added

- Snapshot dependencies are added as their own layer. ([#584](https://github.com/GoogleContainerTools/jib/pull/584))
- `jibBuildTar` task to build an image tarball at `build/jib-image.tar`, which can be loaded into docker using `docker load`. ([#514](https://github.com/GoogleContainerTools/jib/issues/514))
- `container.useCurrentTimestamp` parameter to set the image creation time to the build time. ([#413](https://github.com/GoogleContainerTools/jib/issues/413))
- Authentication over HTTP using the `sendCredentialsOverHttp` system property. ([#599](https://github.com/GoogleContainerTools/jib/issues/599))
- HTTP connection and read timeouts for registry interactions configurable with the `jib.httpTimeout` system property. ([#656](https://github.com/GoogleContainerTools/jib/pull/656))
- Docker context export command-line option `--targetDir` to `--jibTargetDir`. ([#662](https://github.com/GoogleContainerTools/jib/issues/662))

### Changed

- Docker context export command-line option `--targetDir` to `--jibTargetDir`. ([#662](https://github.com/GoogleContainerTools/jib/issues/662))

### Fixed

- Using multi-byte characters in container configuration. ([#626](https://github.com/GoogleContainerTools/jib/issues/626))
- For Docker Hub, also tries registry aliases when getting a credential from the Docker config. ([#605](https://github.com/GoogleContainerTools/jib/pull/605))

## 0.9.6

### Fixed

- Using a private registry that does token authentication with `allowInsecureRegistries` set to `true`. ([#572](https://github.com/GoogleContainerTools/jib/pull/572))

## 0.9.5

### Added

- Incubating feature to build `src/main/jib` as extra layer in image. ([#562](https://github.com/GoogleContainerTools/jib/pull/562))

## 0.9.4

### Fixed

- Fixed handling case-insensitive `Basic` authentication method. ([#546](https://github.com/GoogleContainerTools/jib/pull/546))
- Fixed regression that broke pulling base images from registries that required token authentication. ([#549](https://github.com/GoogleContainerTools/jib/pull/549))

## 0.9.3

### Fixed

- Using Docker config for finding registry credentials (was not ignoring extra fields and handling `https` protocol). ([#524](https://github.com/GoogleContainerTools/jib/pull/524))

## 0.9.2

### Added

- Can configure `jibExportDockerContext` output directory with `jibExportDockerContext.targetDir`. ([#492](https://github.com/GoogleContainerTools/jib/pull/492))

### Changed

### Fixed

- Set `jibExportDockerContext` output directory with command line option `--targetDir`. ([#499](https://github.com/GoogleContainerTools/jib/pull/499))

## 0.9.1

### Added

- `container.ports` parameter to define container's exposed ports (similar to Dockerfile `EXPOSE`). ([#383](https://github.com/GoogleContainerTools/jib/issues/383))
- Can set `allowInsecureRegistries` parameter to `true` to use registries that only support HTTP. ([#388](https://github.com/GoogleContainerTools/jib/issues/388))

### Changed

- Fetches credentials from inferred credential helper before Docker config. ([#401](https://github.com/GoogleContainerTools/jib/issues/401))
- Container creation date set to timestamp 0. ([#341](https://github.com/GoogleContainerTools/jib/issues/341))
- Does not authenticate base image pull unless necessary - reduces build time by about 500ms. ([#414](https://github.com/GoogleContainerTools/jib/pull/414))
- `jvmFlags`, `mainClass`, `args`, and `format` are now grouped under `container` configuration object. ([#384](https://github.com/GoogleContainerTools/jib/issues/384))
- Warns instead of errors when classes not found. ([#462](https://github.com/GoogleContainerTools/jib/pull/462))

### Fixed

- Using Azure Container Registry now works - define credentials in `jib.to.auth`/`jib.from.auth`. ([#415](https://github.com/GoogleContainerTools/jib/issues/415))
- Supports `access_token` as alias to `token` in registry authentication. ([#420](https://github.com/GoogleContainerTools/jib/pull/420))
- Docker context export for Groovy project. ([#459](https://github.com/GoogleContainerTools/jib/pull/459))
- Visibility of `jib.to.image`. ([#460](https://github.com/GoogleContainerTools/jib/pull/460))

## 0.9.0

### Added

- Export a Docker context (including a Dockerfile) with `jibExportDockerContext`. ([#204](https://github.com/google/jib/issues/204))
- Warns if build may not be reproducible. ([#245](https://github.com/GoogleContainerTools/jib/pull/245))
- `jibDockerBuild` gradle task to build straight to Docker daemon. ([#265](https://github.com/GoogleContainerTools/jib/pull/265))
- `mainClass` is inferred by searching through class files if configuration is missing. ([#278](https://github.com/GoogleContainerTools/jib/pull/278))
- All tasks depend on `classes` by default. ([#335](https://github.com/GoogleContainerTools/jib/issues/335))
- Can now specify target image with `--image`. ([#328](https://github.com/GoogleContainerTools/jib/issues/328))
- `args` parameter to define default main arguments. ([#346](https://github.com/GoogleContainerTools/jib/issues/346))

### Changed

- Removed `reproducible` parameter - application layers will always be reproducible. ([#245](https://github.com/GoogleContainerTools/jib/pull/245))

### Fixed

- Using base images that lack entrypoints. ([#284](https://github.com/GoogleContainerTools/jib/pull/284)

## 0.1.1

### Added

- Warns if specified `mainClass` is not a valid Java class. ([#206](https://github.com/google/jib/issues/206))
- Can specify registry credentials to use directly with `from.auth` and `to.auth`. ([#215](https://github.com/google/jib/issues/215))
