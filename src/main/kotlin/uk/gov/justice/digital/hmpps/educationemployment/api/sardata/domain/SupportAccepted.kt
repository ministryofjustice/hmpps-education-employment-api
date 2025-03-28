package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(name = "SARSupportAccepted", description = "Support accepted of the SAR Readiness Profile")
data class SupportAccepted(
  var modifiedDateTime: LocalDateTime?,
  val actionsRequired: ActionsRequired,
  val workImpacts: WorkImpacts,
  val workInterests: WorkInterests,
  val workExperience: WorkExperience,
)
