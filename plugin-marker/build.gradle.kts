plugins {
  kotlin("jvm")

  `maven-publish`
}

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
