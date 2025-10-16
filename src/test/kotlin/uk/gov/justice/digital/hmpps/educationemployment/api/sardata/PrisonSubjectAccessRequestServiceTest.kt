package uk.gov.justice.digital.hmpps.educationemployment.api.sardata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues.Companion.objectMapper
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.SARContentDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PrisonSubjectAccessRequestServiceTest {

  @Mock
  private lateinit var profileService: ProfileV2Service

  @InjectMocks
  private lateinit var sarService: PrisonSubjectAccessRequestService

  @AfterEach
  fun afterEach() {
    Mockito.reset(profileService)
  }

  @Test
  fun `when we have prisoner data return it`() {
    val sarContent = SARContentDTO(
      offenderId = "offenderId",
      createdDateTime = LocalDateTime.now(),
      modifiedDateTime = LocalDateTime.now(),
      profileData = objectMapper.readTree("{}"),
    )

    whenever(profileService.getProfileForOffenderFilterByPeriod(anyString(), isNull(), isNull())).thenReturn(listOf(sarContent))

    val result = sarService.getPrisonContentFor("prn", null, null)

    assertThat(result).isEqualTo(HmppsSubjectAccessRequestContent(listOf(sarContent)))
  }

  @Test
  fun `when there is no prisoner data return null`() {
    whenever(profileService.getProfileForOffenderFilterByPeriod(anyString(), isNull(), isNull())).thenReturn(emptyList())

    val result = sarService.getPrisonContentFor("prn", null, null)

    assertThat(result).isNull()
  }

  @Test
  fun `when a NotFoundException is thrown return null`() {
    whenever(profileService.getProfileForOffenderFilterByPeriod(anyString(), isNull(), isNull())).thenAnswer { throw NotFoundException("prn") }

    val result = sarService.getPrisonContentFor("prn", null, null)

    assertThat(result).isNull()
  }
}
