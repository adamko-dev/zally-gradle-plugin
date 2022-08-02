rootProject.name = "zally-gradle-plugin"


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
