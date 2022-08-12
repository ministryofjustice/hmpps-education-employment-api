package uk.gov.justice.digital.hmpps.educationemploymentapi.data

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Note
import java.time.LocalDateTime

data class NoteDTO(
  @Schema(description = "userId of the note creator", example = "whilesp")
  val createdBy: String,

  @Schema(description = "Created date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ss", example = "2018-12-01T13:45:00", required = true)
  val createdDateTime: LocalDateTime,

  @Schema(description = "The attribute the note relates to", example = "DISCLOSURE_LETTER")
  val attribute: String,

  @Schema(description = "The notes text", example = "Will call manager to arrange this")
  val text: String
) {
  constructor(note: Note) : this(
    createdBy = note.createdBy,
    createdDateTime = note.createdDateTime,
    attribute = note.attribute.name,
    text = note.text
  )
}
