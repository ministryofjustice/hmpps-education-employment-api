plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.2.2"
  kotlin("plugin.spring") version "2.3.21"
  kotlin("plugin.jpa") version "2.3.21"
  id("jacoco")
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.1.1")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.data:spring-data-envers")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
  implementation("com.fasterxml.jackson.core:jackson-databind")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.1.1")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test")

  testRuntimeOnly("org.springframework.boot:spring-boot-starter-data-jpa-test")
}

testing {
  suites {
    @Suppress("UnstableApiUsage", "unused")
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
    }

    @Suppress("UnstableApiUsage", "unused")
    val integrationTest by registering(JvmTestSuite::class) {
      dependencies {
        kotlin.target.compilations { named("integrationTest") { associateWith(getByName("main")) } }
        implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.1.1")
        implementation("uk.gov.justice.service.hmpps:hmpps-subject-access-request-test-support:2.3.0")
        implementation("org.springframework.boot:spring-boot-starter-test")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
        implementation("org.springframework.boot:spring-boot-restclient")
        implementation("org.springframework.boot:spring-boot-resttestclient")
        implementation("org.springframework.boot:spring-boot-webtestclient")
        implementation("org.springframework.security:spring-security-test")
        runtimeOnly("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")
        runtimeOnly("org.flywaydb:flyway-database-postgresql")
        implementation("org.testcontainers:testcontainers-postgresql")
        implementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
        implementation("io.swagger.parser.v3:swagger-parser:2.1.40") {
          exclude(group = "io.swagger.core.v3")
        }
      }

      targets {
        all {
          testTask.configure {
            shouldRunAfter(test)
          }
        }
      }
    }
  }
}

tasks {
  named("check") {
    dependsOn(named("test"), named("integrationTest"))
  }

  named("test") {
    finalizedBy("jacocoTestReport")
  }

  named("integrationTest") {
    mustRunAfter(named("test"))
  }

  named<JacocoReport>("jacocoTestReport") {
    reports {
      html.required.set(true)
      xml.required.set(true)
    }
  }

  named("assemble") {
    doFirst {
      delete(
        fileTree(project.layout.buildDirectory.get())
          .include("libs/*-plain.jar"),
      )
    }
  }
}
dependencyCheck {
  failBuildOnCVSS = 5f
  suppressionFiles.add("test-suppressions.xml")
  format = "ALL"
  analyzers.assemblyEnabled = false
}
