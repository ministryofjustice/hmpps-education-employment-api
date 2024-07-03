import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.0"
  kotlin("plugin.spring") version "2.0.0"
  kotlin("plugin.jpa") version "2.0.0"
  id("jacoco")
}
configurations {
  implementation {
    exclude(group = "org.xmlunit", module = "xmlunit-core")
  }
  testImplementation {
    exclude(group = "org.xmlunit", module = "xmlunit-core")
  }
}
dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:2.1.1") {
    exclude(group = "org.xmlunit", module = "xmlunit-core")
  }
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0") {
    exclude(group = "org.xmlunit", module = "xmlunit-core")
  }
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("io.jsonwebtoken:jjwt-impl:0.12.5")
  implementation("io.jsonwebtoken:jjwt-jackson:0.12.5")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.3")
  testImplementation("org.testcontainers:localstack")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.testcontainers:testcontainers")
  developmentOnly("org.springframework.boot:spring-boot-devtools") {
    exclude(group = "org.xmlunit", module = "xmlunit-core")
  }
  testImplementation("org.springframework.security:spring-security-test") {
    exclude(group = "org.xmlunit", module = "xmlunit-core")
  }
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5") {
    exclude(group = "org.xmlunit", module = "xmlunit-core")
  }
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("com.h2database:h2")
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
    }

    register<JvmTestSuite>("integrationTest") {
      dependencies {
        testType.set(TestSuiteType.INTEGRATION_TEST)
        kotlin.target.compilations { named("integrationTest") { associateWith(getByName("main")) } }
        implementation("org.springframework.boot:spring-boot-starter-test") {
          exclude(group = "org.xmlunit", module = "xmlunit-core")
        }
        implementation("org.springframework.security:spring-security-test") {
          exclude(group = "org.xmlunit", module = "xmlunit-core")
        }
        implementation("io.jsonwebtoken:jjwt-impl:0.12.5")
        implementation("io.jsonwebtoken:jjwt-jackson:0.12.5")
        implementation("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")
        implementation("com.microsoft.azure:applicationinsights-logging-logback")
        runtimeOnly("org.flywaydb:flyway-database-postgresql")
        implementation("com.zaxxer:HikariCP:5.1.0")
        implementation("com.h2database:h2")
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
  withType<KotlinCompile> {
    named("check") {
      dependsOn(testing.suites.named("integrationTest"))
    }

    named("compileIntegrationTestKotlin") {
      dependsOn(named("copyAgent"))
    }

    named<JacocoReport>("jacocoTestReport") {
      dependsOn(named("check"))
      reports {
        html.required.set(true)
        xml.required.set(true)
      }
    }

    finalizedBy(named("jacocoTestReport"))
  }
}
