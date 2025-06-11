package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.IDocs

data class Action(
  var todoItem: ActionTodo,
  var status: ActionStatus,
  var other: String?,
  var id: List<IDocs>?,
) {
  val todoItemAsList: List<ActionTodo>
    get() = listOf(todoItem)

  val statusAsList: List<ActionStatus>
    get() = listOf(status)
}
