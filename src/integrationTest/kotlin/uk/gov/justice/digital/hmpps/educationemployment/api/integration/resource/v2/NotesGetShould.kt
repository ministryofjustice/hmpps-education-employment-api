package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource.v2

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.ANOTHER_PRISON_NUMBER
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.KNOWN_PRISON_NUMBER
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.anotherPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileJsonRequest
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.knownPrisonNumber
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.unknownPrisonNumber

class NotesGetShould : ReadinessProfileV2TestCase() {

  @Nested
  inner class GivenUnknownReadinessProfile {
    private val prisonNumber = unknownPrisonNumber

    @Test
    fun `return error, for unknown prisoner`() {
      val expectedError = "Readiness profile does not exist for offender $prisonNumber"

      val result = assertGetNotesFailed(prisonNumber, ActionTodo.ID)
      assertErrorMessageIsExpected(result, expectedUserMessage = expectedError)
    }
  }

  @Nested
  inner class GivenKnownReadinessProfile {
    private val prisonNumber = knownPrisonNumber

    @BeforeEach
    internal fun setUp() {
      assertAddReadinessProfileIsOk(prisonNumber, parseProfileRequestDTO(createProfileJsonRequest))
    }

    @Test
    fun `return empty list of notes`() {
      val result = assertGetNotesIsOk(prisonNumber, ActionTodo.DISCLOSURE_LETTER)
      assertThat(result.body)
        .isNotNull
        .isEmpty()
    }

    @Nested
    inner class AndNotesExist {
      private val attribute = ActionTodo.DISCLOSURE_LETTER
      private val noteTexts = listOf("1st note", "2nd note")

      @BeforeEach
      internal fun setUp() {
        noteTexts.forEach { assertAddNoteIsOk(prisonNumber, attribute, it) }
      }

      @Test
      fun `return list of notes`() {
        val result = assertGetNotesIsOk(prisonNumber, attribute)

        assertThat(result.body)
          .isNotNull
          .hasSize(noteTexts.size)
        val attributes = result.body!!.map { it.attribute }
        val texts = result.body!!.map { it.text }
        assertThat(attributes).containsOnly(attribute.name)
        assertThat(texts).containsOnly(*noteTexts.toTypedArray())
      }
    }
  }

  @Nested
  inner class GivenSomeProfilesExist {
    private val prisonNumbers = listOf(knownPrisonNumber, anotherPrisonNumber)

    @BeforeEach
    internal fun setUp() {
      prisonNumbers.forEach { assertAddReadinessProfileIsOk(it, parseProfileRequestDTO(createProfileJsonRequest)) }
    }

    @ParameterizedTest
    @ValueSource(strings = [KNOWN_PRISON_NUMBER, ANOTHER_PRISON_NUMBER])
    fun `return empty list of notes`(prisonNumber: String) {
      val result = assertGetNotesIsOk(prisonNumber, ActionTodo.DISCLOSURE_LETTER)
      assertThat(result.body)
        .isNotNull
        .isEmpty()
    }

    @Nested
    inner class AndOneProfileWithNotes {
      private val prisonNumberWithNotes = knownPrisonNumber
      private val prisonNumberWithoutNotes = anotherPrisonNumber
      private val attribute = ActionTodo.DISCLOSURE_LETTER
      private val noteText = ProfileObjects.noteString

      @BeforeEach
      internal fun setUp() {
        assertAddNoteIsOk(prisonNumberWithNotes, attribute, noteText)
      }

      @Test
      fun `return empty list of notes, when NOT exist`() {
        val result = assertGetNotesIsOk(prisonNumberWithoutNotes, attribute)

        assertThat(result.body).isEmpty()
      }

      @Test
      fun `return list of notes, when exists`() {
        val result = assertGetNotesIsOk(prisonNumberWithNotes, attribute)

        assertThat(result.body)
          .isNotEmpty
          .hasSize(1)
      }
    }
  }
}
