package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.AbilityToWorkImpactedBy
import java.time.LocalDateTime

@Schema(name = "SARWorkImpacts", description = "Work impacts of the SAR Readiness Profile")
data class WorkImpacts(
  val modifiedDateTime: LocalDateTime,

  val abilityToWorkImpactedBy: List<AbilityToWorkImpactedBy>,
  val caringResponsibilitiesFullTime: Boolean,
  val ableToManageMentalHealth: Boolean,
  val ableToManageDependencies: Boolean,
)
