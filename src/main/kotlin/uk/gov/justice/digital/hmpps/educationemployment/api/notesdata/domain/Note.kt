package uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain

import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import java.time.LocalDateTime

data class Note(
  val createdBy: String,
  val createdDateTime: LocalDateTime,

  val attribute: ActionTodo,
  val text: String,
)
