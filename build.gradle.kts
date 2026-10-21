import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("com.diffplug.spotless") version "8.10.4"
  id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "it.unibo"

repositories { mavenCentral() }

val vertxVersion = "4.5.35"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "it.unibo.donkeykong.MainVerticle"
val launcherClassName = "it.unibo.donkeykong.Launcher"

application { mainClass.set("it.unibo.donkeykong.Launcher") }

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.22.4")

  val osClassifiers = listOf("win", "mac-aarch64", "linux")
  for (os in osClassifiers) {
    implementation("org.openjfx:javafx-base:17.0.14:$os")
    implementation("org.openjfx:javafx-graphics:17.0.14:$os")
    implementation("org.openjfx:javafx-controls:17.0.14:$os")
    implementation("org.openjfx:javafx-fxml:17.0.14:$os")
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

spotless {
  java {
    googleJavaFormat()
    trimTrailingWhitespace()
    endWithNewline()
  }

  kotlinGradle {
    target("*.gradle.kts")
    ktfmt()
  }
}

javafx {
  version = "17.0.14"
  modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest { attributes(mapOf("Main-Verticle" to mainVerticleName)) }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging { events = setOf(PASSED, SKIPPED, FAILED) }
}

tasks.withType<JavaExec> { args = listOf(mainVerticleName) }

tasks.withType<JavaCompile> { dependsOn("spotlessApply") }
