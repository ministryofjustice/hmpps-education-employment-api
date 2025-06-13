package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo

interface ActionMixin {
  @get:JsonProperty("todoItem")
  var todoItemAsList: List<ActionTodo>

  @get:JsonProperty("status")
  var statusAsList: List<ActionStatus>
}
