@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileV1JsonRequestWithSupportDeclined
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileV2JsonRequest
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileV2JsonRequestWithSupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileV2JsonRequestWithSupportDeclined

object SARTestData {
  private val objectMapper = CapturedSpringConfigValues.objectMapper

  val knownCaseReferenceNumber = "A000AA"

  val profileRequestOfKnownPrisonNumber = makeProfileRequestDTO(createProfileV2JsonRequest)
  val profileJsonOfKnownPrisonNumber = objectMapper.valueToTree<JsonNode>(profileRequestOfKnownPrisonNumber).get("profileData")

  val profileOfAnotherPrisonNumber =
    makeProfileRequestDTO(createProfileV2JsonRequestWithSupportDeclined).profileData
  val profileJsonOfAnotherPrisonNumber = objectMapper.valueToTree<JsonNode>(profileOfAnotherPrisonNumber)

  val profileWithSupportAccepted =
    makeProfileRequestDTO(createProfileV2JsonRequestWithSupportAccepted).profileData
  val profileJsonWithSupportAccepted = objectMapper.valueToTree<JsonNode>(profileWithSupportAccepted)

  fun makeProfileRequestOfAnotherPrisonNumber() = makeProfileRequestDTO(createProfileV1JsonRequestWithSupportDeclined).apply {
    profileData.supportDeclined!!.let {
      profileData.supportDeclined = it.copy(supportToWorkDeclinedReasonOther = "another reason to decline support")
    }
  }

  fun makeProfileRequestWithSupportDeclined() = makeProfileRequestDTO(createProfileV2JsonRequestWithSupportDeclined).apply {
    profileData.supportDeclined!!.let {
      profileData.supportDeclined = it.copy(supportToWorkDeclinedReasonOther = "another reason to decline support")
    }
  }

  fun makeProfileRequestWithSupportAccepted() = makeProfileRequestDTO(createProfileV2JsonRequestWithSupportAccepted).apply {
    profileData.supportAccepted!!.let {
      profileData.supportAccepted = it.copy(workExperience = it.workExperience.copy(previousWorkOrVolunteering = ""))
    }
  }

  private fun makeProfileRequestDTO(profileJson: String) = objectMapper.readValue(
    profileJson,
    object : TypeReference<ReadinessProfileRequestDTO>() {},
  )
}

const val SAR_ROLE = "ROLE_SAR_DATA_ACCESS"
const val INCORRECT_SAR_ROLE = "WRONG_SAR_DATA_ACCESS"
