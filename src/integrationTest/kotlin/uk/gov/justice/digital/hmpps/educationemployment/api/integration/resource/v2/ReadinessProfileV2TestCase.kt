@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.helpers.ProfileV1Helper
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.helpers.ProfileV2Helper
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.READINESS_PROFILE_ENDPOINT
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.ReadinessProfileTestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.StatusChangeUpdateRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.profilesFromPrison1
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.profilesFromPrison2
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.requestDto
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile as ProfileV1
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1.ReadinessProfileRequestDTO as ReadinessProfileV1RequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO as ReadinessProfileV2RequestDTO

abstract class ReadinessProfileV2TestCase :
  ReadinessProfileTestCase<ReadinessProfileDTO, ReadinessProfileRequestDTO>(
    READINESS_PROFILE_ENDPOINT,
    ReadinessProfileDTO::class.java,
    object : TypeReference<ReadinessProfileRequestDTO>() {},
    object : ParameterizedTypeReference<List<ReadinessProfileDTO>>() {},
  ) {
  @Autowired
  protected lateinit var profileV1Helper: ProfileV1Helper

  @Autowired
  protected lateinit var profileV2Helper: ProfileV2Helper

  private val typeRefProfile by lazy { object : TypeReference<Profile>() {} }
  private val typeRefProfileV1 by lazy { object : TypeReference<ProfileV1>() {} }
  private val typeRefRequestV1 by lazy { object : TypeReference<ReadinessProfileV1RequestDTO>() {} }
  private val typeRefRequestV2 by lazy { object : TypeReference<ReadinessProfileRequestDTO>() {} }

  protected val expectedVersion = "2.0"

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

  protected fun addProfileV2(prisonNumber: String, request: ReadinessProfileV2RequestDTO) = profileV2Helper.addReadinessProfileForTest(
    userId = authUser,
    offenderId = prisonNumber,
    bookingId = request.bookingId,
    profile = request.profileData,
  )

  protected fun getProfileForOffenderFilterByPeriodV2(prisonNumber: String, fromDate: LocalDate, toDate: LocalDate) = profileV2Helper.getProfileForOffenderFilterByPeriodForTest(
    offenderId = prisonNumber,
    fromDate = fromDate,
    toDate = toDate,
  )

  protected fun updateProfileV2(prisonNumber: String, request: ReadinessProfileV2RequestDTO) = profileV2Helper.updateReadinessProfileForTest(
    userId = authUser,
    offenderId = prisonNumber,
    bookingId = request.bookingId,
    profile = request.profileData,
  )

  protected fun parseProfileV1(profileData: JsonNode): ProfileV1 = objectMapper.treeToValue(profileData, typeRefProfileV1)
  protected fun parseProfileV1RequestDTO(profileJson: String) = objectMapper.readValue(profileJson, typeRefRequestV1)

  protected fun Profile.statusChangeRequestToAccepted(newStatus: ProfileStatus = ProfileStatus.SUPPORT_NEEDED) = StatusChangeUpdateRequestDTO(supportAccepted, supportDeclined, newStatus)
  protected fun Profile.statusChangeRequestToDeclined() = StatusChangeUpdateRequestDTO(supportAccepted, supportDeclined, ProfileStatus.SUPPORT_DECLINED)

  protected fun givenMoreProfilesFromMultiplePrisons() = (profilesFromPrison2 + profilesFromPrison1).toTypedArray().let { givenProfilesAreCreated(*it) }

  protected fun givenProfilesAreCreated(vararg profiles: ReadinessProfile) = profiles.map {
    assertAddReadinessProfileIsOk(it.offenderId, it.requestDto).body
  }.filterNotNull()
}
