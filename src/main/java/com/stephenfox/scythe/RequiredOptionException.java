package com.stephenfox.scythe;

/**
 * Exception class for options that are required but not found.
 *
 * @author Stephen Fox.
 */
class RequiredOptionException extends RuntimeException {
  RequiredOptionException(String message) {
    super(message);
  }
}
