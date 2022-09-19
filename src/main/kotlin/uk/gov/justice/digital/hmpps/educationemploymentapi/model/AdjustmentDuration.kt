package uk.gov.justice.digital.hmpps.educationemploymentapi.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.DAYS

data class AdjustmentDuration(
  @Schema(description = "Amount of adjustment")
  val adjustmentValue: Int = 0,
  @Schema(description = "Unit of adjustment", example = "DAYS")
  val type: ChronoUnit = DAYS,
)
