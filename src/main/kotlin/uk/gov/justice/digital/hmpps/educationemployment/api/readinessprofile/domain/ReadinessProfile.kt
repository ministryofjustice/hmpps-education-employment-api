package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain

import com.fasterxml.jackson.databind.JsonNode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.envers.Audited
import org.hibernate.type.SqlTypes
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Audited
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "work_readiness")
data class ReadinessProfile(
  @Id
  val offenderId: String,

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

  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  var profileData: JsonNode,

  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  var notesData: JsonNode,

  @Transient
  @param:Value("false")
  val new: Boolean,
)
