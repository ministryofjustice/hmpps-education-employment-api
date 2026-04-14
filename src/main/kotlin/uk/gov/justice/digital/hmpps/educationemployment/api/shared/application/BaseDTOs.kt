package uk.gov.justice.digital.hmpps.educationemployment.api.shared.application

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.ZoneId

interface ModificationAuditable {
  @get:Schema(description = "Author of modification", example = "user4")
  var modifiedBy: String?

  @get:Schema(description = "Modified date and time", type = "string", format = "date-time", pattern = "yyyy-MM-dd'T'HH:mm:ssZ", example = "2018-12-01T13:45:00Z")
  var modifiedDateTime: Instant?
}

interface ModificationAudited {
  @get:Schema(description = "Author of modification", example = "user4")
  val modifiedBy: String

  @get:Schema(description = "Modified date and time", type = "string", format = "date-time", pattern = "yyyy-MM-dd'T'HH:mm:ssZ", example = "2018-12-01T13:45:00Z")
  val modifiedDateTime: Instant
}

interface CreationAudited {
  @get:Schema(description = "Author of creation", example = "user4")
  val createdBy: String?

  @get:Schema(description = "Creation date and time", type = "string", format = "date-time", pattern = "yyyy-MM-dd'T'HH:mm:ssZ", example = "2018-12-01T13:45:00Z")
  val createdDateTime: Instant?
}

interface EntityConvertible<E> {
  /**
   * DTO to domain entity, with local timestamp in given time zone
   */
  fun entity(timeZoneId: ZoneId): E
}

interface AuditedDTO :
  CreationAudited,
  ModificationAudited
