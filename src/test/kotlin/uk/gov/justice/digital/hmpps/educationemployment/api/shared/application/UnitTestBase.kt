@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.educationemployment.api.shared.application

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.notesdata.domain.Note
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v2.Profile
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v1.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.domain.TimeProvider
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.v1.Profile as ProfileV1

@ExtendWith(MockitoExtension::class)
abstract class UnitTestBase {
  @Mock
  protected lateinit var timeProvider: TimeProvider

  @Spy
  protected val objectMapper = CapturedSpringConfigValues.objectMapper

  protected val defaultTimeZoneOffset = ZoneOffset.UTC
  protected val defaultTimeZone: ZoneId = defaultTimeZoneOffset
  protected val defaultCurrentLocalTime = LocalDateTime.of(2025, 1, 1, 1, 1, 1)
  protected val defaultCurrentTime: Instant by lazy { defaultCurrentLocalTime.atZone(defaultTimeZone).toInstant() }

  @BeforeEach
  internal open fun setUpBase() {
    lenient().whenever(timeProvider.nowAsInstant()).thenReturn(defaultCurrentTime)
    lenient().whenever(timeProvider.now()).thenReturn(defaultCurrentLocalTime)
  }

  companion object {
    internal val typeReferenceOfProfileV1 by lazy { object : TypeReference<ProfileV1>() {} }
    internal val typeReferenceOfProfile by lazy { object : TypeReference<Profile>() {} }
    internal val typeReferenceOfNoteList by lazy { object : TypeReference<MutableList<Note>>() {} }
    internal val typeReferenceOfReadinessProfile by lazy { object : TypeReference<ReadinessProfileDTO>() {} }
    internal val typeReferenceOfReadinessProfileList by lazy { object : TypeReference<List<ReadinessProfileDTO>>() {} }
  }

  protected fun readinessProfileToValue(jsonText: String) = jsonTextToValue(jsonText, typeReferenceOfReadinessProfile)
  protected fun readinessProfileToList(jsonText: String) = jsonTextToValue(jsonText, typeReferenceOfReadinessProfileList)

  protected fun profileV1JsonToValue(json: JsonNode) = jsonToValue(json, typeReferenceOfProfileV1)
  protected fun profileJsonToValue(json: JsonNode) = jsonToValue(json, typeReferenceOfProfile)

  protected fun notesToList(jsonText: String) = jsonTextToValue(jsonText, typeReferenceOfNoteList)
  protected fun notesJsonToList(json: JsonNode) = jsonToValue(json, typeReferenceOfNoteList)

  protected fun <T> jsonToValue(json: JsonNode, typeReference: TypeReference<T>) = objectMapper.treeToValue(json, typeReference)
  protected fun <T> jsonTextToValue(json: String, typeReference: TypeReference<T>) = objectMapper.readValue(json, typeReference)
}
