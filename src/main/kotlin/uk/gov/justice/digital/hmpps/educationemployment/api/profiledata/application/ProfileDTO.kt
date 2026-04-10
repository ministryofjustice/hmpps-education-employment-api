package uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.EntityConvertible
import java.time.Instant
import java.time.ZoneId

data class ProfileDTO(
  @get:Schema(description = "Status of the profile")
  var status: ProfileStatus,
  @get:Schema(description = "Indicator of any status change")
  var statusChange: Boolean?,
  @get:Schema(description = "Date time of status change", type = "string", format = "date-time", pattern = "yyyy-MM-dd'T'HH:mm:ssZ", example = "2018-12-01T13:45:00Z")
  var statusChangeDate: Instant?,
  @get:Schema(description = "Type of status change")
  var statusChangeType: StatusChange?,
  @get:Schema(description = "Prison ID of the prisoner at last update")
  var prisonId: String?,
  var prisonName: String?,
  @get:Schema(description = "Indicator if prisoner was within 12 weeks from release at last update")
  var within12Weeks: Boolean?,
  @get:Schema(description = "Data set for prisoner does/did not want support")
  var supportDeclined: SupportDeclinedDTO?,
  @get:Schema(description = "Data set for prisoner needs/needed support")
  var supportAccepted: SupportAcceptedDTO?,
) : EntityConvertible<Profile> {
  constructor(entity: Profile, timeZoneId: ZoneId) : this(
    status = entity.status,
    statusChange = entity.statusChange,
    statusChangeDate = entity.statusChangeDate?.instantFromZone(timeZoneId),
    statusChangeType = entity.statusChangeType,
    prisonId = entity.prisonId,
    prisonName = entity.prisonName,
    within12Weeks = entity.within12Weeks,
    supportDeclined = entity.supportDeclined?.let { SupportDeclinedDTO(it, timeZoneId) },
    supportAccepted = entity.supportAccepted?.let { SupportAcceptedDTO(it, timeZoneId) },
  )

  override fun entity(timeZoneId: ZoneId) = Profile(
    status = status,
    statusChange = statusChange,
    statusChangeDate = statusChangeDate?.localDateTimeAtZone(timeZoneId),
    statusChangeType = statusChangeType,
    prisonId = prisonId,
    prisonName = prisonName,
    within12Weeks = within12Weeks,
    supportDeclined = supportDeclined?.entity(timeZoneId),
    supportAccepted = supportAccepted?.entity(timeZoneId),
  )
}
