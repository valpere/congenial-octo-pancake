package com.webanalyzer.core.comparator

/**
 * Options for HTML document comparison.
 */
class ComparisonOptions {
  /**
   * The comparison mode.
   * - "structure": Compare DOM structure and hierarchy
   * - "content": Compare text and element content
   * - "visual": Compare visual styling elements
   */
  String mode = "content"

  /**
   * Optional CSS selector to limit comparison to specific elements.
   */
  String selector

  /**
   * List of attributes to ignore during comparison.
   */
  List<String> ignoreAttributes = []

  /**
   * Character encoding to use when reading files.
   */
  String encoding = "UTF-8"
}

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
