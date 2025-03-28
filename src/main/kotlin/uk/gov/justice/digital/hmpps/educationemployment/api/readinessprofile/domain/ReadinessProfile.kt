package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain

import com.fasterxml.jackson.databind.JsonNode
import jakarta.persistence.Column
import jakarta.persistence.ColumnResult
import jakarta.persistence.ConstructorResult
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.NamedNativeQuery
import jakarta.persistence.SqlResultSetMapping
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
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.SARReadinessProfileDTO
import java.time.LocalDateTime

@NamedNativeQuery(
  name = "ReadinessProfile.findInductionsInATimePeriod_Named",
  query = "SELECT r.offender_id AS offenderId,c.booking_id AS bookingId, c.created_by AS createdBy, c.created_date_time AS  createdDateTime,c.modified_by AS modifiedBy,c.modified_date_time AS modifiedDateTime," +
    "c.schema_version AS schemaVersion,c.profile_data AS profileData ,c.notes_data AS notesData, FROM ReadinessProfile r where  r.offender_id = :offenderId and r.created_date_time >= :fromDate and r.created_date_time<= :endDate",
  resultSetMapping = "Mapping.SARReadinessProfileDTO",
)
@SqlResultSetMapping(
  name = "Mapping.SARReadinessProfileDTO",
  classes = [
    ConstructorResult(
      targetClass = SARReadinessProfileDTO::class,
      columns = arrayOf(
        ColumnResult(name = "offenderId"),
        ColumnResult(name = "bookingId"),
        ColumnResult(name = "createdBy"),
        ColumnResult(name = "createdDateTime", type = LocalDateTime::class),
        ColumnResult(name = "modifiedDateTime", type = LocalDateTime::class),
        ColumnResult(name = "modifiedBy"),
        ColumnResult(name = "schemaVersion"),
        ColumnResult(name = "profileData"),
        ColumnResult(name = "notesData"),
      ),
    ),
  ],
)
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
  @Value("false")
  val new: Boolean,
)
