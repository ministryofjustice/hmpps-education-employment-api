package uk.gov.justice.digital.hmpps.educationemployment.api.data.sarprofile

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Action
import java.time.LocalDateTime

@Schema(name = "SARActionsRequired", description = "Actions required of the SAR Readiness Profile")
data class ActionsRequired(
  val modifiedDateTime: LocalDateTime,
  val actions: List<Action>,
)
