plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.4.1-beta"
  kotlin("plugin.spring") version "1.7.10"
}

dependencyCheck {
  suppressionFiles.add("reactive-suppressions.xml")
  // Please remove the below suppressions once it has been suppressed in the DependencyCheck plugin (see this issue: https://github.com/jeremylong/DependencyCheck/issues/4616)
  suppressionFiles.add("postgres-suppressions.xml")
}

configurations {
  implementation { exclude(module = "spring-boot-starter-web") }
  implementation { exclude(module = "spring-boot-starter-tomcat") }
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springdoc:springdoc-openapi-webflux-ui:1.6.9")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.9")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")
  implementation("io.jsonwebtoken:jjwt:0.9.1")
  implementation("io.opentelemetry:opentelemetry-api:1.16.0")
  implementation("io.r2dbc:r2dbc-postgresql:0.8.12.RELEASE")
  runtimeOnly("org.postgresql:r2dbc-postgresql:1.0.0.RC1")
  implementation("org.flywaydb:flyway-core:8.5.12")
  implementation("com.vladmihalcea:hibernate-types-52:2.16.2")
  runtimeOnly("org.postgresql:postgresql:42.4.0")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
    }
  }
}
