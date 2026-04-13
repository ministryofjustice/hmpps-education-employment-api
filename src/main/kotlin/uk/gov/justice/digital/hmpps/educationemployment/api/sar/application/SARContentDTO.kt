package uk.gov.justice.digital.hmpps.educationemployment.api.sar.application

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.Note
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2.Profile as SARProfile

data class SARContentDTO(
  @field:Schema(description = "Offender Id", example = "ABC12345")
  val offenderId: String,

  @field:Schema(description = "Author of original profile", example = "user4")
  val createdBy: String,

  @field:Schema(description = "Created date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val createdDateTime: LocalDateTime,

  @field:Schema(description = "Author of last modification", example = "user4")
  val modifiedBy: String,

  @field:Schema(description = "Last modified date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val modifiedDateTime: LocalDateTime,

  @field:Schema(description = "Work readiness profile data")
  val profileData: SARProfile,

  @field:Schema(description = "Work readiness profile notes")
  val notesData: List<Note>,
)
