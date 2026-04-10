package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.SupportDeclined
import java.time.Instant
import java.time.ZoneId
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile as ProfileEntity

@Schema(name = "SARProfile", description = "The SAR Readiness Profile")
data class Profile(
  var status: ProfileStatus,
  var statusChange: Boolean?,
  var statusChangeDate: Instant?,
  var statusChangeType: StatusChange?,
  var prisonId: String?,
  var prisonName: String?,
  var within12Weeks: Boolean?,
  var supportDeclined: SupportDeclined?,
  var supportAccepted: SupportAccepted?,
) {
  constructor(entity: ProfileEntity, timeZoneId: ZoneId) : this(
    status = entity.status,
    statusChange = entity.statusChange,
    statusChangeDate = entity.statusChangeDate?.instantFromZone(timeZoneId),
    statusChangeType = entity.statusChangeType,
    prisonId = entity.prisonId,
    prisonName = entity.prisonName,
    within12Weeks = entity.within12Weeks,
    supportDeclined = entity.supportDeclined?.let { SupportDeclined(it, timeZoneId) },
    supportAccepted = entity.supportAccepted?.let { SupportAccepted(it, timeZoneId) },
  )
}
