package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.CreationAudited
import java.time.Instant
import java.time.ZoneId

data class NoteDTO(
  override val createdBy: String,
  override val createdDateTime: Instant,

  @field:Schema(description = "The attribute the note relates to", example = "DISCLOSURE_LETTER")
  val attribute: String,

  @field:Schema(description = "The notes text", example = "Will call manager to arrange this")
  val text: String,
) : CreationAudited {
  constructor(note: Note, timeZoneId: ZoneId) : this(
    createdBy = note.createdBy,
    createdDateTime = note.createdDateTime.instantFromZone(timeZoneId),
    attribute = note.attribute.name,
    text = note.text,
  )
}
