package uk.gov.justice.digital.hmpps.educationemployment.api.resource.dashboard

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationemployment.api.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.exceptions.CustomValidationException
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsDocumentSupportResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsReasonsSupportDeclinedResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsSummaryResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.GetMetricsWorkStatusResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.ProfileMetricsService
import java.time.LocalDate

@Validated
@RestController
@RequestMapping("/dashboard", produces = [APPLICATION_JSON_VALUE])
@Tag(name = "Dashboard")
@PreAuthorize("hasAnyRole('WORK_READINESS_EDIT','WORK_READINESS_VIEW', 'ROLE_EDUCATION_WORK_PLAN_EDIT', 'ROLE_EDUCATION_WORK_PLAN_VIEW')")
class DashboardGet(
  private val profileMetricsService: ProfileMetricsService,
) {
  @GetMapping("/reasons-support-declined")
  @Operation(
    summary = "Retrieve metrics Reasons for support declined, of given prison",
    description = "requires role ${DESC_READ_ONLY_ROLES}",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The success status is set as the request has been processed correctly.",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = GetMetricsReasonsSupportDeclinedResponse::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The failure status is set when the request is invalid. An error response will be provided.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Error: Unauthorised. The error status is set as the required authorisation was not provided.",
        content = [Content()],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Error: Access Denied. The error status is set as the required system role(s) was/were not found.",
        content = [Content()],
      ),
    ],
  )
  fun retrieveMetricsReasonsSupportDeclined(
    @RequestParam(required = true)
    @Parameter(description = "The identifier of the given prison.", example = "MDI")
    prisonId: String,
    @RequestParam(required = true)
    @Parameter(description = "The start date of reporting period (in ISO-8601 date format)", example = "2024-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    dateFrom: LocalDate,
    @RequestParam(required = true)
    @Parameter(description = "The end date of reporting period (in ISO-8601 date format)", example = "2024-01-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    dateTo: LocalDate,
  ): ResponseEntity<List<GetMetricsReasonsSupportDeclinedResponse>> {
    validateDatePeriodAndThrowError(dateFrom, dateTo)
    return ResponseEntity.ok(profileMetricsService.retrieveMetricsReasonsSupportDeclinedByPrisonIdAndDates(prisonId, dateFrom, dateTo))
  }

  @GetMapping("/documents-support-needed")
  @Operation(
    summary = "Retrieve metrics Documentation and support needed, of given prison",
    description = "requires role ${DESC_READ_ONLY_ROLES}",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The success status is set as the request has been processed correctly.",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = GetMetricsDocumentSupportResponse::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The failure status is set when the request is invalid. An error response will be provided.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Error: Unauthorised. The error status is set as the required authorisation was not provided.",
        content = [Content()],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Error: Access Denied. The error status is set as the required system role(s) was/were not found.",
        content = [Content()],
      ),
    ],
  )
  fun retrieveMetricsDocumentsSupportNeeded(
    @RequestParam(required = true)
    @Parameter(description = "The identifier of the given prison.", example = "MDI")
    prisonId: String,
    @RequestParam(required = true)
    @Parameter(description = "The start date of reporting period (in ISO-8601 date format)", example = "2024-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    dateFrom: LocalDate,
    @RequestParam(required = true)
    @Parameter(description = "The end date of reporting period (in ISO-8601 date format)", example = "2024-01-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    dateTo: LocalDate,
  ): ResponseEntity<List<GetMetricsDocumentSupportResponse>> {
    validateDatePeriodAndThrowError(dateFrom, dateTo)
    return ResponseEntity.ok(profileMetricsService.retrieveMetricsDocumentsSupportNeededByPrisonIdAndDates(prisonId, dateFrom, dateTo))
  }

  @GetMapping("/work-status")
  @Operation(
    summary = "Retrieve metrics Work status progress, of given prison",
    description = "requires role ${DESC_READ_ONLY_ROLES}",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The success status is set as the request has been processed correctly.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = GetMetricsWorkStatusResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The failure status is set when the request is invalid. An error response will be provided.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Error: Unauthorised. The error status is set as the required authorisation was not provided.",
        content = [Content()],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Error: Access Denied. The error status is set as the required system role(s) was/were not found.",
        content = [Content()],
      ),
    ],
  )
  fun retrieveMetricsWorkStatusProgress(
    @RequestParam(required = true)
    @Parameter(description = "The identifier of the given prison.", example = "MDI")
    prisonId: String,
    @RequestParam(required = true)
    @Parameter(description = "The start date of reporting period (in ISO-8601 date format)", example = "2024-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    dateFrom: LocalDate,
    @RequestParam(required = true)
    @Parameter(description = "The end date of reporting period (in ISO-8601 date format)", example = "2024-01-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    dateTo: LocalDate,
  ): ResponseEntity<GetMetricsWorkStatusResponse> {
    validateDatePeriodAndThrowError(dateFrom, dateTo)
    return ResponseEntity.ok(profileMetricsService.retrieveMetricsWorkStatusProgressByPrisonIdAndDates(prisonId, dateFrom, dateTo))
  }

  @GetMapping("/summary")
  @Operation(
    summary = "Retrieve metrics Summary, of given prison",
    description = "requires role ${DESC_READ_ONLY_ROLES}",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The success status is set as the request has been processed correctly.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = GetMetricsSummaryResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The failure status is set when the request is invalid. An error response will be provided.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Error: Unauthorised. The error status is set as the required authorisation was not provided.",
        content = [Content()],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Error: Access Denied. The error status is set as the required system role(s) was/were not found.",
        content = [Content()],
      ),
    ],
  )
  fun retrieveMetricsSummary(
    @RequestParam(required = true)
    @Parameter(description = "The identifier of the given prison.", example = "MDI")
    prisonId: String,
    @RequestParam(required = true)
    @Parameter(description = "The start date of reporting period (in ISO-8601 date format)", example = "2024-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    dateFrom: LocalDate,
    @RequestParam(required = true)
    @Parameter(description = "The end date of reporting period (in ISO-8601 date format)", example = "2024-01-31")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    dateTo: LocalDate,
  ): ResponseEntity<GetMetricsSummaryResponse> {
    validateDatePeriodAndThrowError(dateFrom, dateTo)
    return ResponseEntity.ok(profileMetricsService.retrieveMetricsSummaryByPrisonIdAndDates(prisonId, dateFrom, dateTo))
  }

  private fun validateDatePeriodAndThrowError(dateFrom: LocalDate, dateTo: LocalDate) {
    validateDatePeriod(dateFrom, dateTo)?.let { errorMessage -> throw CustomValidationException(errorMessage) }
  }
  private fun validateDatePeriod(dateFrom: LocalDate, dateTo: LocalDate): String? = when {
    dateFrom.isAfter(dateTo) -> "dateFrom ($dateFrom) cannot be after dateTo ($dateTo)"
    else -> null
  }
}

private const val DESC_READ_ONLY_ROLES = "<b>WORK_READINESS_VIEW</b> or <b>WORK_READINESS_EDIT</b>"
