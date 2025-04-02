package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import com.fasterxml.jackson.annotation.JsonIgnore

interface OptionalModifiedByExclusionMixIn {
  @get:JsonIgnore
  @set:JsonIgnore
  var modifiedBy: String?
}
