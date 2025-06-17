package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import java.time.LocalDateTime

@Schema(name = "SARProfile", description = "The SAR Readiness Profile")
data class Profile(
  var status: ProfileStatus,
  var statusChange: Boolean?,
  var statusChangeDate: LocalDateTime?,
  var statusChangeType: StatusChange?,
  var prisonId: String?,
  var prisonName: String?,
  var within12Weeks: Boolean?,
  var supportDeclined: SupportDeclined?,
  var supportAccepted: SupportAccepted?,
)
