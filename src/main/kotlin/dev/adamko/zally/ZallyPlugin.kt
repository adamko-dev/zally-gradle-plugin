package dev.adamko.zally

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin

abstract class ZallyPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    val settings = target.createSettings()

    target.createZallyTask(settings)
  }

  private fun Project.createSettings(): ZallySettings =
    extensions.create<ZallySettings>(ZALLY_GRADLE_SETTINGS_NAME).apply {
      enabled.convention(true)
      disabledRules.convention(listOf(
        "PathParameterRule",
      ))
    }

  private fun Project.createZallyTask(settings: ZallySettings) {
    tasks.register<ZallyValidateTask>(ZALLY_GRADLE_TASK_NAME)

    tasks.withType<ZallyValidateTask>().configureEach {
      enabled = settings.enabled.get()
      openApiSpec.convention(settings.openApiSpec)
      disabledRules.convention(settings.disabledRules)
    }
  }

  companion object {
    const val ZALLY_GRADLE_SETTINGS_NAME = "zally"
    const val ZALLY_GRADLE_TASKS_GROUP = LifecycleBasePlugin.VERIFICATION_GROUP
    const val ZALLY_GRADLE_TASK_NAME = "zally"
  }
}
