package uk.gov.justice.digital.hmpps.educationemployment.api.integration.audit.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.displayName
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.system
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.username
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.infrastructure.UserPrincipalAuditorAware
import uk.gov.justice.digital.hmpps.educationemployment.api.config.DpsPrincipal
import java.util.*

@DisplayName("UserPrincipalAuditorAware Should")
class UserPrincipalAuditorAwareShould {
  private val auditorAware = UserPrincipalAuditorAware()

  @AfterEach
  fun resetSpringSecurity() {
    SecurityContextHolder.clearContext()
  }

  @Nested
  @DisplayName("Given a user is authenticated")
  inner class GivenAUserIsAuthenticated {
    @BeforeEach
    fun setUp() {
      setAuthentication()
    }

    @Test
    fun `get current auditor username`() {
      assertThat(auditorAware.currentAuditor).isEqualTo(Optional.of(username))
    }

    @Test
    fun `get current auditor display name`() {
      val actualDisplayName = UserPrincipalAuditorAware.getCurrentAuditorDisplayName()

      assertThat(actualDisplayName).isEqualTo(displayName)
    }

    private fun setAuthentication() {
      val principal = DpsPrincipal(username = username, displayName = displayName)
      val roles = emptyList<GrantedAuthority>()
      val authentication = TestingAuthenticationToken(principal, null, roles)
      SecurityContextHolder.getContext().authentication = authentication
    }
  }

  @Nested
  @DisplayName("Given a user is not authenticated")
  inner class GivenAUserIsNotAuthenticated {
    @Test
    fun `get current auditor username`() {
      assertThat(auditorAware.currentAuditor).isEqualTo(Optional.of(system))
    }

    @Test
    fun `get current auditor display name`() {
      val actualDisplayName = UserPrincipalAuditorAware.getCurrentAuditorDisplayName()
      assertThat(actualDisplayName).isEqualTo(system)
    }
  }
}
