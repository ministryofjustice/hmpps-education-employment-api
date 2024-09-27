package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.data.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.util.TestData

object SARTestData {
  private val objectMapper = CapturedSpringConfigValues.objectMapper

  val knownPRN = "A1234BB"
  val bookingIdOfKnownPRN = TestData.newBookingId
  val profileRequestOfKnownPRN = makeProfileRequestDTO(TestData.createProfileJsonRequest)
  val profileJsonOfKnownPRN = objectMapper.valueToTree<JsonNode>(profileRequestOfKnownPRN).get("profileData")

  val unknownPRN = "A1234BD"

  val knownCRN = "X08769"

  private fun makeProfileRequestDTO(profileJson: String, cleanHistory: Boolean = true) = objectMapper.readValue(
    profileJson,
    object : TypeReference<ReadinessProfileRequestDTO>() {},
  ).apply {
    if (cleanHistory) {
      profileData.supportAccepted_history?.clear()
      profileData.supportDeclined_history?.clear()
    }
  }
}

const val SAR_ROLE = "ROLE_SAR_DATA_ACCESS"
const val INCORRECT_SAR_ROLE = "WRONG_SAR_DATA_ACCESS"
