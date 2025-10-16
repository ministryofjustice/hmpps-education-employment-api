package uk.gov.justice.digital.hmpps.educationemployment.api.sardata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
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

  @Test
  fun `when we have prisoner data return it`() {
    val sarContent = SARContentDTO(
      offenderId = "offenderId",
      createdDateTime = LocalDateTime.now(),
      modifiedDateTime = LocalDateTime.now(),
      profileData = objectMapper.readTree("{}"),
    )

    lenient().whenever(profileService.getProfileForOffenderFilterByPeriod(any(), any(), any())).thenReturn(listOf(sarContent))

    val result = sarService.getPrisonContentFor("prn", null, null)

    assertThat(result).isEqualTo(HmppsSubjectAccessRequestContent(listOf(sarContent)))
  }

  @Test
  fun `when there is no prisoner data return null`() {
    lenient().whenever(profileService.getProfileForOffenderFilterByPeriod(any(), any(), any())).thenReturn(emptyList())

    val result = sarService.getPrisonContentFor("prn", null, null)

    assertThat(result).isNull()
  }

  @Test
  fun `when a NotFoundException is thrown return null`() {
    lenient().whenever(profileService.getProfileForOffenderFilterByPeriod(any(), any(), any())).thenThrow(NotFoundException("prn3"))

    val result = sarService.getPrisonContentFor("prn", null, null)

    assertThat(result).isNull()
  }
}
