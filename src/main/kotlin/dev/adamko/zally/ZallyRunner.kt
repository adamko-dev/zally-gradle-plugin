package dev.adamko.zally

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.classgraph.ClassGraph
import io.swagger.parser.OpenAPIParser
import java.io.IOException
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.invariantSeparatorsPathString
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.zalando.zally.core.CheckDetails
import org.zalando.zally.core.DefaultContext
import org.zalando.zally.core.JsonPointerLocator
import org.zalando.zally.core.Result
import org.zalando.zally.core.RuleDetails
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.RuleSet
import org.zalando.zally.rule.api.Violation

// translated from https://github.com/ethlo/zally-maven-plugin/blob/8c4830a27759793d5772bc1e873a5520edc7a463/src/main/java/com/ethlo/zally/ZallyRunner.java
class ZallyRunner(ruleConfigs: Config) {

  private val logger: Logger = Logging.getLogger(this::class.java)

  private val ruleClasses: List<Class<*>> = loadRuleClasses()

  private val rules: List<RuleDetails> =
    ruleClasses.map { ruleClass ->
      val simpleName = ruleClass.simpleName
      logger.debug("Loading rule $simpleName")
      val instance = createRuleInstance(ruleClass, ruleConfigs)
      val ruleAnnotation = ruleClass.getAnnotation(Rule::class.java)
      RuleDetails(
        createInstance(ruleAnnotation.ruleSet.java),
        ruleAnnotation,
        instance
      )
    }

  fun validate(
    url: Path,
    skipped: Set<String>,
  ): Map<CheckDetails, List<Result>> {

    val openApiFileLocation: String = url.invariantSeparatorsPathString

    val parseResult = OpenAPIParser().readLocation(openApiFileLocation, null, null)
    val openAPI = parseResult.openAPI
    val context: Context = DefaultContext("", openAPI, null)
    val returnValue: MutableMap<CheckDetails, List<Result>> = LinkedHashMap()

    for ((ruleSet, rule, instance) in rules) {
      if (!skipped.contains(instance.javaClass.simpleName)) {
        for (method in instance.javaClass.declaredMethods) {
          val checkAnnotation = method.getAnnotation(
            Check::class.java
          )
          if (checkAnnotation != null && method.parameterTypes.size == 1 && method.parameterTypes[0] == Context::class.java) {
            val violationList: MutableList<Result> = ArrayList()
            val checkDetails = performCheck(
              context,
              violationList,
              instance,
              rule,
              ruleSet,
              method,
              checkAnnotation,
              openApiFileLocation,
            )
            returnValue[checkDetails] = violationList
          }
        }
      }
    }
    return returnValue
  }

  private fun performCheck(
    context: Context,
    violationList: MutableList<Result>,
    instance: Any,
    ruleAnnotation: Rule,
    ruleSet: RuleSet,
    method: Method,
    checkAnnotation: Check,
    url: String
  ): CheckDetails {
    val checkDetails = CheckDetails(ruleSet, ruleAnnotation, instance, checkAnnotation, method)
    val result: Any? = method.invoke(instance, context)

    if (result != null) {
      if (result is Iterable<*>) {
        for (violation in result as Iterable<Violation>) {
          // Ignore violations if there are x-zally-ignore markers.
          if (context.isIgnored(violation.pointer, checkDetails.rule.id)
            || context.isIgnored(violation.pointer, "*")
          ) {
            System.out.printf(
              "Ignore violation, rule = %s, at %s%n",
              checkDetails.rule.id,
              violation.pointer
            )
            continue
          }
          violationList.add(handleViolation(url, checkDetails, violation))
        }
      } else if (result is Violation) {
        violationList.add(handleViolation(url, checkDetails, result))
      }
    }
    return checkDetails
  }

  private fun loadRuleClasses(): List<Class<*>> {
    return ClassGraph().enableClassInfo().enableAnnotationInfo().scan().use { result ->
      val classInfos = result.getClassesWithAnnotation(Rule::class.java)
      classInfos.map { it.loadClass() }
    }
  }

  private fun createRuleInstance(
    ruleClass: Class<*>,
    ruleConfig: Config
  ): Any {
    return try {
      for (constructor in ruleClass.constructors) {
        val paramTypes = constructor.parameterTypes
        if (paramTypes.size == 1 && paramTypes[0] == Config::class.java) {
          return constructor.newInstance(ruleConfig.withFallback(ConfigFactory.parseMap(emptyMap())))
        }
      }
      ruleClass.getConstructor().newInstance()
    } catch (e: Exception) {
      throw RuntimeException("Cannot instantiate rule $ruleClass", e)
    }
  }

  private fun createInstance(type: Class<*>): RuleSet {
    return try {
      val constructor = type.getConstructor()
      constructor.newInstance() as RuleSet
    } catch (e: Exception) {
      throw RuntimeException("Cannot instantiate class $type", e)
    }
  }

  private fun handleViolation(
    url: String,
    details: CheckDetails,
    violation: Violation
  ): Result {
    val locator = try {
      JsonPointerLocator(Files.readString(Paths.get(url)))
    } catch (e: IOException) {
      logger.warn("Could not read File $url - ${e.message}")
      JsonPointerLocator("")
    }
    return Result(
      details.rule.id,
      details.ruleSet.url(details.rule),
      details.rule.title,
      violation.description,
      details.check.severity,
      violation.pointer,
      locator.locate(violation.pointer)
    )
  }

  fun getRules(): List<RuleDetails> {
    return rules
  }
}
