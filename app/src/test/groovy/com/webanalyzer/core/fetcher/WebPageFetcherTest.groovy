package com.webanalyzer.core.fetcher

import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll

/**
 * Unit tests for the WebPageFetcher class.
 *
 * These tests verify both static and dynamic web page fetching capabilities,
 * with a focus on proper configuration handling and error management.
 */
class WebPageFetcherTest extends Specification {

  def "should fetch static page content"() {
    given:
    def fetcher = new WebPageFetcher()
    def options = new FetchOptions(
        dynamic: false,
        timeout: 10000
    )

    when:
    def content = fetcher.fetchPage("https://example.com", options)

    then:
    content.contains("<h1>Example Domain</h1>")
    content.contains("<html")
    content.contains("</html>")
  }

  @Timeout(60)
  def "should fetch dynamic page content using headless browser"() {
    given:
    def fetcher = new WebPageFetcher()
    def options = new FetchOptions(
        dynamic: true,
        wait: 2000,
        timeout: 30000
    )

    when:
    def content = fetcher.fetchPage("https://www.google.com", options)

    then:
    content.contains("<html")
    content.contains("</html>")
    content.contains("google")
  }

  def "should handle connection timeout gracefully"() {
    given:
    def fetcher = new WebPageFetcher()
    def options = new FetchOptions(
        dynamic: false,
        timeout: 1  // 1ms timeout - guaranteed to time out
    )

    when:
    fetcher.fetchPage("https://example.com", options)

    then:
    thrown(FetchException)
  }

  def "should handle invalid URLs gracefully"() {
    given:
    def fetcher = new WebPageFetcher()
    def options = new FetchOptions(
        dynamic: false,
        timeout: 5000
    )

    when:
    fetcher.fetchPage("invalid-url", options)

    then:
    thrown(FetchException)
  }

  def "should apply custom user agent"() {
    given:
    def fetcher = new WebPageFetcher()
    def customAgent = "WebPageAnalyzer-Test/1.0"
    def options = new FetchOptions(
        dynamic: false,
        timeout: 10000,
        userAgent: customAgent
    )

    when:
    def content = fetcher.fetchPage("https://httpbin.org/user-agent", options)

    then:
    content.contains(customAgent)
  }

  def "should apply custom headers"() {
    given:
    def fetcher = new WebPageFetcher()
    def customHeaderName = "X-Custom-Header"
    def customHeaderValue = "Test-Value"
    def options = new FetchOptions(
        dynamic: false,
        timeout: 10000,
        headers: [(customHeaderName): customHeaderValue]
    )

    when:
    def content = fetcher.fetchPage("https://httpbin.org/headers", options)

    then:
    content.contains(customHeaderName)
    content.contains(customHeaderValue)
  }

  @Unroll
  def "should validate FetchOptions with #scenario"() {
    given:
    def options = new FetchOptions(
        timeout: timeout,
        wait: wait,
        userAgent: userAgent
    )

    expect:
    options.timeout == expectedTimeout
    options.wait == expectedWait
    options.userAgent == expectedUserAgent

    where:
    scenario          | timeout | wait  | userAgent         || expectedTimeout | expectedWait | expectedUserAgent
    "default values"  | 30000   | 5000  | "WebPageAnalyzer" || 30000           | 5000         | "WebPageAnalyzer"
    "custom values"   | 60000   | 10000 | "Custom/1.0"      || 60000           | 10000        | "Custom/1.0"
    "minimum timeout" | 1000    | 5000  | "WebPageAnalyzer" || 1000            | 5000         | "WebPageAnalyzer"
    "minimum wait"    | 30000   | 0     | "WebPageAnalyzer" || 30000           | 0            | "WebPageAnalyzer"
  }
}
