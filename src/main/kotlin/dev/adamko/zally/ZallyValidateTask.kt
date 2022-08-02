package dev.adamko.zally

import com.typesafe.config.ConfigFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

abstract class ZallyValidateTask : DefaultTask() {

  @get:InputFile
  abstract val openApiSpec: RegularFileProperty

  @get:Input
  abstract val disabledRules: SetProperty<String>

  init {
    group = ZallyPlugin.ZALLY_GRADLE_TASKS_GROUP
  }

  @TaskAction
  fun validate() {

    val openApiSpec = openApiSpec.asFile.get().toPath()
    val disabledRules = disabledRules.get().toSet()

    val runner = ZallyRunner(ConfigFactory.load())

    val checkResults = runner.validate(
      openApiSpec,
      disabledRules,
    )

    val message = checkResults.entries
      .filter { (_, results) -> results.isNotEmpty() }
      .joinToString("\n") { (_, results) ->
        "Zally Validation failed: \n" + results.joinToString("") { result ->
          "  - [${result.violationType}] ${result.description} - ${result.pointer}\n"
        }
      }

    if (message.isNotBlank()) {
      throw GradleException(message)
    }
  }
}
