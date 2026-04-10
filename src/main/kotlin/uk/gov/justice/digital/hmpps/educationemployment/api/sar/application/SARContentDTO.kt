package uk.gov.justice.digital.hmpps.educationemployment.api.sar.application

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.AuditedDTO
import java.time.Instant
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2.Profile as SARProfile

data class SARContentDTO(
  @field:Schema(description = "Offender Id", example = "ABC12345")
  val offenderId: String,

  @get:Schema(description = "Author of original profile", example = "user4")
  override val createdBy: String,

  @get:Schema(description = "Created date time", type = "string", format = "date-time", pattern = "yyyy-MM-dd'T'HH:mm:ssZ", example = "2018-12-01T13:45:00Z")
  override val createdDateTime: Instant,

  @get:Schema(description = "Author of last modification", example = "user4")
  override val modifiedBy: String,

  @get:Schema(description = "Last modified date time", type = "string", format = "date-time", pattern = "yyyy-MM-dd'T'HH:mm:ssZ", example = "2018-12-01T13:45:00Z")
  override val modifiedDateTime: Instant,

  @field:Schema(description = "Work readiness profile data")
  val profileData: SARProfile,

  @field:Schema(description = "Work readiness profile notes")
  val notesData: List<Note>,
) : AuditedDTO
