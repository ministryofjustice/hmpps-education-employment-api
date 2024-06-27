package uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile

data class Action(
  val todoItem: ActionTodo,
  val status: ActionStatus,
  val other: String?,
  val id: List<IDocs>?,
)
