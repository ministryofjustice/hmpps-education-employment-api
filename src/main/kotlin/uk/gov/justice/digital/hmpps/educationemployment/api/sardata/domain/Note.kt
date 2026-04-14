package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.CreationAudited
import java.time.Instant
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note as NoteEntity

@Schema(name = "SARNote", description = "The note of work readiness profile for subject access request (SAR)")
data class Note(
  @get:Schema(description = "userId of the note creator", example = "user4")
  override val createdBy: String,

  @get:Schema(description = "Created date time", type = "string", pattern = "yyyy-MM-dd'T'HH:mm:ssZ", example = "2018-12-01T13:45:00Z")
  override val createdDateTime: Instant,

  @field:Schema(description = "The attribute the note relates to", example = "DISCLOSURE_LETTER")
  val attribute: ActionTodo,

  @field:Schema(description = "The notes text", example = "Will call manager to arrange this")
  val text: String,
) : CreationAudited {
  constructor(note: NoteEntity, timeZoneId: ZoneId) : this(
    createdBy = note.createdBy,
    createdDateTime = note.createdDateTime.instantFromZone(timeZoneId),
    attribute = note.attribute,
    text = note.text,
  )
}
