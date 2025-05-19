package uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.domain

import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.defaultAuditLocalTime
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.defaultAuditor
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.CircumstanceChangesRequiredToWork
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import java.io.FileNotFoundException

object ProfileObjects {
  const val KNOWN_PRISON_NUMBER = "A1234BB"
  const val ANOTHER_PRISON_NUMBER = "K9876BC"
  const val SUPPORT_DECLINED_PRISON_NUMBER = "D1111EF"

  private val objectMapper = CapturedSpringConfigValues.objectMapper

  val knownPrisonNumber = KNOWN_PRISON_NUMBER
  val anotherPrisonNumber = ANOTHER_PRISON_NUMBER
  val declinedPrisonNumber = SUPPORT_DECLINED_PRISON_NUMBER
  val unknownPrisonNumber = "A1234BD"

  val prisonId = "prison2"
  val prisonName = "Prison 2"

  val prison1 = "prison1"
  val prison1Name = "Prison 1"

  val prison3 = "prison3"

  val createProfileJsonRequest = readJsonProfile("CreateProfile_correct.json")
  val createProfileV1JsonRequest = readJsonProfile("CreateProfile_correct_v1.json")
  val createProfileV1JsonRequestWithSupportDeclined = readJsonProfile("CreateProfileDeclinedHistories.json")
  val createProfileV1JsonRequestWithSupportAccepted = readJsonProfile("CreateProfileAcceptedHistories.json")

  val profileJsonSample = readJsonProfile("sampleprofile.json")
  val profileJsonSample2 = readJsonProfile("sample2.json")
  val profileJsonSampleReadinessProfile = readJsonProfile("sampleReadinessprofile.json")
  val profileJsonSupportDeclined = readJsonProfile("profileOfStateSupportDeclined.json")

  val profileOfKnownPrisoner = makeProfile(knownPrisonNumber, 111111, profileJsonSample)
  val profileOfAnotherPrisoner = makeProfile(anotherPrisonNumber, 222222, profileJsonSample2)
  val profileOfUnknownPrisoner = makeProfile(unknownPrisonNumber, 333333, profileJsonSampleReadinessProfile)
  val profileOfDeclinedSupportPrisoner = makeProfile(unknownPrisonNumber, 444444, profileJsonSupportDeclined)

  val profilesFromPrison2 = listOf(profileOfKnownPrisoner, profileOfAnotherPrisoner)
  val profilesFromPrison1 = listOf(profileOfDeclinedSupportPrisoner)

  val migratedProfile = profileOfKnownPrisoner.copy(
    createdBy = "system",
    modifiedBy = "system",
    schemaVersion = "2.0",
  ).let { ReadinessProfileDTO(it) }.apply {
    with(profileData) {
      prisonId = ""
      within12Weeks = true
    }
  }

  val noteString = "M*** had another little lamb"

  val createdBy = "USER1_GEN"
  val lastModifiedBy = "USER2_GEN"

  var noteFreeTextJson = """
    {
    "text": "$noteString"
    }
  """.trimIndent()

  val circumstanceChangesRequiredToWorkList = listOf(CircumstanceChangesRequiredToWork.DEPENDENCY_SUPPORT)

  val ReadinessProfile.requestDto: ReadinessProfileRequestDTO get() = ReadinessProfileRequestDTO(
    bookingId,
    objectMapper.treeToValue(profileData, Profile::class.java),
  )

  private fun readJsonProfile(fileName: String) = readTextFromResource("jsonprofile/$fileName")

  private fun readTextFromResource(filePath: String) = this.javaClass.classLoader.getResource(filePath)?.readText()
    ?: run { throw FileNotFoundException("$filePath not found") }

  private fun makeProfile(prisonNumber: String, bookingId: Long, profileData: String, notesData: String? = null, schemaVersion: String = "2.0") = ReadinessProfile(
    offenderId = prisonNumber,
    bookingId = bookingId,
    createdBy = defaultAuditor,
    createdDateTime = defaultAuditLocalTime,
    modifiedBy = defaultAuditor,
    modifiedDateTime = defaultAuditLocalTime,
    schemaVersion = schemaVersion,
    profileData = objectMapper.readTree(profileData),
    notesData = objectMapper.readTree(notesData ?: "[]"),
    new = true,
  )
}
