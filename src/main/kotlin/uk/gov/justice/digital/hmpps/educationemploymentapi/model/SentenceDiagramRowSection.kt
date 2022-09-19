package uk.gov.justice.digital.hmpps.educationemploymentapi.model

import java.time.LocalDate

data class SentenceDiagramRowSection(
  val start: LocalDate,
  val end: LocalDate,
  val description: String?
)
