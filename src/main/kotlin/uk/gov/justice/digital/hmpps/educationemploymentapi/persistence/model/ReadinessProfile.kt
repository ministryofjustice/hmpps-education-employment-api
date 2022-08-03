package uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.r2dbc.postgresql.codec.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.CapturedSpringMapperConfiguration
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile
import java.time.LocalDateTime

@Table("work_readiness")
data class ReadinessProfile(
  @Id
  val offenderId: String,
  val bookingId: Int,
  val createdDateTime: LocalDateTime,
  val modifiedDateTime: LocalDateTime,
  val author: String,
  val schemaVersion: String,
  val profileData: Json,
  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true
) : Persistable<String> {

  constructor(offenderId:String, bookingId:Int, author:String, profile:Profile): this(
    offenderId = offenderId,
    bookingId = bookingId,
    createdDateTime = LocalDateTime.now(),
    modifiedDateTime = LocalDateTime.now(),
    author = author,
    schemaVersion = "1.0.0",
    profileData = Json.of(CapturedSpringMapperConfiguration.OBJECT_MAPPER.writeValueAsString(profile))
  )

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ReadinessProfile) return false

    if (id != other.id) return false

    return true
  }
  override fun isNew(): Boolean = new

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun getId(): String = offenderId
}
