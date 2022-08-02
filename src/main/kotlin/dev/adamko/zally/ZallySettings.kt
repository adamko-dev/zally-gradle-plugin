package dev.adamko.zally

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile

interface ZallySettings {

  @get:InputFile
  val openApiSpec: RegularFileProperty

  @get:Input
  val enabled: Property<Boolean>

  @get:Input
  val disabledRules: SetProperty<String>
}
