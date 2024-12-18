package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.data.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.integration.util.TestData
import java.io.File

object SARTestData {
  private val objectMapperSAR = CapturedSpringConfigValues.objectMapperSAR
  private val objectMapper = CapturedSpringConfigValues.objectMapper

  val knownPrisonNumber = "A1234BB"
  val profileRequestOfKnownPrisonNumber = makeProfileRequestDTO(TestData.createProfileJsonRequest)
  val profileJsonOfKnownPrisonNumber = objectMapperSAR.valueToTree<JsonNode>(profileRequestOfKnownPrisonNumber).get("profileData")

  val createProfileJsonRequestWithSupportDeclined =
    File("src/test/resources/CreateProfileDeclinedHistories.json").inputStream().readBytes().toString(Charsets.UTF_8)
  val anotherPrisonNumber = "K9876BC"
  val profileOfAnotherPrisonNumber =
    makeProfileRequestDTO(createProfileJsonRequestWithSupportDeclined, cleanHistory = false).profileData
  val profileJsonOfAnotherPrisonNumber = objectMapperSAR.valueToTree<JsonNode>(profileOfAnotherPrisonNumber)

  val createProfileJsonRequestWithSupportAccepted =
    File("src/test/resources/CreateProfileAcceptedHistories.json").inputStream().readBytes().toString(Charsets.UTF_8)
  val profileWithSupportAcceptedHistory =
    makeProfileRequestDTO(createProfileJsonRequestWithSupportAccepted, cleanHistory = false).profileData
  val profileJsonWithSupportAcceptedHistory = objectMapperSAR.valueToTree<JsonNode>(profileWithSupportAcceptedHistory)

  val unknownPrisonNumber = "A1234BD"

  val knownnCaseReferenceNumber = "X08769"

  fun makeProfileRequestOfAnotherPrisonNumber() = makeProfileRequestDTO(createProfileJsonRequestWithSupportDeclined).apply {
    profileData.supportDeclined!!.let {
      profileData.supportDeclined = it.copy(supportToWorkDeclinedReasonOther = "another reason to decline support")
    }
  }

  fun makeProfileRequestWithSupportAccepted() = makeProfileRequestDTO(createProfileJsonRequestWithSupportAccepted).apply {
    profileData.supportAccepted!!.let {
      profileData.supportAccepted = it.copy(workExperience = it.workExperience.copy(previousWorkOrVolunteering = ""))
    }
  }

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
