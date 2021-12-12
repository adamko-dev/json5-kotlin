import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.0"
  jacoco
}

dependencies {

  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  val junitVersion = "5.8.2"
  testImplementation(enforcedPlatform("org.junit:junit-bom:$junitVersion"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher") {
    because("Only needed to run tests in a version of IntelliJ IDEA that bundles older versions")
  }

  val kotestVersion = "5.0.2"
  testImplementation(enforcedPlatform("io.kotest:kotest-bom:$kotestVersion"))
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.kotest:kotest-property:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
}

group = "at.syntaxerror"
version = "2.0.0"
description = "JSON5 for Kotlin"
//java.sourceCompatibility = JavaVersion.VERSION_11

java {
  withSourcesJar()
}

kotlin {
  jvmToolchain {
    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks.withType<KotlinCompile>().configureEach {

  kotlinOptions {
    jvmTarget = "11"
    apiVersion = "1.6"
    languageVersion = "1.6"
  }

  kotlinOptions.freeCompilerArgs += listOf(
    "-Xopt-in=kotlin.RequiresOptIn",
    "-Xopt-in=kotlin.ExperimentalStdlibApi",
    "-Xopt-in=kotlin.time.ExperimentalTime",
  )
}

tasks.compileTestKotlin {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=io.kotest.common.ExperimentalKotest"
}

tasks.withType<Test> {
  useJUnitPlatform()
  // report is always generated after tests run
  finalizedBy(tasks.withType<JacocoReport>())
}

jacoco {
  toolVersion = "0.8.7"
}

tasks.withType<JacocoReport> {
  dependsOn(tasks.withType<Test>())

  reports {
    xml.required.set(true)
    html.required.set(true)
    csv.required.set(false)
  }

  doLast {
    val htmlReportLocation = reports.html.outputLocation.locationOnly
      .map { it.asFile.resolve("index.html").invariantSeparatorsPath }

    logger.lifecycle("Jacoco report for ${project.name}: ${htmlReportLocation.get()}")
  }
}
