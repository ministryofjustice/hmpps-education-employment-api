package uk.gov.justice.digital.hmpps.educationemploymentapi.model.external

import java.time.LocalDate

data class ReturnToCustodyDate(
  val bookingId: Long,
  val returnToCustodyDate: LocalDate
)
