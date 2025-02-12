package uk.gov.justice.digital.hmpps.educationemployment.api.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.ActionsRequired
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.SupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.SupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.WorkExperience
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.WorkImpacts
import uk.gov.justice.digital.hmpps.educationemployment.api.data.jsonprofile.WorkInterests
import uk.gov.justice.digital.hmpps.educationemployment.api.data.sarprofile.ModifiedByExclusionMixIn
import uk.gov.justice.digital.hmpps.educationemployment.api.data.sarprofile.OptionalModifiedByExclusionMixIn

/**
 * This class is used as a way to capture the Spring Jackson mapper in a way that
 * data classes etc may use it as they cannot have it auto injected
 */
@Configuration
class CapturedSpringConfigValues {
  companion object {
    var objectMapper: ObjectMapper = configObjectMapper()
    var objectMapperSAR = configObjectMapper().apply {
      listOf(
        SupportDeclined::class.java,
        SupportAccepted::class.java,
      ).forEach { this.addMixIn(it, OptionalModifiedByExclusionMixIn::class.java) }
      listOf(
        ActionsRequired::class.java,
        WorkExperience::class.java,
        WorkImpacts::class.java,
        WorkInterests::class.java,
      ).forEach { this.addMixIn(it, ModifiedByExclusionMixIn::class.java) }
    }

    fun getDPSPrincipal(): DpsPrincipal = SecurityContextHolder.getContext().authentication.principal as DpsPrincipal
    fun configObjectMapper() = JsonMapper.builder()
      .addModules(JavaTimeModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build()
      .registerKotlinModule()
  }
}
