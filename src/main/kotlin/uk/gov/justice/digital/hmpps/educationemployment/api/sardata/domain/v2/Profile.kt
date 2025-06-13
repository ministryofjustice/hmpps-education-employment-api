package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.SupportDeclined
import java.time.LocalDateTime

@Schema(name = "SARProfile", description = "The SAR Readiness Profile")
data class Profile(
  var status: ProfileStatus,
  var statusChange: Boolean?,
  var statusChangeDate: LocalDateTime?,
  var prisonName: String?,
  var statusChangeType: StatusChange?,
  var supportDeclined: SupportDeclined?,
  var supportAccepted: SupportAccepted?,
)

data class ProfileDTO(
  val status: List<ProfileStatus>,
  val statusChange: Boolean?,
  val statusChangeDate: LocalDateTime?,
  val prisonName: String?,
  val statusChangeType: List<StatusChange?>,
  val supportDeclined: SupportDeclined?,
  val supportAccepted: SupportAccepted?,
)

fun Profile.toDto(): ProfileDTO = ProfileDTO(
  status = listOf(this.status),
  statusChange = this.statusChange,
  statusChangeDate = this.statusChangeDate,
  prisonName = this.prisonName,
  statusChangeType = this.statusChangeType?.let { listOf(it) } ?: emptyList(),
  supportDeclined = this.supportDeclined,
  supportAccepted = this.supportAccepted,
)
