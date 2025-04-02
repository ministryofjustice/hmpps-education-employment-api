package uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain

import com.fasterxml.jackson.annotation.JsonIgnore

interface ModifiedByExclusionMixIn {
  @get:JsonIgnore
  val modifiedBy: String
}
