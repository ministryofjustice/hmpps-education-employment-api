@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO

object SARTestData {
  // private val objectMapperSAR = CapturedSpringConfigValues.objectMapperSAR
  private val objectMapper = CapturedSpringConfigValues.objectMapper

  // val profileRequestOfKnownPrisonNumber = makeProfileRequestDTO(createProfileV1JsonRequest)

  private fun makeProfileRequestDTO(profileJson: String): ReadinessProfileRequestDTO = objectMapper.readValue(
    profileJson,
    object : TypeReference<ReadinessProfileRequestDTO>() {},
  )
}

const val SAR_ROLE = "ROLE_SAR_DATA_ACCESS"
const val INCORRECT_SAR_ROLE = "WRONG_SAR_DATA_ACCESS"
