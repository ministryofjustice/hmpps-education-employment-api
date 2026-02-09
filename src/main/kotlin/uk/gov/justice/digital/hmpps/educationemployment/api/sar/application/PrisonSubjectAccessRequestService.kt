package uk.gov.justice.digital.hmpps.educationemployment.api.sar.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2.Profile
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
class PrisonSubjectAccessRequestService(
  private val profileService: ProfileV2Service,
  private val objectMapper: ObjectMapper,
) : HmppsPrisonSubjectAccessRequestService {
  private val typeRefSARProfile by lazy { object : TypeReference<Profile>() {} }

  override fun getPrisonContentFor(
    prn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? = try {
    profileService.getProfilesForOffenderFilterByPeriod(prn, fromDate, toDate)
      .map {
        SARContentDTO(
          offenderId = it.offenderId,
          createdDateTime = it.createdDateTime,
          modifiedDateTime = it.modifiedDateTime,
          profileData = objectMapper.treeToValue(it.profileData, typeRefSARProfile),
        )
      }
      .ifEmpty { null }?.let { it -> HmppsSubjectAccessRequestContent(it) }
  } catch (_: NotFoundException) {
    null
  }
}
