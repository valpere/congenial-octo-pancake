package com.webanalyzer.core.fetcher

/**
 * Options for configuring the page fetch.
 */
class FetchOptions {
  boolean dynamic = false
  int wait = 5000
  int timeout = 30000
  String userAgent = "WebPageAnalyzer/1.0"
  String encoding = "UTF-8"
  String waitForSelector = null
  String customJavaScript = null
  Map<String, String> headers = [:]
}
