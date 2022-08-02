package dev.adamko.zally

import com.typesafe.config.ConfigFactory
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

abstract class ZallyValidateTask : Task {

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

    val zr = ZallyRunner(ConfigFactory.load())

    val zRResult = zr.validate(
      openApiSpec,
      disabledRules,
    )
  }
}
