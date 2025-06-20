package uk.gov.justice.digital.hmpps.educationemployment.api.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.Action
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkExperience
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkImpacts
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.WorkInterests
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.ActionMixin
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.ActionsRequired
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.ModifiedByExclusionMixIn
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.OptionalModifiedByExclusionMixIn
import uk.gov.justice.digital.hmpps.educationemployment.api.sardata.domain.SupportAccepted

/**
 * This class is used as a way to capture the Spring Jackson mapper in a way that
 * data classes etc may use it as they cannot have it auto injected
 */
@Configuration
class CapturedSpringConfigValues {
  companion object {
    var objectMapper: ObjectMapper = configObjectMapper().apply {
      listOf(
        SupportAccepted::class.java,
        ActionsRequired::class.java,
      ).forEach {
        this.addMixIn(it, OptionalModifiedByExclusionMixIn::class.java)
        this.addMixIn(Action::class.java, ActionMixin::class.java)
      }
      listOf(
        uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionsRequired::class.java,
        WorkExperience::class.java,
        WorkImpacts::class.java,
        WorkInterests::class.java,
      ).forEach { this.addMixIn(it, ModifiedByExclusionMixIn::class.java) }
    }

    fun getDPSPrincipal(): DpsPrincipal = SecurityContextHolder.getContext().authentication.principal as DpsPrincipal
    fun configObjectMapper() = JsonMapper.builder()
      .addModules(JavaTimeModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .build()
      .registerKotlinModule()
  }
}
