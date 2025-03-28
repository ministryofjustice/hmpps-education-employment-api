package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain

import java.time.LocalDateTime
data class Profile(
  var status: ProfileStatus,
  var statusChange: Boolean?,
  var statusChangeDate: LocalDateTime?,
  var prisonName: String?,
  var statusChangeType: StatusChange?,
  var supportDeclined_history: MutableList<SupportDeclined>?,
  var supportAccepted_history: MutableList<SupportAccepted>?,
  var supportDeclined: SupportDeclined?,
  var supportAccepted: SupportAccepted?,
)
