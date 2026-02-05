plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.3.0"
  kotlin("plugin.spring") version "2.2.21"
  kotlin("plugin.jpa") version "2.2.21"
  id("jacoco")
}

ext["netty.version"] = "4.1.130.Final"

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.8.1")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.data:spring-data-envers")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("io.jsonwebtoken:jjwt-api:0.12.6")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.8.1")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
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
        implementation("org.springframework.boot:spring-boot-starter-test")
        implementation("org.springframework.security:spring-security-test")
        runtimeOnly("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")
        implementation("org.flywaydb:flyway-core")
        runtimeOnly("org.flywaydb:flyway-database-postgresql")
        implementation("org.testcontainers:postgresql") {
          implementation("org.apache.commons:commons-compress:1.27.1")
        }
        implementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

        implementation("io.jsonwebtoken:jjwt-api:0.12.6")
        runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
        runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
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
