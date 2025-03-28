package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import io.swagger.v3.oas.annotations.media.Schema

data class NoteRequestDTO(
  @Schema(description = "The notes text", example = "Will call manager to arrange this")
  val text: String,
)
