package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import com.fasterxml.jackson.core.type.TypeReference
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.flow.first
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.CapturedSpringMapperConfiguration
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.NotFoundException
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Note
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model.ReadinessProfileFilter
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.repository.ReadinessProfileRespository
import java.time.LocalDateTime

@Service
class ProfileService(
  private val readinessProfileRepository: ReadinessProfileRespository
) {
  suspend fun createProfileForOffender(userId: String, offenderId: String, bookingId: Int, profile: Profile): ReadinessProfile {
    if (readinessProfileRepository.existsById(offenderId)) {
      throw AlreadyExistsException(offenderId)
    }
    return readinessProfileRepository.save(ReadinessProfile(userId, offenderId, bookingId, profile, true))
  }

  suspend fun updateProfileForOffender(userId: String, offenderId: String, bookingId: Int, profile: Profile): ReadinessProfile {
    var storedProfile: ReadinessProfile = readinessProfileRepository.findById(offenderId) ?: throw NotFoundException(offenderId)
    storedProfile.profileData = Json.of(CapturedSpringMapperConfiguration.OBJECT_MAPPER.writeValueAsString(profile))
    storedProfile.modifiedBy = userId
    return readinessProfileRepository.save(storedProfile)
  }
  suspend fun getProfilesForOffenders(offenders: List<String>) = readinessProfileRepository.findForGivenOffenders(ReadinessProfileFilter(offenders))

  suspend fun getProfileForOffender(offenderId: String): ReadinessProfile = readinessProfileRepository.findForGivenOffenders(ReadinessProfileFilter(listOf(offenderId))).first()
  suspend fun addProfileNoteForOffender(userId: String, offenderId: String, attribute: ActionTodo, text: String): List<Note> {
    var storedProfile: ReadinessProfile = readinessProfileRepository.findById(offenderId) ?: throw NotFoundException(offenderId)
    var notesList: MutableList<Note> = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
      storedProfile.notesData.asString(), object : TypeReference<MutableList<Note>> () {}
    )
    notesList.add(Note(userId, LocalDateTime.now(), attribute, text))
    storedProfile.notesData = Json.of(CapturedSpringMapperConfiguration.OBJECT_MAPPER.writeValueAsString(notesList))
    storedProfile.modifiedBy = userId
    readinessProfileRepository.save(storedProfile)
    return notesList.filter { n -> n.attribute == attribute }
  }

  suspend fun getProfileNotesForOffender(offenderId: String, attribute: ActionTodo): List<Note> {
    var storedProfile: ReadinessProfile = readinessProfileRepository.findById(offenderId) ?: throw NotFoundException(offenderId)
    var notesList: List<Note> = CapturedSpringMapperConfiguration.OBJECT_MAPPER.readValue(
      storedProfile.notesData.asString(), object : TypeReference<List<Note>>() {}
    )
    return notesList.filter { n -> n.attribute == attribute }
  }
}
