package uk.gov.justice.digital.hmpps.educationemployment.api.integration.resource

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileJsonRequest
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileJsonRequestWithSupportAccepted
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain.ProfileObjects.createProfileJsonRequestWithSupportDeclined

object SARTestData {
  private val objectMapperSAR = CapturedSpringConfigValues.objectMapperSAR
  private val objectMapper = CapturedSpringConfigValues.objectMapper

  val knownnCaseReferenceNumber = "X08769"

  val profileRequestOfKnownPrisonNumber = makeProfileRequestDTO(createProfileJsonRequest)
  val profileJsonOfKnownPrisonNumber = objectMapperSAR.valueToTree<JsonNode>(profileRequestOfKnownPrisonNumber).get("profileData")

  val profileOfAnotherPrisonNumber = makeProfileRequestDTO(createProfileJsonRequestWithSupportDeclined).profileData
  val profileJsonOfAnotherPrisonNumber = objectMapperSAR.valueToTree<JsonNode>(profileOfAnotherPrisonNumber)

  fun makeProfileRequestOfAnotherPrisonNumber(): ReadinessProfileRequestDTO = makeProfileRequestDTO(createProfileJsonRequestWithSupportDeclined).apply {
    profileData.supportDeclined!!.let {
      profileData.supportDeclined = it.copy(supportToWorkDeclinedReasonOther = "another reason to decline support")
    }
  }

  fun makeProfileRequestWithSupportAccepted(): ReadinessProfileRequestDTO = makeProfileRequestDTO(createProfileJsonRequestWithSupportAccepted).apply {
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
