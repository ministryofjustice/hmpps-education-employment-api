package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.CircumstanceChangesRequiredToWork
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import java.time.Instant
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined as SupportDeclinedEntity

@Schema(name = "SARSupportDeclined", description = "Support declined of the SAR Readiness Profile")
data class SupportDeclined(
  val modifiedBy: String?,
  val modifiedDateTime: Instant?,

  val supportToWorkDeclinedReason: List<SupportToWorkDeclinedReason>,
  val supportToWorkDeclinedReasonOther: String,
  val circumstanceChangesRequiredToWork: List<CircumstanceChangesRequiredToWork>,
  val circumstanceChangesRequiredToWorkOther: String,
) {
  constructor(entity: SupportDeclinedEntity, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime?.instantFromZone(timeZoneId),
    supportToWorkDeclinedReason = entity.supportToWorkDeclinedReason.toList(),
    supportToWorkDeclinedReasonOther = entity.supportToWorkDeclinedReasonOther,
    circumstanceChangesRequiredToWork = entity.circumstanceChangesRequiredToWork.toList(),
    circumstanceChangesRequiredToWorkOther = entity.circumstanceChangesRequiredToWorkOther,
  )
}
