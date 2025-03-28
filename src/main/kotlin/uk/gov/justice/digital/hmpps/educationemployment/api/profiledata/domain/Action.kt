package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

data class Action(
  val todoItem: ActionTodo,
  val status: ActionStatus,
  val other: String?,
  val id: List<IDocs>?,
)
