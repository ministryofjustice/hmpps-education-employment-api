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
  @PreAuthorize("hasAnyRole('SAR_DATA_ACCESS')")
  @GetMapping
  @Operation(
    summary = "Fetch the work readiness profile for a given offender",
    description = "Currently requires role <b>ROLE_VIEW_PRISONER_DATA</b>",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile for the requested offender",
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
    if (prn.isNullOrBlank() && crn.isNullOrBlank()) {
      return ResponseEntity.badRequest().body(
        ErrorResponse(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "One of prn or crn must be supplied.",
          developerMessage = "One of prn or crn must be supplied.",
        ),
      )
    }

    if (!prn.isNullOrEmpty()) {
      return try {
        SARReadinessProfileDTO(profileService.getProfileForOffender(prn)).let { ResponseEntity.ok(it) }
      } catch (ex: NotFoundException) {
        return ResponseEntity.noContent().build()
      }
    } else {
      return ResponseEntity.status(209).build()
    }
  }
}
