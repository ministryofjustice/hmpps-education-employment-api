@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.helpers

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import java.time.LocalDate

/**
 * helper class for testing with profile v2
 */
@Service
class ProfileV2Helper(
  val profileV2Service: ProfileV2Service,
) {
  fun addReadinessProfileForTest(userId: String, offenderId: String, bookingId: Long, profile: Profile) = profileV2Service.createProfileForOffender(userId, offenderId, bookingId, profile)

  fun updateReadinessProfileForTest(userId: String, offenderId: String, bookingId: Long, profile: Profile) = profileV2Service.updateProfileForOffender(userId, offenderId, bookingId, profile)

  fun getProfileForOffenderFilterByPeriodForTest(offenderId: String, fromDate: LocalDate?, toDate: LocalDate?) = profileV2Service.getProfileForOffenderFilterByPeriod(offenderId, fromDate, toDate)
}
