package uk.gov.justice.digital.hmpps.educationemployment.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.envers.repository.config.EnableEnversRepositories

@Configuration
@EnableEnversRepositories(basePackages = ["uk.gov.justice.digital.hmpps.educationemployment.api"])
class EnversConfig
