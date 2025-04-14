package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2

import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.SupportDeclined
import java.time.LocalDateTime

data class Profile(
  var status: ProfileStatus,
  var statusChange: Boolean?,
  var statusChangeDate: LocalDateTime?,
  var prisonId: String?,
  var prisonName: String?,
  var within12Weeks: Boolean?,
  var statusChangeType: StatusChange?,
  var supportDeclined: SupportDeclined?,
  var supportAccepted: SupportAccepted?,
)
