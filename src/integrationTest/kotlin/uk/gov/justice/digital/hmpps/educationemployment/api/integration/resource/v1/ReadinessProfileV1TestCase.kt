@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v1

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.helpers.ProfileV1Helper
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.READINESS_PROFILE_ENDPOINT
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.ReadinessProfileTestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1.ReadinessProfileRequestDTO

class ReadinessProfileV1TestCase :
  ReadinessProfileTestCase<ReadinessProfileDTO, ReadinessProfileRequestDTO>(
    READINESS_PROFILE_ENDPOINT,
    ReadinessProfileDTO::class.java,
    object : TypeReference<ReadinessProfileRequestDTO>() {},
    object : ParameterizedTypeReference<List<ReadinessProfileDTO>>() {},
  ) {
  @Autowired
  protected lateinit var profileV1Helper: ProfileV1Helper

  private val typeRefProfile by lazy { object : TypeReference<Profile>() {} }

  protected fun addProfile(prisonNumber: String, request: ReadinessProfileRequestDTO) = profileV1Helper.addReadinessProfileForTest(
    userId = authUser,
    offenderId = prisonNumber,
    bookingId = request.bookingId,
    profile = request.profileData,
  )

  protected fun updateProfile(prisonNumber: String, request: ReadinessProfileRequestDTO) = profileV1Helper.updateReadinessProfileForTest(
    userId = authUser,
    offenderId = prisonNumber,
    bookingId = request.bookingId,
    profile = request.profileData,
  )

  protected fun parseProfile(profileData: JsonNode): Profile = objectMapper.treeToValue(profileData, typeRefProfile)
}
