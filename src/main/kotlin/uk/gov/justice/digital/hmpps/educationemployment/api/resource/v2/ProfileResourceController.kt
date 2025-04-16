package uk.gov.justice.digital.hmpps.educationemployment.api.resource.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationemployment.api.config.DpsPrincipal
import uk.gov.justice.digital.hmpps.educationemployment.api.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.StatusChangeUpdateRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ProfileV2Service
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.v2.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.shared.infrastructure.OffenderIdConstraint

@Validated
@RestController
@RequestMapping("/v2/readiness-profiles", produces = [MediaType.APPLICATION_JSON_VALUE])
class ProfileResourceController(
  private val profileService: ProfileV2Service,
) {
  @PreAuthorize("hasAnyRole('WORK_READINESS_VIEW','WORK_READINESS_EDIT')")
  @PostMapping("/search")
  @Operation(
    summary = "Fetch work readiness profile summaries for a set of offenders",
    description = "The records are un-paged and requires role <b>ROLE_VIEW_PRISONER_DATA</b> currently",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile summaries for the requested offenders",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = ReadinessProfileDTO::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getOffenderProfiles(
    @Schema(description = "List of offender Ids", example = "[\"A1234BC\", \"B1234DE\"]", required = true)
    @RequestBody
    @OffenderIdConstraint(message = "Invalid Offender Id")
    offenderIds: List<@Valid String>,
  ): List<ReadinessProfileDTO> {
    val profiles = ArrayList<ReadinessProfileDTO>()
    profileService.getProfilesForOffenders(offenderIds).forEach {
      profiles.add(ReadinessProfileDTO(it))
    }
    return profiles
  }

  @PreAuthorize("hasRole('WORK_READINESS_EDIT')")
  @PostMapping("/{offenderId}")
  @Operation(
    summary = "Create the work readiness profile for an offender",
    description = "Called once to initially create the profile. Currently requires role <b>ROLE_VIEW_PRISONER_DATA</b>",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile created",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ReadinessProfileDTO::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun createOffenderProfile(
    @Valid
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$", message = "Invalid Offender Id")
    @PathVariable
    offenderId: String,
    @Valid
    @RequestBody
    requestDTO: ReadinessProfileRequestDTO,
    @AuthenticationPrincipal oauth2User: DpsPrincipal,
  ): ReadinessProfileDTO = ReadinessProfileDTO(
    profileService.createProfileForOffender(
      oauth2User.name,
      offenderId,
      requestDTO.bookingId,
      requestDTO.profileData,
    ),
  )

  @PreAuthorize("hasRole('WORK_READINESS_EDIT')")
  @PutMapping("/{offenderId}")
  @Operation(
    summary = "Update the work readiness profile for an offender",
    description = "Called to modify an offenders work readiness profile. Currently requires role <b>ROLE_VIEW_PRISONER_DATA</b>",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile modified",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ReadinessProfileDTO::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun updateOffenderProfile(
    @Valid
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$", message = "Invalid Offender Id")
    @PathVariable
    offenderId: String,
    @Valid
    @RequestBody
    @Parameter
    requestDTO: ReadinessProfileRequestDTO,
    @AuthenticationPrincipal oauth2User: DpsPrincipal,
  ): ReadinessProfileDTO = ReadinessProfileDTO(
    profileService.updateProfileForOffender(
      oauth2User.name,
      offenderId,
      requestDTO.bookingId,
      requestDTO.profileData,
    ),
  )

  @PreAuthorize("hasRole('WORK_READINESS_EDIT')")
  @PutMapping("/status-change/{offenderId}")
  @Operation(
    summary = "Update the work readiness profile for an offender",
    description = "Called to modify an offenders work readiness profile. Currently requires role <b>ROLE_VIEW_PRISONER_DATA</b>",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile modified",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ReadinessProfileDTO::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun changeStatusOfOffender(
    @Valid
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$", message = "Invalid Offender Id")
    @PathVariable
    offenderId: String,
    @Valid
    @RequestBody
    @Parameter
    statusChangeUpdateRequestDTO: StatusChangeUpdateRequestDTO,
    @AuthenticationPrincipal oauth2User: DpsPrincipal,
  ): ReadinessProfileDTO = ReadinessProfileDTO(
    profileService.changeStatusForOffender(
      oauth2User.name,
      offenderId,
      statusChangeUpdateRequestDTO,
    ),
  )

  @PreAuthorize("hasAnyRole('WORK_READINESS_VIEW','WORK_READINESS_EDIT')")
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
            schema = Schema(implementation = ReadinessProfileDTO::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getOffenderProfile(
    @Schema(description = "offenderId", example = "A1234BC", required = true)
    @Valid
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$")
    @PathVariable
    offenderId: String,
  ): ReadinessProfileDTO = ReadinessProfileDTO(profileService.getProfileForOffender(offenderId))
}
