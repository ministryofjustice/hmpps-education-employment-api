package uk.gov.justice.digital.hmpps.educationemploymentapi.resource

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.CreateReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.Profile
import uk.gov.justice.digital.hmpps.educationemploymentapi.persistence.model.ReadinessProfile
import uk.gov.justice.digital.hmpps.educationemploymentapi.service.ProfileService
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@RestController
@RequestMapping("/readiness-profiles", produces = [MediaType.APPLICATION_JSON_VALUE])
class ProfileResource(
  private val profileService: ProfileService,
  private val objectMapper: ObjectMapper
) {
//  @PreAuthorize("hasRole('ROLE_TBD')")
  @PostMapping("/search")
  @Operation(
    summary = "Fetch work readiness profile summaries for a set of offenders",
    description = "The records are un-paged and requires role <b>TBD</b>",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile summaries for the requested offenders",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = ReadinessProfileDTO::class))
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
  suspend fun getOffenderProfiles(
    @Schema(description = "List of offender Ids", example = "[2342342, 212312]", required = true)
    @RequestBody @Valid @NotEmpty offenderIds: List<String>) : List<ReadinessProfileDTO> {
    val profiles = ArrayList<ReadinessProfileDTO>()
    profileService.getProfilesForOffenders(offenderIds).collect {
      profiles.add(ReadinessProfileDTO(it))
    }
    return profiles
  }

  @PostMapping("/{offenderId}")
  @Operation(
    summary = "Create the work readiness profile for an offender",
    description = "Called once to initially create the profile",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile created",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ReadinessProfileDTO::class)
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
  suspend fun createOffenderProfile(
    @Schema(description = "offenderId", example = "2342342", required = true)
    @PathVariable offenderId: String,
    @RequestBody requestDTO:CreateReadinessProfileRequestDTO) :ReadinessProfileDTO = ReadinessProfileDTO(profileService.createProfileForOffender(requestDTO.offenderId, requestDTO.bookingId, requestDTO.profileData))


  @GetMapping("/{offenderId}")
  @Operation(
    summary = "Fetch work readiness profile summaries for a set of a given offender",
    description = "Requires role <b>TBD</b>",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile summary for the requested offender",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ReadinessProfileDTO::class)
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
  suspend fun getOffenderProfileSummary(
    @Schema(description = "offenderId", example = "2342342", required = true)
    @PathVariable offenderId: String) :ReadinessProfileDTO = ReadinessProfileDTO(profileService.getProfileForOffender(offenderId))
}
