package uk.gov.justice.digital.hmpps.educationemployment.api.sardata

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
class PrisonSubjectAccessRequestService(
  private val profileService: ProfileV2Service,
) : HmppsPrisonSubjectAccessRequestService {

  override fun getPrisonContentFor(
    prn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? = try {
    profileService.getProfileForOffenderFilterByPeriod(prn, fromDate, toDate).ifEmpty { null }?.let { data ->
      HmppsSubjectAccessRequestContent(data)
    }
  } catch (_: NotFoundException) {
    null
  }
}
