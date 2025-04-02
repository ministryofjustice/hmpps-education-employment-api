package uk.gov.justice.digital.hmpps.educationemployment.api.integration.config

import org.mockito.Mockito.lenient
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.defaultAuditTime
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.domain.AuditObjects.defaultAuditor
import uk.gov.justice.digital.hmpps.educationemployment.api.audit.infrastructure.UserPrincipalAuditorAware
import java.util.*

@TestConfiguration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider", auditorAwareRef = "auditorProvider")
@ComponentScan("uk.gov.justice.digital.hmpps.educationemployment.api.integration.shared.infrastructure")
@Profile("repository-test")
class TestJpaConfig {
  @Primary
  @Bean
  fun dateTimeProvider(): DateTimeProvider = mock(DateTimeProvider::class.java).also { dateTimeProvider ->
    lenient().whenever(dateTimeProvider.now).thenReturn(Optional.of(defaultAuditTime))
  }

  @Bean
  fun auditorProvider(): AuditorAware<String> = mock(UserPrincipalAuditorAware::class.java).also { auditorProvider ->
    lenient().whenever(auditorProvider.currentAuditor).thenReturn(Optional.of(defaultAuditor))
  }
}
