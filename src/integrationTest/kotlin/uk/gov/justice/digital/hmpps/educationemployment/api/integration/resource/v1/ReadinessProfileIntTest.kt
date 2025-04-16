@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileV1JsonRequest
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile

class ReadinessProfileIntTest : ReadinessProfileV1TestCase() {
  @Test
  fun `Get the exception for profile for an unknown offender`() {
    val prisonNumber = ProfileObjects.unknownPrisonNumber
    val expectedError = "Readiness profile does not exist for offender $prisonNumber"
    assertGetReadinessProfileFailed(prisonNumber, HttpStatus.BAD_REQUEST, expectedError)
  }

  @Nested
  inner class GivenKnownProfile {
    private val prisonNumber = ProfileObjects.knownPrisonNumber

    @Test
    fun `Post a profile for an offender, will fail`() {
      val request = parseProfileRequestDTO(createProfileV1JsonRequest)

      assertAddReadinessProfileFailed(prisonNumber, request, HttpStatus.GONE)
    }

    @Nested
    inner class AndProfileExists {
      private lateinit var knownProfile: ReadinessProfile

      @BeforeEach
      internal fun setUp() {
        knownProfile = addProfile(prisonNumber, parseProfileRequestDTO(createProfileV1JsonRequest))
      }

      @Test
      fun `Update a profile for an offender, will fail`() {
        val request = parseProfileRequestDTO(createProfileV1JsonRequest)

        assertUpdateReadinessProfileFailed(prisonNumber, request, HttpStatus.GONE)
      }

      @Test
      fun `Get a profile for an offender`() {
        val knownProfileData = parseProfile(knownProfile.profileData)

        val result = assertGetReadinessProfileIsOk(prisonNumber)
        assertThat(result.body).isNotNull
        assertThat(result.body!!.profileData).isEqualTo(knownProfileData)
      }

      @Test
      fun `Get an empty list when a profile note is not present for an offender`() {
        val result = assertGetNotesIsOk(prisonNumber, ActionTodo.DISCLOSURE_LETTER)
        assertThat(result.body).isNullOrEmpty()
      }
    }
  }

  @Nested
  inner class GivenAnotherProfileExists {
    private val prisonNumber = ProfileObjects.anotherPrisonNumber
    private lateinit var anotherProfile: ReadinessProfile

    @BeforeEach
    internal fun setUp() {
      anotherProfile = addProfile(prisonNumber, parseProfileRequestDTO(createProfileV1JsonRequest))
    }

    @Test
    fun `Post a profile note for an offender`() {
      val noteText = ProfileObjects.noteString
      val attribute = ActionTodo.DISCLOSURE_LETTER

      val result = assertAddNoteIsOk(prisonNumber, attribute, noteText)
      assertThat(result.body)
        .isNotEmpty
        .first()
        .hasFieldOrPropertyWithValue("attribute", attribute.name)
        .hasFieldOrPropertyWithValue("text", noteText)
    }

    @Nested
    inner class AndNoteExists {
      private val noteText = ProfileObjects.noteString
      private val attribute = ActionTodo.DISCLOSURE_LETTER

      @BeforeEach
      internal fun setUp() {
        assertAddNoteIsOk(prisonNumber, attribute, noteText)
      }

      @Test
      fun `Retrieve a profile note for an offender`() {
        val result = assertGetNotesIsOk(prisonNumber, attribute)
        assertThat(result.body)
          .isNotEmpty
          .first()
          .hasFieldOrPropertyWithValue("attribute", attribute.name)
          .hasFieldOrPropertyWithValue("text", noteText)
      }
    }
  }
}
