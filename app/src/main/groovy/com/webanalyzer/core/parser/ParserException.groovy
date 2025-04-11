package com.webanalyzer.core.parser

/**
 * Custom exception for HTML parsing errors
 */
class ParserException extends Exception {
  ParserException(String message) {
    super(message)
  }

  ParserException(String message, Throwable cause) {
    super(message, cause)
  }
}
