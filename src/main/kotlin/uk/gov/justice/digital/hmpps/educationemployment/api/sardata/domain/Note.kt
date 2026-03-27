package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import java.time.LocalDateTime

data class Note(
  @field:Schema(description = "userId of the note creator", example = "user4")
  val createdBy: String,

  @field:Schema(description = "Created date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val createdDateTime: LocalDateTime,

  @field:Schema(description = "The attribute the note relates to", example = "DISCLOSURE_LETTER")
  val attribute: ActionTodo,

  @field:Schema(description = "The notes text", example = "Will call manager to arrange this")
  val text: String,
)
