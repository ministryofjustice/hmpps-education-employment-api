package uk.gov.justice.digital.hmpps.educationemploymentapi.data

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile

data class CreateReadinessProfileRequestDTO(
  @Schema(description = "Offender Id", example = "ABC12345")
  val offenderId: String,

  @Schema(description = "Booking Id", example = "ABC123")
  val bookingId: Int,

  @Schema(description = "Work readiness profile JSON data", example = "{...}")
  val profileData: Profile
)
