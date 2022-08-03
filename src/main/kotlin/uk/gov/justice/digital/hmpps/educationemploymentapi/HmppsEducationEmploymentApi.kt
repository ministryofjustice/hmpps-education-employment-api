package uk.gov.justice.digital.hmpps.educationemploymentapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class HmppsEducationEmploymentApi

fun main(args: Array<String>) {
  runApplication<HmppsEducationEmploymentApi>(*args)
}
