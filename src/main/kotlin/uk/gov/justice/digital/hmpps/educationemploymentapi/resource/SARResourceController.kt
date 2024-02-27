package uk.gov.justice.digital.hmpps.educationemploymentapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.SARReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.service.ProfileService
import javax.validation.Valid
import javax.validation.constraints.Pattern

@Validated
@RestController
@RequestMapping("/subject-access-request", produces = [MediaType.APPLICATION_JSON_VALUE])
class SARResourceController(
  private val profileService: ProfileService,
) {
  @PreAuthorize("hasAnyRole('SAR_DATA_ACCESS')")
  @GetMapping("/{offenderId}")
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
            schema = Schema(implementation = SARReadinessProfileDTO::class)
          )
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  fun getOffenderProfile(
    @Schema(description = "offenderId", example = "A1234BC", required = true)
    @Valid @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$")
    @PathVariable offenderId: String
  ): SARReadinessProfileDTO {
    return SARReadinessProfileDTO(profileService.getProfileForOffender(offenderId))
  }
}
