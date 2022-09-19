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
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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
