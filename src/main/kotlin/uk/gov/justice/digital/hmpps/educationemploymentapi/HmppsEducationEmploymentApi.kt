package uk.gov.justice.digital.hmpps.educationemploymentapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication()
@ConfigurationPropertiesScan
class HmppsEducationEmploymentApi

fun main(args: Array<String>) {
  runApplication<HmppsEducationEmploymentApi>(args = args)
}
