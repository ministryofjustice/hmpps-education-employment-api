package uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile

import java.time.LocalDateTime

data class Note(
  val createdBy: String,
  val createdDateTime: LocalDateTime,

  val attribute: ActionTodo,
  val text: String,
)
