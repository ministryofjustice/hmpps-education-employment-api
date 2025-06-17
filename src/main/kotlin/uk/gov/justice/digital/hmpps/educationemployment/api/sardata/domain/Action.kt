package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.IDocs

data class Action(
  @get:JsonIgnore
  val todoItem: ActionTodo,

  @get:JsonIgnore
  val status: ActionStatus,

  val other: String,
  val id: List<IDocs>,
) {
  @get:JsonProperty("todoItem")
  val todoItemAsList: List<ActionTodo>
    get() = listOf(todoItem)

  @get:JsonProperty("status")
  val statusAsList: List<ActionStatus>
    get() = listOf(status)
}
