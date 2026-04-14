package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import java.time.Instant
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportAccepted as SupportAcceptedEntity

@Schema(name = "SARSupportAccepted", description = "Support accepted of the SAR Readiness Profile")
data class SupportAccepted(
  val modifiedBy: String?,
  val modifiedDateTime: Instant?,
  val actionsRequired: ActionsRequired,
  val workImpacts: WorkImpacts,
  val workInterests: WorkInterests,
  val workExperience: WorkExperience,
) {
  constructor(entity: SupportAcceptedEntity, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime?.instantFromZone(timeZoneId),
    actionsRequired = ActionsRequired(entity.actionsRequired, timeZoneId),
    workImpacts = WorkImpacts(entity.workImpacts, timeZoneId),
    workInterests = WorkInterests(entity.workInterests, timeZoneId),
    workExperience = WorkExperience(entity.workExperience, timeZoneId),
  )
}
