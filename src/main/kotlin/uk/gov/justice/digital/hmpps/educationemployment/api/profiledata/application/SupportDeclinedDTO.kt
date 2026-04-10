package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.CircumstanceChangesRequiredToWork
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportToWorkDeclinedReason
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.EntityConvertible
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.ModificationAuditable
import java.time.Instant
import java.time.ZoneId

data class SupportDeclinedDTO(
  override var modifiedBy: String? = null,
  override var modifiedDateTime: Instant? = null,

  @get:Schema(description = "Reason(s) of not wanting support")
  val supportToWorkDeclinedReason: List<SupportToWorkDeclinedReason>,
  @get:Schema(description = "Other reason of not wanting support")
  val supportToWorkDeclinedReasonOther: String,
  @get:Schema(description = "Change in circumstance that would help")
  val circumstanceChangesRequiredToWork: List<CircumstanceChangesRequiredToWork>,
  @get:Schema(description = "Other change in circumstance that would help")
  val circumstanceChangesRequiredToWorkOther: String,
) : ModificationAuditable,
  EntityConvertible<SupportDeclined> {
  /**
   * Domain entity to DTO
   */
  constructor(entity: SupportDeclined, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime?.instantFromZone(timeZoneId),
    supportToWorkDeclinedReason = entity.supportToWorkDeclinedReason.toList(),
    supportToWorkDeclinedReasonOther = entity.supportToWorkDeclinedReasonOther,
    circumstanceChangesRequiredToWork = entity.circumstanceChangesRequiredToWork.toList(),
    circumstanceChangesRequiredToWorkOther = entity.circumstanceChangesRequiredToWorkOther,
  )

  override fun entity(timeZoneId: ZoneId) = SupportDeclined(
    modifiedBy = modifiedBy,
    modifiedDateTime = modifiedDateTime?.localDateTimeAtZone(timeZoneId),
    supportToWorkDeclinedReason = supportToWorkDeclinedReason.toList(),
    supportToWorkDeclinedReasonOther = supportToWorkDeclinedReasonOther,
    circumstanceChangesRequiredToWork = circumstanceChangesRequiredToWork.toList(),
    circumstanceChangesRequiredToWorkOther = circumstanceChangesRequiredToWorkOther,
  )
}
