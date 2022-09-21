package uk.gov.justice.digital.hmpps.educationemploymentapi.resource

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
import uk.gov.justice.digital.hmpps.educationemploymentapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.NoteDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.NoteRequestDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.ReadinessProfileDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.ReadinessProfileRequestDTO
import uk.gov.justice.digital.hmpps.educationemploymentapi.data.jsonprofile.ActionTodo
import uk.gov.justice.digital.hmpps.educationemploymentapi.service.ProfileService
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.ValidationException
import javax.validation.Validator
import javax.validation.constraints.Pattern

@Validated
@RestController
@RequestMapping("/readiness-profiles", produces = [MediaType.APPLICATION_JSON_VALUE])
class ProfileResourceController(
  private val profileService: ProfileService,
  private val validator: Validator,
  private val objectMapper: ObjectMapper
) {
  @PreAuthorize("hasRole('ROLE_VIEW_PRISONER_DATA')")
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
  fun getOffenderProfiles(
    @Schema(description = "List of offender Ids", example = "[\"A1234BC\", \"B1234DE\"]", required = true)
    @RequestBody offenderIds: List<String>
  ): List<ReadinessProfileDTO> {

    offenderIds.forEach { validateOffenderId(it) }

    val profiles = ArrayList<ReadinessProfileDTO>()
    profileService.getProfilesForOffenders(offenderIds).forEach {
      profiles.add(ReadinessProfileDTO(it, objectMapper))
    }
    return profiles
  }

  @PreAuthorize("hasRole('ROLE_VIEW_PRISONER_DATA')")
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
  fun createOffenderProfile(
    @PathVariable offenderId: String,
    @RequestBody requestDTO: ReadinessProfileRequestDTO,
    @AuthenticationPrincipal oauth2User: String
  ): ReadinessProfileDTO {

    validateOffenderId(offenderId)
    validateReadinessProfileRequest(requestDTO)

    return ReadinessProfileDTO(
      profileService.createProfileForOffender(
        oauth2User,
        offenderId,
        requestDTO.bookingId,
        requestDTO.profileData
      ),
      objectMapper
    )
  }

  @PreAuthorize("hasRole('ROLE_VIEW_PRISONER_DATA')")
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
  fun updateOffenderProfile(
    @PathVariable offenderId: String,
    @RequestBody @Parameter requestDTO: ReadinessProfileRequestDTO,
    @AuthenticationPrincipal oauth2User: String
  ): ReadinessProfileDTO {

    validateOffenderId(offenderId)
    validateReadinessProfileRequest(requestDTO)

    return ReadinessProfileDTO(
      profileService.updateProfileForOffender(
        oauth2User,
        offenderId,
        requestDTO.bookingId,
        requestDTO.profileData
      ),
      objectMapper
    )
  }

  @PreAuthorize("hasRole('ROLE_VIEW_PRISONER_DATA')")
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
  fun getOffenderProfile(
    @Schema(description = "offenderId", example = "A1234BC", required = true)
    @Valid @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$")
    @PathVariable offenderId: String
  ): ReadinessProfileDTO {

    validateOffenderId(offenderId)

    return ReadinessProfileDTO(profileService.getProfileForOffender(offenderId), objectMapper)
  }

  @PreAuthorize("hasRole('ROLE_VIEW_PRISONER_DATA')")
  @PostMapping("/{offenderId}/notes/{attribute}")
  @Operation(
    summary = "Create a note against the offenders profile for the given attribute",
    description = "Currently requires role <b>ROLE_VIEW_PRISONER_DATA</b>. Attribute must be one of the enums values of SupportAccepted.ActionsRequired.Action.todoItem",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile attribute note created - the notes for that attribute after the addition are returned",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = NoteDTO::class))
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
  fun createOffenderProfileNote(
    @Schema(description = "offenderId", example = "A1234BC", required = true)
    @PathVariable offenderId: String,
    @Schema(description = "attribute", example = "DISCLOSURE_LETTER", required = true)
    @PathVariable attribute: ActionTodo,
    @RequestBody requestDTO: NoteRequestDTO,
    @AuthenticationPrincipal oauth2User: String
  ): List<NoteDTO> {

    validateOffenderId(offenderId)

    // TODO: validate the NoteRequestDTO - field length

    return profileService.addProfileNoteForOffender(oauth2User, offenderId, attribute, requestDTO.text)
      .map { note -> NoteDTO(note) }
  }

  @PreAuthorize("hasRole('ROLE_VIEW_PRISONER_DATA')")
  @GetMapping("/{offenderId}/notes/{attribute}")
  @Operation(
    summary = "Get all notes against the offenders profile for the given attribute",
    description = "Gets all notes against the given attribute for the offender. Currently requires role <b>ROLE_VIEW_PRISONER_DATA</b>",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile notes for the given attribute are returned:w" +
          "",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = NoteDTO::class))
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
  fun getOffenderProfileNotes(
    @Schema(description = "offenderId", example = "A1234BC", required = true)
    @PathVariable offenderId: String,
    @Schema(description = "attribute", example = "DISCLOSURE_LETTER", required = true)
    @PathVariable attribute: ActionTodo
  ): List<NoteDTO> {

    validateOffenderId(offenderId)

    return profileService.getProfileNotesForOffender(offenderId, attribute).map { note -> NoteDTO(note) }
  }

  private fun validateOffenderId(offenderId: String) {
    if (!offenderId.matches(Regex("^[A-Z]\\d{4}[A-Z]{2}\$"))) {
      throw ValidationException("OffenderId provided ($offenderId) does not match pattern ie 'A1111AA'")
    }
  }

  private fun validateReadinessProfileRequest(requestDTO: ReadinessProfileRequestDTO) {
    val violations: Set<ConstraintViolation<ReadinessProfileRequestDTO>> = validator.validate(requestDTO)
    if (violations.isNotEmpty()) {
      throw ConstraintViolationException(violations)
    }
  }
}
