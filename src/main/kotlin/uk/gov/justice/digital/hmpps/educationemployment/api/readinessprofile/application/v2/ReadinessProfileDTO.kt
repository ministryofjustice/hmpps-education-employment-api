package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.ProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.AuditedDTO
import java.time.Instant
import java.time.ZoneId

data class ReadinessProfileDTO(
  @field:Schema(description = "Offender Id", example = "ABC12345")
  val offenderId: String,

  @field:Schema(description = "Booking Id", example = "1234567")
  val bookingId: Long,

  @field:Schema(description = "Author of original profile", example = "user4")
  override val createdBy: String,

  override val createdDateTime: Instant,

  @get:Schema(description = "Author of last modification", example = "user4")
  override val modifiedBy: String,

  @get:Schema(description = "Last modified date time", type = "string", format = "date-time", pattern = "yyyy-MM-dd'T'HH:mm:ssZ", example = "2018-12-01T13:45:00Z")
  override val modifiedDateTime: Instant,

  @field:Schema(description = "Version of the JSON schema", example = "2.0")
  val schemaVersion: String,

  @field:Schema(description = "Work readiness profile JSON data")
  val profileData: ProfileDTO,
) : AuditedDTO {
  constructor(profileEntity: ReadinessProfile, profileData: Profile, timeZoneId: ZoneId) : this(
    offenderId = profileEntity.offenderId,
    bookingId = profileEntity.bookingId,
    createdBy = profileEntity.createdBy,
    createdDateTime = profileEntity.createdDateTime.instantFromZone(timeZoneId),
    modifiedBy = profileEntity.modifiedBy,
    modifiedDateTime = profileEntity.modifiedDateTime.instantFromZone(timeZoneId),
    schemaVersion = profileEntity.schemaVersion,
    profileData = ProfileDTO(profileData, timeZoneId),
  )
}
