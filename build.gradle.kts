import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.5.1"
  kotlin("plugin.spring") version "1.7.10"
  kotlin("plugin.jpa") version "1.7.10"
  id("jacoco")
  kotlin("jvm") version "1.8.22"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

val integrationTest = task<Test>("integrationTest") {
  description = "Integration tests"
  group = "verification"
  shouldRunAfter("test")
}

tasks.named<Test>("integrationTest") {
  useJUnitPlatform()
  filter {
    includeTestsMatching("*.Int.*")
  }
}

tasks.named<Test>("test") {
  filter {
    excludeTestsMatching("*.Int.*")
  }
}

tasks.named("check") {
  setDependsOn(
    dependsOn.filterNot {
      it is TaskProvider<*> && it.name == "detekt"
    }
  )
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  // Spring boot dependencies
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  // GOVUK Notify:
  implementation("uk.gov.service.notify:notifications-java-client:3.17.3-RELEASE")

  // Enable kotlin reflect
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20")

  // Database dependencies
  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.4.0")

  implementation("io.arrow-kt:arrow-core:1.1.2")
  implementation("com.vladmihalcea:hibernate-types-52:2.16.2")

  // OpenAPI
  implementation("org.springdoc:springdoc-openapi-ui:1.6.9")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.9")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.9")

  implementation("com.google.code.gson:gson:2.9.0")

  // Test dependencies
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.35.0")
  testImplementation("io.swagger.parser.v3:swagger-parser-v2-converter:2.0.33")
  testImplementation("org.mockito:mockito-inline:4.6.1")
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
  testImplementation("com.h2database:h2")
  implementation(kotlin("stdlib-jdk8"))
}
repositories {
  mavenCentral()
}

jacoco {
  // You may modify the Jacoco version here
  toolVersion = "0.8.8"
}

kotlin {
  jvmToolchain {
    this.languageVersion.set(JavaLanguageVersion.of("18"))
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(18))
}

dependencyCheck {
  suppressionFiles.add("$rootDir/dependencyCheck/suppression.xml")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
  jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
  jvmTarget = "1.8"
}
