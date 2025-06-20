package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ReadinessProfileRepository
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.domain.TimeProvider
import java.time.LocalDate

interface ProfileService<ProfileType, SARResponseType> {
  fun createProfileForOffender(userId: String, offenderId: String, bookingId: Long, profile: ProfileType): ReadinessProfile

  fun updateProfileForOffender(userId: String, offenderId: String, bookingId: Long, profile: ProfileType): ReadinessProfile

  fun changeStatusForOffender(userId: String, offenderId: String, statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO?): ReadinessProfile

  fun getProfilesForOffenders(offenders: List<String>): List<ReadinessProfile>

  fun getProfileForOffender(offenderId: String): ReadinessProfile?

  fun getProfileForOffenderFilterByPeriod(
    prisonNumber: String,
    fromDate: LocalDate? = null,
    toDate: LocalDate? = null,
  ): SARResponseType
}

@Service
class ProfileNoteService(
  private val readinessProfileRepository: ReadinessProfileRepository,
  private val timeProvider: TimeProvider,
  private val objectMapper: ObjectMapper,
) {
  private val typeRefMutableNoteList by lazy { object : TypeReference<MutableList<Note>>() {} }
  private val typeRefNoteList by lazy { object : TypeReference<MutableList<Note>>() {} }

  fun addProfileNoteForOffender(userId: String, offenderId: String, attribute: ActionTodo, text: String): List<Note> {
    val storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    val notesList: MutableList<Note> = parseNoteListMutable(storedProfile.notesData)
    notesList.add(Note(userId, timeProvider.now(), attribute, text))
    storedProfile.notesData = notesList.json()
    storedProfile.modifiedBy = userId
    readinessProfileRepository.save(storedProfile)
    return notesList.filter { n -> n.attribute == attribute }
  }

  fun getProfileNotesForOffender(offenderId: String, attribute: ActionTodo): List<Note> {
    val storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    val notesList: List<Note> = parseNoteList(storedProfile.notesData)
    return notesList.filter { n -> n.attribute == attribute }
  }

  private fun parseNoteList(notesData: JsonNode) = objectMapper.treeToValue(notesData, typeRefNoteList)
  private fun parseNoteListMutable(notesData: JsonNode) = objectMapper.treeToValue(notesData, typeRefMutableNoteList)

  private fun List<Note>.json(): JsonNode = objectMapper.valueToTree(this)
}
