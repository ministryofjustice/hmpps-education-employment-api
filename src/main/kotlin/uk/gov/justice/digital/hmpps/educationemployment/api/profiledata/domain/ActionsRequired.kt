package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

import java.time.LocalDateTime

data class ActionsRequired(
  val modifiedBy: String,
  val modifiedDateTime: LocalDateTime,

  val actions: List<Action>,
)
