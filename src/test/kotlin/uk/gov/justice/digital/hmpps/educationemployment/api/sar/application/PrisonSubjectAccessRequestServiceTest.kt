package uk.gov.justice.digital.hmpps.educationemployment.api.sar.application

import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2.Profile
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2.Profile as SARProfile

@ExtendWith(MockitoExtension::class)
class PrisonSubjectAccessRequestServiceTest {

  private val objectMapper = CapturedSpringConfigValues.configObjectMapper()

  @Mock
  private lateinit var profileService: ProfileV2Service

  private val sarService by lazy { PrisonSubjectAccessRequestService(profileService, objectMapper) }

  @AfterEach
  fun afterEach() {
    Mockito.reset(profileService)
  }

  @Test
  fun `when we have prisoner data return it`() {
    val currentTime = LocalDateTime.now()
    val sarContent = SARContentDTO(
      offenderId = "A1234BC",
      createdDateTime = currentTime,
      modifiedDateTime = currentTime,
      profileData = SARProfile(
        status = ProfileStatus.NO_RIGHT_TO_WORK,
        statusChange = false,
        statusChangeDate = null,
        statusChangeType = StatusChange.NEW,
        prisonId = "MDI",
        prisonName = null,
        within12Weeks = true,
        supportDeclined = null,
        supportAccepted = null,
      ),
    )
    val userId = "TESTER"
    val profileData: JsonNode = Profile(
      status = ProfileStatus.NO_RIGHT_TO_WORK,
      statusChange = false,
      statusChangeDate = null,
      statusChangeType = StatusChange.NEW,
      prisonId = "MDI",
      prisonName = null,
      within12Weeks = true,
      supportDeclined = null,
      supportAccepted = null,
    ).let { objectMapper.valueToTree(it) }
    val notesData = objectMapper.readTree("[]")
    val readinessProfile = ReadinessProfile(
      offenderId = "A1234BC",
      bookingId = 111111,
      createdBy = userId,
      createdDateTime = currentTime,
      modifiedBy = userId,
      modifiedDateTime = currentTime,
      schemaVersion = "2.0",
      profileData = profileData,
      notesData = notesData,
      new = false,
    )

    whenever(profileService.getProfilesForOffenderFilterByPeriod(ArgumentMatchers.anyString(), isNull(), isNull())).thenReturn(listOf(readinessProfile))

    val result = sarService.getPrisonContentFor("prn", null, null)

    Assertions.assertThat(result).isEqualTo(HmppsSubjectAccessRequestContent(listOf(sarContent)))
  }

  @Test
  fun `when there is no prisoner data return null`() {
    whenever(profileService.getProfilesForOffenderFilterByPeriod(ArgumentMatchers.anyString(), isNull(), isNull())).thenReturn(emptyList())

    val result = sarService.getPrisonContentFor("prn", null, null)

    Assertions.assertThat(result).isNull()
  }

  @Test
  fun `when a NotFoundException is thrown return null`() {
    whenever(profileService.getProfilesForOffenderFilterByPeriod(anyString(), isNull(), isNull())).thenAnswer { throw NotFoundException("prn") }

    val result = sarService.getPrisonContentFor("prn", null, null)

    Assertions.assertThat(result).isNull()
  }
}
