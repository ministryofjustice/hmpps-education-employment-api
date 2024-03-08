package uk.gov.justice.digital.hmpps.educationemploymentapi.data

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile

data class ReadinessProfileRequestDTO(

  @Schema(description = "Booking Id", example = "12345678")
  val bookingId: Long,

  @Schema(description = "Work readiness profile JSON data", example = "{...}")
  @Valid
  val profileData: Profile,
)
