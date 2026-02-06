package uk.gov.justice.digital.hmpps.educationemployment.api.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.ProfileNoteService
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects

const val NOTES_ENDPOINT = "/readiness-profiles/{id}/notes/{attribute}"

@WebMvcTest(controllers = [ProfileNotesResourceController::class])
@ContextConfiguration(classes = [ProfileNotesResourceController::class])
class ProfileResourceControllerV1Test : ControllerTestBase() {
  @MockitoBean
  private lateinit var noteService: ProfileNoteService

  @BeforeEach
  internal fun reset() {
    reset(noteService)
    initMvcMock(ProfileNotesResourceController(noteService))
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
}
