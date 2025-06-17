package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo

abstract class ActionMixin {
  @JsonIgnore
  abstract fun getTodoItem(): ActionTodo

  @JsonIgnore
  abstract fun getStatus(): ActionStatus

  @JsonProperty("todoItem")
  abstract fun getTodoItemAsList(): List<ActionTodo>

  @JsonProperty("status")
  abstract fun getStatusAsList(): List<ActionStatus>
}
