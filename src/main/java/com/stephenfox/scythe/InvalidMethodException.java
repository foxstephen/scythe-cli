package com.stephenfox.scythe;

/**
 * An exception class when methods are declared at the wrong scope/ level.
 *
 * @author Stephen Fox.
 */
public class InvalidMethodException extends RuntimeException {
  InvalidMethodException(String message) {
    super(message);
  }
}
