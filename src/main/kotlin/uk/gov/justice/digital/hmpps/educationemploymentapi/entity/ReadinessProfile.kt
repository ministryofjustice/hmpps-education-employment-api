package uk.gov.justice.digital.hmpps.educationemploymentapi.entity

import com.fasterxml.jackson.databind.JsonNode
import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.SARReadinessProfileDTO
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.ColumnResult
import javax.persistence.ConstructorResult
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedNativeQuery
import javax.persistence.SqlResultSetMapping
import javax.persistence.Table

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
@Entity
@Table(name = "work_readiness")
@TypeDefs(
  TypeDef(name = "json", typeClass = JsonType::class)
)
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
  @Type(type = "json")
  @Column(columnDefinition = "jsonb")
  var profileData: JsonNode,

  @Type(type = "json")
  @Column(columnDefinition = "jsonb")
  var notesData: JsonNode,

  @Transient
  @Value("false")
  val new: Boolean
)
