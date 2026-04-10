package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.AbilityToWorkImpactedBy
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.ModificationAudited
import java.time.Instant
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkImpacts as WorkImpactsEntity

@Schema(name = "SARWorkImpacts", description = "Work impacts of the SAR Readiness Profile")
data class WorkImpacts(
  override val modifiedBy: String,
  override val modifiedDateTime: Instant,

  val abilityToWorkImpactedBy: List<AbilityToWorkImpactedBy>,
  val caringResponsibilitiesFullTime: Boolean,
  val ableToManageMentalHealth: Boolean,
  val ableToManageDependencies: Boolean,
) : ModificationAudited {
  constructor(entity: WorkImpactsEntity, timeZoneId: ZoneId) : this(
    modifiedBy = entity.modifiedBy,
    modifiedDateTime = entity.modifiedDateTime.instantFromZone(timeZoneId),
    abilityToWorkImpactedBy = entity.abilityToWorkImpactedBy.toList(), // deep copy
    caringResponsibilitiesFullTime = entity.caringResponsibilitiesFullTime,
    ableToManageMentalHealth = entity.ableToManageMentalHealth,
    ableToManageDependencies = entity.ableToManageDependencies,
  )
}
