package uk.gov.justice.digital.hmpps.educationemploymentapi.model

import java.time.LocalDate

data class WorkingDay(
  val date: LocalDate,
  val adjustedForWeekend: Boolean = false,
  val adjustedForBankHoliday: Boolean = false
)
