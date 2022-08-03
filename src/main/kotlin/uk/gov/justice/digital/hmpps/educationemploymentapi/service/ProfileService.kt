package uk.gov.justice.digital.hmpps.educationemploymentapi.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model.ReadinessProfileFilter
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.repository.ReadinessProfileRespository
import java.util.Arrays

@Service
class ProfileService(
  private val readinessProfileRepository:ReadinessProfileRespository
) {
  suspend fun createProfileForOffender(offenderId:String, bookingId:Int, profile:Profile): ReadinessProfile {
    return readinessProfileRepository.save(ReadinessProfile(offenderId, bookingId, "todo_from_auth", profile))
  }

  suspend fun getProfilesForOffenders(offenders:List<String>) = readinessProfileRepository.findForGivenOffenders(ReadinessProfileFilter(offenders))

  suspend fun getProfileForOffender(offenderId:String) :ReadinessProfile = readinessProfileRepository.findForGivenOffenders(ReadinessProfileFilter(
    listOf(offenderId)
  )).take(1).first()

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
