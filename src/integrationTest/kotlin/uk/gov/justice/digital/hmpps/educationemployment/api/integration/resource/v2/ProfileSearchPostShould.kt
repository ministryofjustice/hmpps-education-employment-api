package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileJsonRequest

class ProfileSearchPostShould : ReadinessProfileV2TestCase() {
  private val knownPrisonNumber = ProfileObjects.knownPrisonNumber
  private val anotherPrisonNumber = ProfileObjects.anotherPrisonNumber
  private val unknownPrisonNumber = ProfileObjects.unknownPrisonNumber

  private val allPrisonNumbers = listOf(knownPrisonNumber, anotherPrisonNumber, unknownPrisonNumber)

  @Nested
  inner class GivenNoProfileExist {
    @Test
    fun `return empty list`() {
      val result = assertSearchReadinessProfileIsOk(allPrisonNumbers)

      assertThat(result.body).isEmpty()
    }
  }

  @Nested
  inner class GivenSomeProfilesExist {
    private val prisonNumbers = listOf(knownPrisonNumber, anotherPrisonNumber)
    private lateinit var profileDTOs: List<ReadinessProfileDTO>

    @BeforeEach
    internal fun setUp() {
      profileDTOs = prisonNumbers.map { prisonNumber ->
        val result = assertAddReadinessProfileIsOk(prisonNumber, parseProfileRequestDTO(createProfileJsonRequest))
        assertThat(result.body).isNotNull
        result.body!!
      }.toList()
    }

    @Test
    fun `return existing profiles found`() {
      val result = assertSearchReadinessProfileIsOk(allPrisonNumbers)

      assertThat(result.body).isNotEmpty.hasSize(prisonNumbers.size)
      val profileMap = result.body!!.map { it.offenderId to it }.toMap()
      assertThat(profileMap).containsOnlyKeys(*prisonNumbers.toTypedArray())
      profileDTOs.forEach {
        assertThat(profileMap[it.offenderId]).isEqualTo(it)
      }
    }

    @Test
    fun `return one profile of given prisoner`() {
      val expectedProfile = profileDTOs.first()
      val prisonNumber = expectedProfile.offenderId

      val result = assertSearchReadinessProfileIsOk(prisonNumber)

      assertThat(result.body).isNotEmpty.hasSize(1)
      assertThat(result.body!!.first()).isEqualTo(expectedProfile)
    }

    @Test
    fun `return empty list of unknown prisoner`() {
      val result = assertSearchReadinessProfileIsOk(unknownPrisonNumber)

      assertThat(result.body).isEmpty()
    }
  }
}
