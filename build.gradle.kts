import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("com.diffplug.spotless") version "8.0.0"
  id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "it.unibo"

version = "1.0.0-SNAPSHOT"

repositories { mavenCentral() }

val vertxVersion = "5.0.4"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "it.unibo.donkeykong.MainVerticle"
val launcherClassName = "io.vertx.launcher.application.VertxApplication"

application { mainClass.set("it.unibo.donkeykong.ui.DonkeyKongRushUI") } // launcherClassName

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-launcher-application")
  implementation("io.vertx:vertx-web")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
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
