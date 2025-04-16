@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.helpers.ProfileV1Helper
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.ReadinessProfileTestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.StatusChangeUpdateRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile as ProfileV1
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1.ReadinessProfileRequestDTO as ReadinessProfileV1RequestDTO

class ReadinessProfileV2TestCase :
  ReadinessProfileTestCase<ReadinessProfileDTO, ReadinessProfileRequestDTO>(
    READINESS_PROFILE_V2_ENDPOINT,
    ReadinessProfileDTO::class.java,
    object : TypeReference<ReadinessProfileRequestDTO>() {},
    object : ParameterizedTypeReference<List<ReadinessProfileDTO>>() {},
  ) {
  @Autowired
  protected lateinit var profileV1Helper: ProfileV1Helper

  private val typeRefProfile by lazy { object : TypeReference<Profile>() {} }
  private val typeRefProfileV1 by lazy { object : TypeReference<ProfileV1>() {} }

  protected fun parseProfile(profileData: JsonNode): Profile = objectMapper.treeToValue(profileData, typeRefProfile)

  protected fun addProfileV1(prisonNumber: String, request: ReadinessProfileV1RequestDTO) = profileV1Helper.addReadinessProfileForTest(
    userId = authUser,
    offenderId = prisonNumber,
    bookingId = request.bookingId,
    profile = request.profileData,
  )

  protected fun updateProfileV1(prisonNumber: String, request: ReadinessProfileV1RequestDTO) = profileV1Helper.updateReadinessProfileForTest(
    userId = authUser,
    offenderId = prisonNumber,
    bookingId = request.bookingId,
    profile = request.profileData,
  )

  protected fun parseProfileV1(profileData: JsonNode): ProfileV1 = objectMapper.treeToValue(profileData, typeRefProfileV1)

  protected fun Profile.statusChangeRequestToAccepted(newStatus: ProfileStatus = ProfileStatus.SUPPORT_NEEDED) = StatusChangeUpdateRequestDTO(supportAccepted, supportDeclined, newStatus)
  protected fun Profile.statusChangeRequestToDeclined() = StatusChangeUpdateRequestDTO(supportAccepted, supportDeclined, ProfileStatus.SUPPORT_DECLINED)
}

private const val READINESS_PROFILE_V2_ENDPOINT = "/v2/readiness-profiles"
