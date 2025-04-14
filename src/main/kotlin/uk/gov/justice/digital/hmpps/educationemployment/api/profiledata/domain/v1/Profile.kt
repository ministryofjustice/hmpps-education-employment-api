package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1

import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined
import java.time.LocalDateTime

@Deprecated(
  message = "Use v2 instead",
  replaceWith = ReplaceWith("Profile", "uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2"),
)
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
