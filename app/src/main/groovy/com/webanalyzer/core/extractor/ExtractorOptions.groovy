package com.webanalyzer.core.extractor

/**
 * Options for element extraction.
 */
class ExtractorOptions {
  String encoding = "UTF-8"
  String format = "json"
  List<String> attributes = []
  boolean includeHtml = false
  boolean prettyPrint = false
}
