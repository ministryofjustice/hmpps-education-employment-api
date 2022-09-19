package uk.gov.justice.digital.hmpps.educationemploymentapi.model

import uk.gov.justice.digital.hmpps.educationemploymentapi.enumerations.ReleaseDateType
import java.time.LocalDate
import java.time.Period

data class CalculationResult(
  val dates: Map<ReleaseDateType, LocalDate>,
  val breakdownByReleaseDateType: Map<ReleaseDateType, ReleaseDateCalculationBreakdown> = mapOf(),
  val otherDates: Map<ReleaseDateType, LocalDate> = mapOf(),
  val effectiveSentenceLength: Period
)
