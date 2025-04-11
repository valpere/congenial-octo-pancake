package com.webanalyzer.core.fetcher

/**
 * Exception thrown when page fetching fails.
 */
class FetchException extends Exception {
  FetchException(String message) {
    super(message)
  }

  FetchException(String message, Throwable cause) {
    super(message, cause)
  }
}
