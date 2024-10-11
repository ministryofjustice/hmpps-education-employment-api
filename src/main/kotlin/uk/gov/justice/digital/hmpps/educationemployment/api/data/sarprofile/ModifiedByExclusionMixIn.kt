package uk.gov.justice.digital.hmpps.educationemployment.api.data.sarprofile

import com.fasterxml.jackson.annotation.JsonIgnore

interface ModifiedByExclusionMixIn {
  @get:JsonIgnore
  val modifiedBy: String
}
