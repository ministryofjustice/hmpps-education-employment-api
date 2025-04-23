package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileJsonRequest
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileV1JsonRequest

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

  @Nested
  inner class GivenProfileOfPreviousVersionExists {
    private val prisonNumber1 = knownPrisonNumber
    private val prisonNumber2 = anotherPrisonNumber
    private val prisonNumbers = listOf(prisonNumber1, prisonNumber2)

    private val profileDTO1 = ProfileObjects.migratedProfile
    private lateinit var profileDTOs: List<ReadinessProfileDTO>

    @BeforeEach
    internal fun setUp() {
      addProfileV1(prisonNumber1, parseProfileV1RequestDTO(createProfileV1JsonRequest))

      val profileDTO2 = assertAddReadinessProfileIsOk(prisonNumber2, parseProfileRequestDTO(createProfileJsonRequest))
        .also { result -> assertThat(result.body).isNotNull }.body!!

      profileDTOs = listOf(profileDTO1, profileDTO2)
    }

    @Test
    fun `retrieve profiles of both current and previous version (migrated)`() {
      val result = assertSearchReadinessProfileIsOk(allPrisonNumbers)

      assertThat(result.body).isNotEmpty.hasSize(profileDTOs.size)
      val profileMap = result.body!!.map { it.offenderId to it }.toMap()
      assertThat(profileMap).containsOnlyKeys(*prisonNumbers.toTypedArray())
      profileDTOs.forEach { expected ->
        val actual = profileMap[expected.offenderId]!!
        assertThat(actual.schemaVersion).isEqualTo(expectedVersion)
        assertThat(actual).isEqualTo(expected)
      }
    }

    @Test
    fun `retrieve and migrate profile(s) of previous version to latest`() {
      val expectedProfile = profileDTOs.first()
      val prisonNumber = expectedProfile.offenderId

      val result = assertSearchReadinessProfileIsOk(prisonNumber)

      assertThat(result.body).isNotEmpty.hasSize(1)
      val actual = result.body!!.first()
      assertThat(actual).isEqualTo(expectedProfile)
      assertThat(actual.schemaVersion).isEqualTo(expectedVersion)
      with(actual.profileData) {
        assertThat(prisonId).isNotNull()
        assertThat(within12Weeks).isNotNull.isTrue()
      }
    }
  }
}
