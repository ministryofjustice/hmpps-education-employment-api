package uk.gov.justice.digital.hmpps.educationemploymentapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing

@Configuration
@EnableR2dbcAuditing
class AuditAwareConfiguration()
