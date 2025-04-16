@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.helpers

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1.ProfileV1Service

/**
 * helper class for testing with profile v1
 */
@Service
class ProfileV1Helper(
  val profileV1Service: ProfileV1Service,
) {
  fun addReadinessProfileForTest(userId: String, offenderId: String, bookingId: Long, profile: Profile) = profileV1Service.createProfileForOffender(userId, offenderId, bookingId, profile)

  fun updateReadinessProfileForTest(userId: String, offenderId: String, bookingId: Long, profile: Profile) = profileV1Service.updateProfileForOffender(userId, offenderId, bookingId, profile)
}
