package uk.gov.justice.digital.hmpps.educationemployment.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationemployment.api.config.CapturedSpringConfigValues
import uk.gov.justice.digital.hmpps.educationemployment.api.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.data.SARReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.NotFoundException
import uk.gov.justice.digital.hmpps.educationemployment.api.service.ProfileService
import java.time.LocalDate

@Validated
@RestController
@RequestMapping("/subject-access-request", produces = [MediaType.APPLICATION_JSON_VALUE])
class SARResourceController(
  private val profileService: ProfileService,
) {
  private val objectMapperSAR = CapturedSpringConfigValues.objectMapperSAR

  @PreAuthorize("hasAnyRole('SAR_DATA_ACCESS', @environment.getProperty('hmpps.sar.additionalAccessRole', 'SAR_DATA_ACCESS'))")
  @GetMapping
  @Operation(
    summary = "Provides content for a prisoner to satisfy the needs of a subject access request on their behalf",
    description = "Requires role ROLE_SAR_DATA_ACCESS or additional role as specified by hmpps.sar.additionalAccessRole configuration.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Request successfully processed - content found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SARReadinessProfileDTO::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "204",
        description = "Request successfully processed - no content found",
        content = [Content()],
      ),
      ApiResponse(
        responseCode = "209",
        description = "Unauthorized to access this endpoint",
        content = [Content()],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request was not formed correctly",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],

      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to access this endpoint",
        content = [Content()],
      ),
    ],
  )
  fun getOffenderProfile(
    @Parameter(description = "NOMIS Prison Reference Number; Required", example = "A1234BC")
    @Valid
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$")
    @RequestParam
    prn: String? = null,
    @Parameter(description = "nDelius Case Reference Number; Not supported")
    @RequestParam
    crn: String? = null,
    @Parameter(description = "Optional parameter denoting minimum date of event occurrence which should be returned in the response")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @RequestParam
    fromDate: LocalDate? = null,
    @Parameter(description = "Optional parameter denoting maximum date of event occurrence which should be returned in the response")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @RequestParam
    toDate: LocalDate? = null,
  ): ResponseEntity<Any> {
    when {
      prn.isNullOrBlank() && crn.isNullOrBlank() -> "One of prn or crn must be supplied."
      fromDate != null && toDate != null && fromDate.isAfter(toDate) -> "fromDate ($fromDate) cannot be after toDate ($toDate)"
      else -> null
    }?.let {
      return ResponseEntity.badRequest().body(
        ErrorResponse(status = HttpStatus.BAD_REQUEST, userMessage = it, developerMessage = it),
      )
    }

    if (!prn.isNullOrEmpty()) {
      return try {
        SARReadinessProfileDTO(
          profileEntity = profileService.getProfileForOffenderFilterByPeriod(prn, fromDate, toDate),
        ).let { objectMapperSAR.writeValueAsString(it) }.let { ResponseEntity.ok(it) }
      } catch (ex: NotFoundException) {
        return ResponseEntity.noContent().build()
      }
    } else {
      return ResponseEntity.status(209).build()
    }
  }
}
