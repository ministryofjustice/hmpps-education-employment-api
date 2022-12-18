package uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile

import java.time.LocalDateTime

data class Profile(
  var status: ProfileStatus,
  var statusChange: Boolean?,
  var statusChangeDate: LocalDateTime?,
  var statusChangeType: StatusChange?,
  var supportDeclined: MutableList<SupportDeclined>?,
  var supportAccepted: MutableList<SupportAccepted>?,
  var currentSupportState: CurrentSupportState
)
