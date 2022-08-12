package uk.gov.justice.digital.hmpps.educationemploymentapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration

/**
 * This class is used as a way to capture the Spring Jackson mapper in a way that
 * data classes etc may use it as they cannot have it auto injected
 */
@Configuration
class CapturedSpringMapperConfiguration(private val objectMapper: ObjectMapper) {
  companion object {
    lateinit var OBJECT_MAPPER: ObjectMapper
  }

  init {
    OBJECT_MAPPER = objectMapper
  }
}
