package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Note
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemploymentapi.entity.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions.AlreadyExistsException
import uk.gov.justice.digital.hmpps.educationemploymentapi.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemploymentapi.repository.ReadinessProfileRepository
import java.time.LocalDateTime

@Service
class ProfileService(
  private val readinessProfileRepository: ReadinessProfileRepository,
  private val objectMapper: ObjectMapper
) {
  fun createProfileForOffender(userId: String, offenderId: String, bookingId: Long, profile: Profile): ReadinessProfile {
    if (readinessProfileRepository.existsById(offenderId)) {
      throw AlreadyExistsException(offenderId)
    }
    return readinessProfileRepository.save(ReadinessProfile(offenderId, bookingId, userId, LocalDateTime.now(), userId, LocalDateTime.now(), "1.0", JacksonUtil.toJsonNode(objectMapper.writeValueAsString(profile)), JacksonUtil.toJsonNode("[]"), true))
  }

  fun updateProfileForOffender(
    userId: String,
    offenderId: String,
    bookingId: Long,
    profile: Profile
  ): ReadinessProfile {
    var storedProfile: ReadinessProfile =
      readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    storedProfile.profileData = JacksonUtil.toJsonNode(objectMapper.writeValueAsString(profile))
    storedProfile.modifiedBy = userId
    return readinessProfileRepository.save(storedProfile)
  }

  fun getProfilesForOffenders(offenders: List<String>) =
    readinessProfileRepository.findAllById(offenders)

  fun getProfileForOffender(offenderId: String): ReadinessProfile {
    var profile: ReadinessProfile = readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    return profile
  }

  fun addProfileNoteForOffender(userId: String, offenderId: String, attribute: ActionTodo, text: String): List<Note> {
    var storedProfile: ReadinessProfile = readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    var notesList: MutableList<Note> = objectMapper.readValue(
      JacksonUtil.toString(storedProfile.notesData), object : TypeReference<MutableList<Note>>() {}
    )
    notesList.add(Note(userId, LocalDateTime.now(), attribute, text))
    storedProfile.notesData = JacksonUtil.toJsonNode(objectMapper.writeValueAsString(notesList))
    storedProfile.modifiedBy = userId
    readinessProfileRepository.save(storedProfile)
    return notesList.filter { n -> n.attribute == attribute }
  }

  fun getProfileNotesForOffender(offenderId: String, attribute: ActionTodo): List<Note> {
    var storedProfile: ReadinessProfile = readinessProfileRepository.findById(offenderId).orElseThrow(NotFoundException(offenderId))
    var notesList: List<Note> = objectMapper.readValue(
      JacksonUtil.toString(storedProfile.notesData), object : TypeReference<List<Note>>() {}
    )
    return notesList.filter { n -> n.attribute == attribute }
  }
}
