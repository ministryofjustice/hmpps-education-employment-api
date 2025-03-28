plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.0.0"
  kotlin("plugin.spring") version "2.1.20"
  kotlin("plugin.jpa") version "2.1.20"
  id("jacoco")
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.2") {
    implementation("org.apache.commons:commons-compress:1.27.1")
  }
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.data:spring-data-envers")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5") {
    implementation("org.webjars:swagger-ui:5.20.0")
  }
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
  implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.5")
  testImplementation("org.testcontainers:localstack")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.testcontainers:testcontainers")
  developmentOnly("org.springframework.boot:spring-boot-devtools")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("com.h2database:h2")
}

testing {
  suites {
    @Suppress("UnstableApiUsage")
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
    }

    @Suppress("UnstableApiUsage")
    val integrationTest by registering(JvmTestSuite::class) {
      dependencies {
        testType.set(TestSuiteType.INTEGRATION_TEST)
        kotlin.target.compilations { named("integrationTest") { associateWith(getByName("main")) } }
        implementation("org.springframework.boot:spring-boot-starter-test")
        implementation("org.springframework.security:spring-security-test")
        implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
        implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
        implementation("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")
        implementation("com.microsoft.azure:applicationinsights-logging-logback")
        runtimeOnly("org.flywaydb:flyway-database-postgresql")
        implementation("com.zaxxer:HikariCP:5.1.0")
        implementation("org.testcontainers:postgresql") {
          implementation("org.apache.commons:commons-compress:1.27.1")
        }
        implementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
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
