package uk.gov.justice.digital.hmpps.hmppseducationemploymentapi.resource

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppseducationemploymentapi.service.ProfileService
import javax.validation.constraints.Size

@RestController
@RequestMapping("/readiness-profiles", produces = [MediaType.APPLICATION_JSON_VALUE])
class ProfileResource(
  private val profileService: ProfileService
) {
  @GetMapping("/{offenderId}")
  fun getOffendersProfile(
    @PathVariable @Size(max = 8, min = 8, message = "Offender ID must be 8 characters") prisonId: String
  ): String {
    return "Hello there"
  }
}
