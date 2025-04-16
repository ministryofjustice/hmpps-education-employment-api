@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileV1JsonRequest
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileV1JsonRequestWithSupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileV1JsonRequestWithSupportDeclined

object SARTestData {
  private val objectMapperSAR = CapturedSpringConfigValues.objectMapperSAR
  private val objectMapper = CapturedSpringConfigValues.objectMapper

  val knownnCaseReferenceNumber = "X08769"

  val profileRequestOfKnownPrisonNumber = makeProfileRequestDTO(createProfileV1JsonRequest)
  val profileJsonOfKnownPrisonNumber = objectMapperSAR.valueToTree<JsonNode>(profileRequestOfKnownPrisonNumber).get("profileData")

  val profileOfAnotherPrisonNumber =
    makeProfileRequestDTO(createProfileV1JsonRequestWithSupportDeclined, cleanHistory = false).profileData
  val profileJsonOfAnotherPrisonNumber = objectMapperSAR.valueToTree<JsonNode>(profileOfAnotherPrisonNumber)

  val profileWithSupportAcceptedHistory =
    makeProfileRequestDTO(createProfileV1JsonRequestWithSupportAccepted, cleanHistory = false).profileData
  val profileJsonWithSupportAcceptedHistory = objectMapperSAR.valueToTree<JsonNode>(profileWithSupportAcceptedHistory)

  fun makeProfileRequestOfAnotherPrisonNumber() = makeProfileRequestDTO(createProfileV1JsonRequestWithSupportDeclined).apply {
    profileData.supportDeclined!!.let {
      profileData.supportDeclined = it.copy(supportToWorkDeclinedReasonOther = "another reason to decline support")
    }
  }

  fun makeProfileRequestWithSupportAccepted() = makeProfileRequestDTO(createProfileV1JsonRequestWithSupportAccepted).apply {
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
