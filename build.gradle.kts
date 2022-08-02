import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version embeddedKotlinVersion
  `kotlin-dsl`
  `java-gradle-plugin`
}

dependencies {
  implementation(platform(kotlin("bom")))

  implementation("io.swagger.parser.v3:swagger-parser:2.1.1")

  val zalandoZally = "2.1.0"
  implementation("org.zalando:zally-core:$zalandoZally")
  implementation("org.zalando:zally-rule-api:$zalandoZally")
  implementation("org.zalando:zally-ruleset-zalando:$zalandoZally")
  implementation("org.zalando:zally-ruleset-zally:$zalandoZally")
  implementation("org.zalando:zally-test:$zalandoZally")

  implementation("io.github.classgraph:classgraph:4.8.149")
}

gradlePlugin {
  plugins {
    create("zally") {
      id = "dev.adamko.zally"
      displayName = "Zally OpenAPI Validator (unofficial Gradle Plugin"
      description = "A minimalistic, simple-to-use OpenAPI 2 and 3 linter"
      implementationClass = "dev.adamko.zally.ZallyPlugin"
    }
  }
}

tasks.wrapper {
  gradleVersion = "7.5"
  distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    this.freeCompilerArgs += listOf(
      "-Xopt-in=kotlin.io.path.ExperimentalPathApi",
    )
  }
}
