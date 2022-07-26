package uk.gov.justice.digital.hmpps.hmppseducationemploymentapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProfileService() {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
