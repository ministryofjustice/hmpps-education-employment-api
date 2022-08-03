package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

import java.time.LocalDateTime

data class Note(
  val author:String,
  val createdDateTime: LocalDateTime,

  val attribute:ActionTodo,
  val text:String
)
