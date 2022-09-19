package uk.gov.justice.digital.hmpps.educationemploymentapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "feature-toggles")
data class FeatureToggles(
  var sopc: Boolean = false
)
