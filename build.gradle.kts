import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.0"
  kotlin("plugin.spring") version "2.0.0"
  kotlin("plugin.jpa") version "2.0.0"
  id("jacoco")
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.projectlombok:lombok:1.18.30")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:3.0.0")
  implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.7.5")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.4.0")
  implementation("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  implementation("commons-codec:commons-codec:1.17.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

  implementation("org.apache.commons:commons-lang3:3.14.0")
  implementation("org.ehcache:ehcache:3.10.8")
  implementation("com.zaxxer:HikariCP:5.1.0")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  // Database dependencies
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.3")

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.mockk:mockk:1.13.11")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
  testImplementation("org.testcontainers:localstack")
  testImplementation("com.h2database:h2")
}

kotlin {
  jvmToolchain(21)
}

repositories {
  mavenCentral()
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = JVM_21
    compilerOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
  }
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    html.required.set(true)
    xml.required.set(true)
  }
}
