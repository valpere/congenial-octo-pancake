package com.webanalyzer.core.analyzer

/**
 * Custom exception for analyzer errors.
 */
class AnalyzerException extends Exception {
  AnalyzerException(String message) {
    super(message)
  }

  AnalyzerException(String message, Throwable cause) {
    super(message, cause)
  }
}
