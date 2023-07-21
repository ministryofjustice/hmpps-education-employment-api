package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

data class Action(
  val todoItem: ActionTodo,
  val status: ActionStatus,
  val other: String?,
  val id: List<IDocs>?
)
