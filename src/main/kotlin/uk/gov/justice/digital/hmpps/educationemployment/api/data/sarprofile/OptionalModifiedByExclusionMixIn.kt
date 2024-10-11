package uk.gov.justice.digital.hmpps.educationemployment.api.data.sarprofile

import com.fasterxml.jackson.annotation.JsonIgnore

interface OptionalModifiedByExclusionMixIn {
  @get:JsonIgnore
  @set:JsonIgnore
  var modifiedBy: String?
}
