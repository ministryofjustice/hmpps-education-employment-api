package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

import io.swagger.v3.oas.annotations.media.Schema

data class Action(
  @get:Schema(description = "To do / In place already")
  val todoItem: ActionTodo,
  val status: ActionStatus,
  @get:Schema(description = "Other type of ID")
  val other: String?,
  @get:Schema(description = "Types of ID")
  val id: List<IDocs>?,
)
