package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ProfileStatus
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.StatusChange

interface ProfileMixin {
  @get:JsonProperty("status")
  val statusAsList: List<ProfileStatus>

  @get:JsonProperty("statusChangeType")
  val statusChangeAsList: List<StatusChange>
}
