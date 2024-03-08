plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.10.1"
  kotlin("plugin.spring") version "1.9.21"
  kotlin("plugin.jpa") version "1.9.21"
  id("name.remal.integration-tests") version "4.0.2"
  id("jvm-test-suite")
  id("jacoco")
}

configurations {
  implementation {
    exclude(module = "commons-logging")
    exclude(module = "log4j")
  }
  testImplementation { exclude(group = "org.junit.vintage") }
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
    }
    val integrationTest by getting(JvmTestSuite::class) {
      useJUnitJupiter()
      dependencies {
        implementation(project())
      }
      sourceSets["main"].apply {
        kotlin.srcDir("${layout.buildDirectory}/src/main/kotlin")
      }
      sourceSets["integrationTest"].apply {
        kotlin.srcDir("${layout.buildDirectory}/src/integrationTest/kotlin")
      }
    }
  }
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
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:2.1.1")
  implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:1.29.0")
  implementation("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  implementation("commons-codec:commons-codec:1.16.0")
  implementation("io.swagger:swagger-annotations:1.6.12")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

  implementation("org.apache.commons:commons-lang3:3.14.0")
  implementation("commons-io:commons-io:2.15.1")
  implementation("com.google.guava:guava:32.1.3-jre")

  implementation("org.ehcache:ehcache:3.10.8")
  implementation("com.zaxxer:HikariCP:5.1.0")
  implementation("com.oracle.database.jdbc:ojdbc10:19.21.0.0")
  implementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
  implementation("org.hibernate.orm:hibernate-community-dialects")

// Database dependencies
  implementation("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.4.0")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.mockk:mockk:1.13.9")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.0")
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.testcontainers:localstack")

  testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
  testImplementation("io.kotest:kotest-assertions-core:5.8.0")
  testImplementation("io.kotest:kotest-property:5.8.0")
  testImplementation("com.h2database:h2")
}

kotlin {
  jvmToolchain(21)
}
kotlin {
  sourceSets["main"].apply {
    kotlin.srcDir("${layout.buildDirectory}/generated/src/main/kotlin")
  }
}
java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

repositories {
  mavenCentral()
}
tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
    withType<JavaCompile> {
      sourceCompatibility = "21"
    }
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
    named("assemble") {
      doFirst {
        delete(
          fileTree(project.layout.buildDirectory.get())
            .include("libs/*-plain.jar")
        )
      }
    }
  }
}
