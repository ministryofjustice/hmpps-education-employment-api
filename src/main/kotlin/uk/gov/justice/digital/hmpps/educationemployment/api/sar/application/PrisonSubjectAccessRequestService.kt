package uk.gov.justice.digital.hmpps.educationemployment.api.sar.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.application.instantFromZone
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.domain.TimeProvider
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note as NoteEntity
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile as ProfileEntity

@Service
class PrisonSubjectAccessRequestService(
  private val profileService: ProfileV2Service,
  private val objectMapper: ObjectMapper,
  timeProvider: TimeProvider,
) : HmppsPrisonSubjectAccessRequestService {
  private val timeZoneId by lazy { timeProvider.timeZoneId }
  private val typeRefProfileNotes by lazy { object : TypeReference<List<NoteEntity>>() {} }

  override fun getPrisonContentFor(
    prn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? = try {
    profileService.getProfilesForOffenderFilterByPeriod(prn, fromDate, toDate)
      .map {
        val profileData = Profile(objectMapper.treeToValue(it.profileData, ProfileEntity::class.java), timeZoneId)
        val notesData = objectMapper.treeToValue(it.notesData, typeRefProfileNotes).map { entity -> Note(entity, timeZoneId) }

        SARContentDTO(
          offenderId = it.offenderId,
          createdBy = it.createdBy,
          createdDateTime = it.createdDateTime.instantFromZone(timeZoneId),
          modifiedBy = it.modifiedBy,
          modifiedDateTime = it.modifiedDateTime.instantFromZone(timeZoneId),
          profileData = profileData,
          notesData = notesData,
        )
      }
      .ifEmpty { null }?.let { HmppsSubjectAccessRequestContent(it) }
  } catch (_: NotFoundException) {
    null
  }
}
