package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain

import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.defaultAuditLocalTime
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.defaultAuditor
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.entity.ReadinessProfile
import java.io.FileNotFoundException

object ProfileObjects {
  private val objectMapper = CapturedSpringConfigValues.objectMapper

  val knownPrisonNumber = "A1234BB"
  val anotherPrisonNumber = "K9876BC"
  val unknownPrisonNumber = "A1234BD"

  val createProfileJsonRequest = readJsonProfile("CreateProfile_correct.json")
  val createProfileJsonRequestWithSupportDeclined = readJsonProfile("CreateProfileDeclinedHistories.json")
  val createProfileJsonRequestWithSupportAccepted = readJsonProfile("CreateProfileAcceptedHistories.json")

  val profileJsonSample = readJsonProfile("sampleprofile.json")
  val profileJsonSample2 = readJsonProfile("sample2.json")
  val profileJsonSampleReadinessProfile = readJsonProfile("sampleReadinessprofile.json")

  val profileOfKnownPrisoner = makeProfile(knownPrisonNumber, 111111, profileJsonSample)
  val profileOfAnotherPrisoner = makeProfile(anotherPrisonNumber, 222222, profileJsonSample2)

  val createdBy = "CCOLUMBUS_GEN"
  val lastModifiedBy = "JSMITH_GEN"

  private fun readJsonProfile(fileName: String) = readTextFromResource("jsonprofile/$fileName")

  private fun readTextFromResource(filePath: String) = this.javaClass.classLoader.getResource(filePath)?.readText()
    ?: run { throw FileNotFoundException("$filePath not found") }

  private fun makeProfile(prisonNumber: String, bookingId: Long, profileData: String, notesData: String? = null) = ReadinessProfile(
    offenderId = prisonNumber,
    bookingId = bookingId,
    createdBy = defaultAuditor,
    createdDateTime = defaultAuditLocalTime,
    modifiedBy = defaultAuditor,
    modifiedDateTime = defaultAuditLocalTime,
    schemaVersion = "1.0",
    profileData = objectMapper.readTree(profileData),
    notesData = objectMapper.readTree(notesData ?: "[]"),
    new = true,
  )
}
