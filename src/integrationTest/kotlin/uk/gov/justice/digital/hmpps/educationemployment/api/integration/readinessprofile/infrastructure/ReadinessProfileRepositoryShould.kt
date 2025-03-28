package uk.gov.justice.digital.hmpps.educationemployment.api.integration.readinessprofile.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.educationemployment.api.entity.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure.RepositoryTestCase
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.profileOfKnownPrisoner

class ReadinessProfileRepositoryShould : RepositoryTestCase() {

  @Test
  fun `return empty list, when nothing has been created yet`() {
    readinessProfileRepository.findAll().let {
      assertThat(it).isEmpty()
    }
  }

  @Test
  fun `return nothing, for any prison number (offender ID)`() {
    val prisonNumber = ProfileObjects.unknownPrisonNumber
    val actual = readinessProfileRepository.findById(prisonNumber)
    assertThat(actual).isEmpty
  }

  @Nested
  @DisplayName("Given a known prisoner")
  inner class GivenPrisoner {
    private val newProfile = profileOfKnownPrisoner
    private val prisonNumber = newProfile.offenderId

    @Nested
    @DisplayName("and a new readiness profile")
    inner class AndNewReadinessProfile {
      @Test
      fun `return nothing, for given prison number`() {
        val actual = readinessProfileRepository.findById(prisonNumber)
        assertThat(actual).isEmpty
      }

      @Test
      fun `create a new profile with given prison number`() {
        val saved = readinessProfileRepository.save(newProfile)

        assertThat(saved).isEqualTo(newProfile.asExpected)
      }
    }

    @Nested
    @DisplayName("and an existing readiness profile")
    inner class AndExistingReadinessProfile {
      private lateinit var existingProfile: ReadinessProfile

      @BeforeEach
      internal fun setUp() {
        existingProfile = readinessProfileRepository.save(newProfile)
      }

      @Test
      fun `update existing profile`() {
        val saved = readinessProfileRepository.save(existingProfile)

        assertThat(saved).isEqualTo(existingProfile.asExpected)
      }
    }
  }

  private val ReadinessProfile.asExpected
    get() = this.copy(
      new = false,
      createdDateTime = currentTimeLocal,
      createdBy = auditor,
      modifiedDateTime = currentTimeLocal,
      modifiedBy = auditor,
    )
}
