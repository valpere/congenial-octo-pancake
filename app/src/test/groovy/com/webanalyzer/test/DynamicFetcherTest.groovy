package com.webanalyzer.test

import com.webanalyzer.core.fetcher.FetchOptions
import com.webanalyzer.core.fetcher.WebPageFetcher
import spock.lang.Specification
import spock.lang.Timeout

/**
 * Tests for the WebPageFetcher class with focus on dynamic page fetching.
 */
class DynamicFetcherTest extends Specification {

  /**
   * Tests fetching a static page without JavaScript rendering.
   */
  def "should fetch static content from a simple page"() {
    given:
    def fetcher = new WebPageFetcher()
    def options = new FetchOptions(
        dynamic: false,
        timeout: 15000
    )

    when:
    def content = fetcher.fetchPage("https://example.com", options)

    then:
    content.contains("<h1>Example Domain</h1>")
    !content.contains("Exception occurred while rendering")
  }

  /**
   * Tests fetching a JavaScript-rendered page using dynamic mode.
   * This test verifies that content rendered by JavaScript is correctly captured.
   */
  @Timeout(60)
  // This test may take longer as it uses a real browser
  def "should fetch dynamically generated content using Selenium"() {
    given:
    def fetcher = new WebPageFetcher()
    def options = new FetchOptions(
        dynamic: true,
        wait: 5000,
        timeout: 30000,
        userAgent: "Mozilla/5.0 WebPageAnalyzer Test"
    )

    when:
    // Using a JavaScript-heavy site that requires rendering
    def content = fetcher.fetchPage("https://www.google.com", options)

    then:
    content.contains("google")
    // Check for actual error indicators rather than just the word "Error"
    !content.contains("Exception occurred while rendering")
    !content.contains("Selenium error:")
    !content.contains("WebDriverException")
  }

  /**
   * Tests using a custom selector to wait for dynamic content to load.
   */
  @Timeout(60)
  def "should wait for specific element using CSS selector"() {
    given:
    def fetcher = new WebPageFetcher()
    def options = new FetchOptions(
        dynamic: true,
        wait: 2000,
        timeout: 30000,
        waitForSelector: "input[name='q']" // Wait for Google's search input
    )

    when:
    def content = fetcher.fetchPage("https://www.google.com", options)

    then:
    content.contains("input")
    content.contains("name=\"q\"")
    !content.contains("Exception occurred while rendering")
  }

  /**
   * Tests executing custom JavaScript during page fetching.
   */
  @Timeout(60)
  def "should execute custom JavaScript during fetch"() {
    given:
    def fetcher = new WebPageFetcher()
    def options = new FetchOptions(
        dynamic: true,
        wait: 3000,
        timeout: 30000,
        customJavaScript: "document.title = 'Modified by WebPageAnalyzer';"
    )

    when:
    def content = fetcher.fetchPage("https://example.com", options)

    then:
    content.contains("Modified by WebPageAnalyzer")
    !content.contains("Exception occurred while rendering")
  }
}
