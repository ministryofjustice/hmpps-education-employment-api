package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
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
  var prisonId: String?,
  var prisonName: String?,
  var within12Weeks: Boolean?,
  var statusChangeType: StatusChange?,
  var supportDeclined: SupportDeclined?,
  var supportAccepted: SupportAccepted?,
) {
  @get:JsonIgnore
  val getStatus: ProfileStatus
    get() = status

  @get:JsonIgnore
  val getStatusChangeType: StatusChange?
    get() = statusChangeType

  @get:JsonProperty("status")
  val statusAsList: List<ProfileStatus>
    get() = listOf(status)

  @get:JsonProperty("statusChangeType")
  val statusChangeAsList: List<StatusChange>
    get() = statusChangeType?.let { listOf(it) } ?: emptyList()
}
