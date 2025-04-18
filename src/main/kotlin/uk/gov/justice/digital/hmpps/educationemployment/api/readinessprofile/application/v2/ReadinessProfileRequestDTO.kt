package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile

data class ReadinessProfileRequestDTO(

  @Schema(description = "Booking Id", example = "12345678")
  val bookingId: Long,

  @Schema(description = "Work readiness profile JSON data", example = "{...}")
  @Valid
  val profileData: Profile,
)
