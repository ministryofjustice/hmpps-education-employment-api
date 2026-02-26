package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.V2Profiles
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.newNotes
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.application.UnitTestBase
import java.util.*
import kotlin.test.assertFailsWith

class ProfileNoteServiceTest : UnitTestBase() {
  @Mock
  private lateinit var readinessProfileRepository: ReadinessProfileRepository

  @InjectMocks
  private lateinit var profileNoteService: ProfileNoteService

  @Nested
  @DisplayName("Given an existing readiness profile, and some notes")
  inner class GivenExistingProfileAndNotes {
    private val prisonNumber = ProfileObjects.newOffenderId
    private val userIdCreator = ProfileObjects.createdBy

    private val updatedProfile = V2Profiles.updatedReadinessProfile

    private val updatedProfileWithNotes = V2Profiles.updatedReadinessProfileNotes

    private val actionToDoCV = ProfileObjects.actionToDoCV
    private val notesText = ProfileObjects.noteString

    @Test
    fun `save a readiness profile note`() {
      givenProfileFound(updatedProfile)
      givenSavedProfile(updatedProfile)

      val listNote = profileNoteService.addProfileNoteForOffender(userIdCreator, prisonNumber, actionToDoCV, notesText)

      verify(readinessProfileRepository).save(any())
      assertThat(listNote[0].text).isEqualTo(notesText)
    }

    @Test
    fun `retrieve a readiness profile note`() {
      givenProfileFound(updatedProfileWithNotes)

      val listNote = profileNoteService.getProfileNotesForOffender(prisonNumber, actionToDoCV)

      assertThat(listNote[0].text).isEqualTo(notesText)
    }
  }

  private fun givenSavedProfile(profile: ReadinessProfile) = whenever<ReadinessProfile>(readinessProfileRepository.save(any<ReadinessProfile>())).thenReturn(profile)

  private fun givenProfileFound(profile: ReadinessProfile) = whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.of(profile))

  @Nested
  @DisplayName("Given a non-existing readiness profile")
  inner class GivenNonExistingProfile {
    private val prisonNumber = ProfileObjects.newOffenderId
    private val userId = ProfileObjects.createdBy

    @BeforeEach
    internal fun setUp() {
      givenNoProfileFound()
    }

    @Test
    fun `throws an exception, when add a note to a non-existing readiness profile`() {
      assertFailsWith<NotFoundException> {
        profileNoteService.addProfileNoteForOffender(userId, prisonNumber, ActionTodo.BANK_ACCOUNT, newNotes)
      }.let {
        Assertions.assertThat(it.message).contains("Readiness profile does not exist for offender")
      }
    }

    @Test
    fun `throws an exception, when retrieve a note from a non-existing readiness profile`() {
      assertFailsWith<NotFoundException> {
        profileNoteService.getProfileNotesForOffender(userId, ActionTodo.BANK_ACCOUNT)
      }.let {
        Assertions.assertThat(it.message).contains("Readiness profile does not exist for offender")
      }
    }

    private fun givenNoProfileFound() = whenever(readinessProfileRepository.findById(any())).thenReturn(Optional.empty())
  }
}
