package com.webanalyzer.core.comparator

/**
 * Custom exception for HTML comparison errors.
 */
class ComparatorException extends Exception {
  ComparatorException(String message) {
    super(message)
  }

  ComparatorException(String message, Throwable cause) {
    super(message, cause)
  }
}
