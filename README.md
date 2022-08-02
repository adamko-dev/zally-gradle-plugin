# Zally Gradle Plugin

[Zally Gradle Plugin](https://github.com/adamko-dev/zally-gradle-plugin) is an
unofficial Gradle Plugin for [Zally](https://github.com/zalando/zally), the
minimalistic, simple-to-use OpenAPI 2 and 3 linter.

The Zally Gradle plugin is based on the
[Zally Maven plugin](https://github.com/ethlo/zally-maven-plugin).

**This project is unfinished**. I'm sharing the work so far to get the ball rolling.

### Setup

```kotlin
// build.gradle.kts

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://jitpack.io")
  }
}

plugins {
//  id("dev.adamko.zally")
  id("com.github.adamko-dev.zally-gradle-plugin") version "main-SNAPSHOT"
}

zally {
  openApiSpec.set(
    layout.projectDirectory.file("src/main/resources/openapi.yml")
  )
}
```

Run: `./gradlew zally`
