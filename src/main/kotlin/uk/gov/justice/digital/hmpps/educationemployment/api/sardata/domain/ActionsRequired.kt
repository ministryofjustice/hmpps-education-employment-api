package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(name = "SARActionsRequired", description = "Actions required of the SAR Readiness Profile")
data class ActionsRequired(
  val modifiedDateTime: LocalDateTime,
  val actions: List<Action>,
)
