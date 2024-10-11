package uk.gov.justice.digital.hmpps.educationemployment.api.data.sarprofile

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.CircumstanceChangesRequiredToWork
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.SupportToWorkDeclinedReason
import java.time.LocalDateTime

@Schema(name = "SARSupportDeclined", description = "Support declined of the SAR Readiness Profile")
data class SupportDeclined(
  var modifiedDateTime: LocalDateTime?,

  val supportToWorkDeclinedReason: List<SupportToWorkDeclinedReason>,
  val supportToWorkDeclinedReasonOther: String,
  val circumstanceChangesRequiredToWork: List<CircumstanceChangesRequiredToWork>,
  val circumstanceChangesRequiredToWorkOther: String,
)
