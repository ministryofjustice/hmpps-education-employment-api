package uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model

import java.time.LocalDateTime

data class ReadinessProfileFilter(
  val offenderIds: List<String>? = null,
)