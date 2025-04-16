package uk.gov.justice.digital.hmpps.educationemployment.api.exceptions

import java.util.function.Supplier

class AlreadyExistsException(offenderId: String) : Exception("Readiness profile already exists for offender $offenderId")

class NotFoundException(var offenderId: String) :
  Exception("Readiness profile does not exist for offender $offenderId"),
  Supplier<Throwable> {
  override fun get(): Throwable = throw NotFoundException(offenderId)
}

class InvalidStateException(var offenderId: String) :
  Exception("Readiness profile is in an invalid state for  $offenderId"),
  Supplier<Throwable> {
  override fun get(): Throwable = throw InvalidStateException(offenderId)
}

class DeprecatedApiException : Exception("The API has been deprecated and is no longer supported.")
