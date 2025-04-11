package com.webanalyzer.core.extractor

/**
 * Custom exception for extractor errors.
 */
class ExtractorException extends Exception {
  ExtractorException(String message) {
    super(message)
  }

  ExtractorException(String message, Throwable cause) {
    super(message, cause)
  }
}
