package uk.gov.justice.digital.hmpps.educationemployment.api.data

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.entity.ReadinessProfile
import java.time.LocalDateTime

data class SARReadinessProfileDTO(
  @Schema(description = "Offender Id", example = "ABC12345")
  val offenderId: String,

  @Schema(description = "Booking Id", example = "1234567")
  val bookingId: Long,

  @Schema(description = "Author of original profile", example = "whilesp")
  val createdBy: String,

  @Schema(description = "Created date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val createdDateTime: LocalDateTime,

  @Schema(description = "Author of last modification", example = "whilesp")
  val modifiedBy: String,

  @Schema(description = "Last modified date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val modifiedDateTime: LocalDateTime,

  @Schema(description = "Version of the JSON schema", example = "1.1.1")
  val schemaVersion: String,

  @Schema(description = "Work readiness profile JSON data", example = "{...}")
  val profileData: Profile,

  @Schema(description = "Work readiness profile JSON data", example = "{...}")
  val noteData: Note?,
) {
  constructor(profileEntity: ReadinessProfile) : this(
    offenderId = profileEntity.offenderId,
    bookingId = profileEntity.bookingId,
    createdBy = profileEntity.createdBy,
    createdDateTime = profileEntity.createdDateTime,
    modifiedBy = profileEntity.modifiedBy,
    modifiedDateTime = profileEntity.modifiedDateTime,
    schemaVersion = profileEntity.schemaVersion,
    profileData = CapturedSpringConfigValues.objectMapper.readValue(CapturedSpringConfigValues.objectMapper.writeValueAsString(profileEntity.profileData), Profile::class.java),
    noteData = if (profileEntity.notesData.isEmpty) {
      null
    } else {
      CapturedSpringConfigValues.objectMapper.readValue(CapturedSpringConfigValues.objectMapper.writeValueAsString(profileEntity.notesData), Note::class.java)
    },
  )
}
