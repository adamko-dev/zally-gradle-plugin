rootProject.name = "zally-gradle-plugin"

include(
  ":plugin-marker"
)

dependencyResolutionManagement {

  repositories {
    mavenCentral()
    gradlePluginPortal()
  }

  pluginManagement {
    repositories {
      gradlePluginPortal()
      mavenCentral()
    }
  }

  @Suppress("UnstableApiUsage") // centralised repositories are incubating
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
