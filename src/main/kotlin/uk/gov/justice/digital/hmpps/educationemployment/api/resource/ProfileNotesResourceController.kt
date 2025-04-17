package uk.gov.justice.digital.hmpps.educationemployment.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.educationemployment.api.config.DpsPrincipal
import uk.gov.justice.digital.hmpps.educationemployment.api.config.ErrorResponse
import uk.gov.justice.digital.hmpps.educationemployment.api.profiledata.domain.ActionTodo
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.NoteDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.NoteRequestDTO
import uk.gov.justice.digital.hmpps.educationemployment.api.readinessprofile.application.ProfileNoteService

@Validated
@RestController
@RequestMapping("/readiness-profiles", "/v1/readiness-profiles", "/v2/readiness-profiles", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Notes")
class ProfileNotesResourceController(
  private val profileNoteService: ProfileNoteService,
) {
  @PreAuthorize("hasRole('WORK_READINESS_EDIT')")
  @PostMapping("/{offenderId}/notes/{attribute}")
  @Operation(
    summary = "Create a note against the offenders profile for the given attribute",
    description = "Currently requires role $DESC_READ_WRITE_ROLE. Attribute must be one of the enums values of SupportAccepted.ActionsRequired.Action.todoItem",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile attribute note created - the notes for that attribute after the addition are returned",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = NoteDTO::class)),
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
  fun createOffenderProfileNote(
    @Schema(description = "offenderId", example = "A1234BC", required = true)
    @Valid
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$")
    @PathVariable
    offenderId: String,
    @Schema(description = "attribute", example = "DISCLOSURE_LETTER", required = true)
    @Valid
    @PathVariable
    attribute: ActionTodo,
    @RequestBody requestDTO: NoteRequestDTO,
    @AuthenticationPrincipal oauth2User: DpsPrincipal,
  ): List<NoteDTO> {
    // TODO: validate the NoteRequestDTO - field length

    return profileNoteService.addProfileNoteForOffender(oauth2User.name, offenderId, attribute, requestDTO.text)
      .map { note -> NoteDTO(note) }
  }

  @PreAuthorize("hasAnyRole('WORK_READINESS_VIEW','WORK_READINESS_EDIT')")
  @GetMapping("/{offenderId}/notes/{attribute}")
  @Operation(
    summary = "Get all notes against the offenders profile for the given attribute",
    description = "Gets all notes against the given attribute for the offender. Currently requires role $DESC_READ_ONLY_ROLES",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Work readiness profile notes for the given attribute are returned:",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = NoteDTO::class)),
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
  fun getOffenderProfileNotes(
    @Schema(description = "offenderId", example = "A1234BC", required = true)
    @Valid
    @Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$", message = "Invalid Offender Id")
    @PathVariable
    offenderId: String,
    @Schema(description = "attribute", example = "DISCLOSURE_LETTER", required = true)
    @PathVariable
    attribute: ActionTodo,
  ): List<NoteDTO> = profileNoteService.getProfileNotesForOffender(offenderId, attribute).map { note -> NoteDTO(note) }
}

private const val DESC_READ_WRITE_ROLE = "<b>WORK_READINESS_EDIT</b>"
private const val DESC_READ_ONLY_ROLES = "<b>WORK_READINESS_VIEW</b> or <b>WORK_READINESS_EDIT</b>"
