plugins {
  kotlin("jvm")

  `maven-publish`
}

// custom Gradle Plugin Marker, to workaround Jitpack overriding the group
// https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers

group = "com.github.adamko-dev.zally-gradle-plugin"
version = "0.0.1-SNAPSHOT"

dependencies {
  api(projects.zallyGradlePlugin)
}

publishing {
  repositories {
    maven(file("$rootDir/build/maven-internal")) {
      name = "ProjectLocalDir"
    }
  }

  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = "com.github.adamko-dev.zally-gradle-plugin.gradle.plugin"
      from(components["java"])
    }
  }
}

java {
  withSourcesJar()
}
