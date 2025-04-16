@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.resource.v1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.isA
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.ProfileNoteService
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1.ProfileV1Service
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V1Profiles
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.joinToJsonString
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.resource.ControllerTestBase
import kotlin.test.assertEquals

private const val READINESS_PROFILES_PATH = "/readiness-profiles"
const val SEARCH_ENDPOINT = "$READINESS_PROFILES_PATH/search"
const val PROFILE_ENDPOINT = "$READINESS_PROFILES_PATH/{id}"
const val NOTES_ENDPOINT = "$READINESS_PROFILES_PATH/{id}/notes/{attribute}"

@WebMvcTest(controllers = [ProfileResourceControllerV1::class])
@ContextConfiguration(classes = [ProfileResourceControllerV1::class])
class ProfileResourceControllerV1Test : ControllerTestBase() {
  @MockitoBean
  private lateinit var profileService: ProfileV1Service

  @MockitoBean
  private lateinit var noteService: ProfileNoteService

  @BeforeEach
  internal fun reset() {
    reset(profileService)
    initMvcMock(ProfileResourceControllerV1(profileService, noteService))
  }

  @Nested
  @DisplayName("Given a profile with notes")
  inner class GivenProfileWithNotes {
    private val prisonNumber = "A1234AB"
    private val disclosureLetter = ActionTodo.DISCLOSURE_LETTER.toString()
    private lateinit var notesList: MutableList<Note>

    @BeforeEach
    internal fun setUp() {
      notesList = notesToList(ProfileObjects.noteListJson)
    }

    @Test
    fun `Test GET of a PRELIMINARY retrieve profile notes`() {
      whenever(noteService.getProfileNotesForOffender(prisonNumber, ActionTodo.DISCLOSURE_LETTER)).thenReturn(notesList)

      val result = assertReadOnlyApiReplyJson(get(NOTES_ENDPOINT, prisonNumber, disclosureLetter))

      val retrievedNotesList = notesToList(result.response.contentAsString)
      assertThat(retrievedNotesList).hasSize(2)
      verify(noteService, times(1)).getProfileNotesForOffender(prisonNumber, ActionTodo.DISCLOSURE_LETTER)
    }

    @Test
    fun `Test Post of a add profile notes`() {
      whenever(noteService.addProfileNoteForOffender(any(), any(), any(), any())).thenReturn(notesList)
      val notes = ProfileObjects.noteFreeTextJson

      val result = assertReadWriteApiReplyJson(post(NOTES_ENDPOINT, prisonNumber, disclosureLetter), notes)

      val retrievedNotesList = notesToList(result.response.contentAsString)
      assert(retrievedNotesList.size == 2)
      verify(noteService, times(1)).addProfileNoteForOffender(any(), any(), any(), any())
    }
  }

  @Nested
  @DisplayName("Given a profile")
  inner class GivenAProfile {
    private val profile = V1Profiles.readinessProfileOfKnownPrisoner
    private val prisonNumber = profile.offenderId
    private val bookingId = profile.bookingId
    private val createdBy = profile.createdBy

    @Test
    fun `Test Post of a new profile, that should fail as deprecated`() {
      val createRequest = ProfileObjects.createProfileV1JsonRequest
      assertCreateProfileIsDeprecated(prisonNumber, createRequest)
    }

    @Test
    fun `Test Put of an update profile, that should fail as deprecated`() {
      val updateRequest = ProfileObjects.createProfileV1JsonRequest

      assertUpdateProfileIsDeprecated(prisonNumber, updateRequest)
    }

    @Test
    fun `Test Get profile of an Offender `() {
      whenever(profileService.getProfileForOffender(any())).thenReturn(profile)

      assertRetrieveProfileIsExpected(prisonNumber)
    }

    private fun assertCreateProfileIsDeprecated(
      prisonNumber: String,
      requestJson: String,
      resultMatcher: ResultMatcher = status().isGone,
    ) = assertReadWriteApiReplyJson(post(PROFILE_ENDPOINT, prisonNumber), requestJson, resultMatcher)
      .also { verify(profileService, never()).createProfileForOffender(any(), any(), any(), isA<Profile>()) }

    private fun assertUpdateProfileIsDeprecated(
      prisonNumber: String,
      requestJson: String,
      resultMatcher: ResultMatcher = status().isGone,
    ) = assertReadWriteApiReplyJson(put(PROFILE_ENDPOINT, prisonNumber), requestJson, resultMatcher)
      .also { verify(profileService, never()).updateProfileForOffender(any(), any(), any(), isA<Profile>()) }

    private fun assertRetrieveProfileIsExpected(prisonNumber: String) = assertReadOnlyApiReplyJson(get(PROFILE_ENDPOINT, prisonNumber))
      .also { result ->
        readinessProfileToValue(result.response.contentAsString).let {
          assertEquals(createdBy, it.createdBy)
          assertEquals(prisonNumber, it.offenderId)
          assertEquals(bookingId, it.bookingId)
        }

        verify(profileService, times(1)).getProfileForOffender(any())
      }
  }

  @Nested
  @DisplayName("Given some profiles")
  inner class GivenSomeProfiles {
    private val prisonNumbers = ProfileObjects.offenderIdList
    private val profileList = V1Profiles.readinessProfileList

    @Test
    fun `Test Get of profile list for offenders `() {
      whenever(profileService.getProfilesForOffenders(any())).thenReturn(profileList)

      assertSearchProfileIsExpected(prisonNumbers, profileList)
      verify(profileService, times(1)).getProfilesForOffenders(any())
    }

    private fun assertSearchProfileIsExpected(
      prisonNumbers: List<String>,
      expectedProfiles: List<ReadinessProfile>,
    ) = assertReadOnlyApiReplyJson(post(SEARCH_ENDPOINT), prisonNumbers.joinToJsonString()).also { result ->
      readinessProfileToList(result.response.contentAsString).forEachIndexed { i, it ->
        with(expectedProfiles[i]) {
          assertEquals(createdBy, it.createdBy)
          assertEquals(offenderId, it.offenderId)
          assertEquals(bookingId, it.bookingId)
        }
      }
    }
  }
}
