package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

import java.time.LocalDateTime

data class WorkImpacts(
  val modifiedBy: String = "",
  val modifiedDateTime: LocalDateTime,

  val abilityToWorkImpactedBy: List<AbilityToWorkImpactedBy>,
  val caringResponsibilitiesFullTime: Boolean,
  val ableToManageMentalHealth: Boolean,
  val ableToManageDependencies: Boolean,
)
