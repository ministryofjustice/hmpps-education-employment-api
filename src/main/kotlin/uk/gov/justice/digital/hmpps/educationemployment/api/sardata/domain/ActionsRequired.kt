package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.Action
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.ModificationAudited
import java.time.Instant
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionsRequired as ActionsRequiredEntity

@Schema(name = "SARActionsRequired", description = "Actions required of the SAR Readiness Profile")
data class ActionsRequired(
  override val modifiedBy: String,
  override val modifiedDateTime: Instant,
  val actions: List<Action>,
) : ModificationAudited {
  constructor(entity: ActionsRequiredEntity, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime.instantFromZone(timeZoneId),
    actions = entity.actions.map { it.copy(id = it.id?.toList()) }, // deep copy
  )
}
