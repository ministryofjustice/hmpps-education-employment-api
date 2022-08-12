package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

import java.time.LocalDateTime

data class SupportDeclined(
  val modifiedBy: String,
  val modifiedDateTime: LocalDateTime,

  val supportToWorkDeclinedReason: List<SupportToWorkDeclinedReason>,
  val supportToWorkDeclinedReasonOther: String,
  val circumstanceChangesRequiredToWork: List<CircumstanceChangesRequiredToWork>,
  val circumstanceChangesRequiredToWorkOther: String
)
