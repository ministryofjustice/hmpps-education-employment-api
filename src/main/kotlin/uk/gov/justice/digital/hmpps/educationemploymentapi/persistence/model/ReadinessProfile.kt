package uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model

import io.r2dbc.postgresql.codec.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.CapturedSpringMapperConfiguration
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile
import java.time.LocalDateTime

@Table("work_readiness")
class ReadinessProfile(
  @Id
  var offenderId: String,

  var bookingId: Long,

  @CreatedBy
  var createdBy: String,

  @CreatedDate
  var createdDateTime: LocalDateTime,

  @LastModifiedBy
  var modifiedBy: String,

  @LastModifiedDate
  var modifiedDateTime: LocalDateTime,

  var schemaVersion: String,

  var profileData: Json,

  var notesData: Json,

  @Transient
  @Value("false")
  val new: Boolean
) : Persistable<String> {

  constructor(userId: String, offenderId: String, bookingId: Long, profile: Profile, isNew: Boolean) : this(
    offenderId = offenderId,
    bookingId = bookingId,
    createdBy = userId,
    createdDateTime = LocalDateTime.now(),
    modifiedBy = userId,
    modifiedDateTime = LocalDateTime.now(),
    schemaVersion = "1.0.0",
    profileData = Json.of(CapturedSpringMapperConfiguration.OBJECT_MAPPER.writeValueAsString(profile)),
    notesData = Json.of("[]"),
    new = isNew
  )

  override fun isNew(): Boolean = new

  override fun getId(): String? = offenderId
}
