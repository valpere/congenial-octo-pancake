package com.webanalyzer.core.transformer

/**
 * Custom exception for transformation errors.
 */
class TransformerException extends Exception {
  TransformerException(String message) {
    super(message)
  }

  TransformerException(String message, Throwable cause) {
    super(message, cause)
  }
}
