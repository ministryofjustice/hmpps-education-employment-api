package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.IDocs

data class Action(
  val todoItem: List<ActionTodo>,
  val status: List<ActionStatus>,
  val other: String?,
  val id: List<IDocs>?,
)
