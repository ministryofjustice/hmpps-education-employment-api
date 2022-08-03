package uk.gov.justice.digital.hmpps.educationemploymentapi.data

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.CapturedSpringMapperConfiguration
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model.ReadinessProfile
import java.time.LocalDateTime

data class ReadinessProfileDTO(
  @Schema(description = "Offender Id", example = "ABC12345")
  val offenderId: String,

  @Schema(description = "Booking Id", example = "ABC123")
  val bookingId: Int,

  @Schema(description = "Created date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val modifiedDateTime: LocalDateTime,

  @Schema(description = "Author of last modification", example = "whilesp")
  val author: String,

  @Schema(description = "Version of the JSON schema", example = "1.1.1")
  val schemaVersion: String,

  @Schema(description = "Work readiness profile JSON data", example = "{...}")
  val profileData: Profile)
  {
    constructor(profileEntity:ReadinessProfile) : this(
      offenderId = profileEntity.offenderId,
      bookingId = profileEntity.bookingId,
      modifiedDateTime = profileEntity.modifiedDateTime,
      author = profileEntity.author,
      schemaVersion = profileEntity.schemaVersion,
      profileData = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(profileEntity.profileData.asString(), Profile::class.java)
    )
}
