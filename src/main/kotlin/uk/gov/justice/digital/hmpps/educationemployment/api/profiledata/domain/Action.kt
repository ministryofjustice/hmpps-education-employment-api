package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

data class Action(
  val todoItem: ActionTodo,
  val status: ActionStatus,
  val other: String?,
  val id: List<IDocs>?,
)
// {
//  @get:JsonIgnore
//  val getTodoItem: ActionTodo
//    get() = todoItem
//
//  @get:JsonIgnore
//  val getStatus: ActionStatus
//    get() = status
//
//  @get:JsonProperty("todoItem")
//  val todoItemAsList: List<ActionTodo>
//    get() = listOf(todoItem)
//
//  @get:JsonProperty("status")
//  val statusAsList: List<ActionStatus>
//    get() = listOf(status)
// }
