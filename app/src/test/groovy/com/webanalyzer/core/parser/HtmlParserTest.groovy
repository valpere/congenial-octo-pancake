package com.webanalyzer.core.parser

import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for the HtmlParser class.
 *
 * These tests verify the HTML to JSON conversion functionality
 * with a focus on proper UTF-8 character handling.
 */
class HtmlParserTest extends Specification {

  @TempDir
  Path tempDir

  def "should parse simple HTML to JSON"() {
    given:
    def parser = new HtmlParser()
    def htmlFile = createTempHtmlFile("<html><head><title>Test</title></head><body><h1>Hello World</h1></body></html>")

    when:
    def result = parser.parseToJson(htmlFile, StandardCharsets.UTF_8, true, false)

    then:
    result.contains('"tagName":"html"')
    result.contains('"tagName":"head"')
    result.contains('"tagName":"title"')
    result.contains('"tagName":"body"')
    result.contains('"tagName":"h1"')
    result.contains('"text":"Hello World"')
  }

  def "should preserve UTF-8 characters"() {
    given:
    def parser = new HtmlParser()
    def htmlContent = "<html><body><p>Привет мир</p><p>你好世界</p><p>こんにちは世界</p></body></html>"
    def htmlFile = createTempHtmlFile(htmlContent)

    when:
    def result = parser.parseToJson(htmlFile, StandardCharsets.UTF_8, true, false)

    then:
    result.contains("Привет мир")
    result.contains("你好世界")
    result.contains("こんにちは世界")
    !result.contains("\\u")  // Should not contain Unicode escape sequences
  }

  def "should handle malformed HTML gracefully"() {
    given:
    def parser = new HtmlParser()
    def malformedHtml = "<html><body><div><p>Unclosed paragraph<div>Another div</html>"
    def htmlFile = createTempHtmlFile(malformedHtml)

    when:
    def result = parser.parseToJson(htmlFile, StandardCharsets.UTF_8, true, false)

    then:
    result.contains('"tagName":"html"')
    result.contains('"tagName":"body"')
    result.contains('"tagName":"div"')
    result.contains('"tagName":"p"')
    result.contains('"text":"Unclosed paragraph"')
  }

  def "should exclude text content when specified"() {
    given:
    def parser = new HtmlParser()
    def htmlFile = createTempHtmlFile("<html><body><p>Some text</p></body></html>")

    when:
    def result = parser.parseToJson(htmlFile, StandardCharsets.UTF_8, false, false)

    then:
    !result.contains('"text":"Some text"')
    result.contains('"tagName":"p"')
  }

  def "should produce pretty-printed JSON when specified"() {
    given:
    def parser = new HtmlParser()
    def htmlFile = createTempHtmlFile("<html><body><p>Hello</p></body></html>")

    when:
    def prettyResult = parser.parseToJson(htmlFile, StandardCharsets.UTF_8, true, true)
    def compactResult = parser.parseToJson(htmlFile, StandardCharsets.UTF_8, true, false)

    then:
    prettyResult.count('\n') > 0
    compactResult.count('\n') == 0
    prettyResult.length() > compactResult.length()
  }

  def "should parse HTML string directly"() {
    given:
    def parser = new HtmlParser()
    def html = "<html><body><p>Direct string parsing</p></body></html>"

    when:
    def result = parser.parseHtmlToJson(html, true, false)

    then:
    result.contains('"tagName":"html"')
    result.contains('"text":"Direct string parsing"')
  }

  def "should throw exception for invalid encoding"() {
    given:
    def parser = new HtmlParser()
    def htmlFile = createTempHtmlFile("<html><body><p>Test</p></body></html>")

    when:
    parser.parseToJson(htmlFile, StandardCharsets.US_ASCII, true, false)

    then:
    // The exact exception will depend on implementation details
    thrown(Exception)
  }

  private File createTempHtmlFile(String content) {
    def file = Files.createFile(tempDir.resolve("test.html")).toFile()
    file.setText(content, "UTF-8")
    return file
  }
}
