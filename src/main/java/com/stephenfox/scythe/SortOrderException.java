package com.stephenfox.scythe;

/**
 * An exception class when no sort order is given to options that will passed via a method.
 *
 * @author Stephen Fox.
 */
public class SortOrderException extends RuntimeException {
  public SortOrderException(String message) {
    super(message);
  }
}
