package uk.gov.justice.digital.hmpps.educationemploymentapi.model

import java.time.LocalDate

data class Adjustment(
  val appliesToSentencesFrom: LocalDate,
  val numberOfDays: Int,
  val fromDate: LocalDate? = null,
  val toDate: LocalDate? = null
)
