package com.webanalyzer.core.extractor

import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for the ElementExtractor class.
 *
 * These tests verify CSS selector-based element extraction and
 * the various output formatting options (JSON, CSV, text).
 */
class ElementExtractorTest extends Specification {

  @TempDir
  Path tempDir

  def "should extract elements using CSS selector"() {
    given:
    def extractor = new ElementExtractor()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <div class="container">
                        <h1>Title</h1>
                        <p class="content">Paragraph 1</p>
                        <p class="content">Paragraph 2</p>
                    </div>
                </body>
            </html>
        ''')
    def options = new ExtractorOptions(
        format: "json"
    )

    when:
    def result = extractor.extractFromFile(htmlFile, "p.content", options)

    then:
    result.contains("Paragraph 1")
    result.contains("Paragraph 2")
    !result.contains("Title")
  }

  def "should extract elements with specified attributes"() {
    given:
    def extractor = new ElementExtractor()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <a href="https://example.com" title="Example" id="link1">Link 1</a>
                    <a href="https://example.org" title="Example 2" id="link2">Link 2</a>
                </body>
            </html>
        ''')
    def options = new ExtractorOptions(
        format: "json",
        attributes: ["href", "id"]
    )

    when:
    def result = extractor.extractFromFile(htmlFile, "a", options)

    then:
    result.contains("https://example.com")
    result.contains("https://example.org")
    result.contains("link1")
    result.contains("link2")
    result.contains("Link 1")
    result.contains("Link 2")
    !result.contains("title")  // The title attribute was not requested
  }

  def "should format output as CSV when requested"() {
    given:
    def extractor = new ElementExtractor()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <a href="https://example.com">Link 1</a>
                    <a href="https://example.org">Link 2</a>
                </body>
            </html>
        ''')
    def options = new ExtractorOptions(
        format: "csv"
    )

    when:
    def result = extractor.extractFromFile(htmlFile, "a", options)

    then:
    result.contains("text,href")
    result.contains("Link 1,https://example.com")
    result.contains("Link 2,https://example.org")
  }

  def "should format output as text when requested"() {
    given:
    def extractor = new ElementExtractor()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <a href="https://example.com">Link 1</a>
                </body>
            </html>
        ''')
    def options = new ExtractorOptions(
        format: "txt"
    )

    when:
    def result = extractor.extractFromFile(htmlFile, "a", options)

    then:
    result.contains("TEXT: Link 1")
    result.contains("HREF: https://example.com")
  }

  def "should include HTML content when requested"() {
    given:
    def extractor = new ElementExtractor()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <div class="container"><p>Text</p></div>
                </body>
            </html>
        ''')
    def options = new ExtractorOptions(
        format: "json",
        includeHtml: true
    )

    when:
    def result = extractor.extractFromFile(htmlFile, "div.container", options)

    then:
    result.contains('"html"')
    result.contains('<div class="container"><p>Text</p></div>')
  }

  def "should handle no matching elements gracefully"() {
    given:
    def extractor = new ElementExtractor()
    def htmlFile = createTempHtmlFile("<html><body><p>Text</p></body></html>")
    def options = new ExtractorOptions(
        format: "json"
    )

    when:
    def result = extractor.extractFromFile(htmlFile, "div.nonexistent", options)

    then:
    result == "[]"  // Empty JSON array
  }

  def "should preserve non-ASCII characters in output"() {
    given:
    def extractor = new ElementExtractor()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <p>Привіт світе</p>
                    <p>你好世界</p>
                </body>
            </html>
        ''')
    def options = new ExtractorOptions(
        format: "json"
    )

    when:
    def result = extractor.extractFromFile(htmlFile, "p", options)

    then:
    result.contains("Привіт світе")
    result.contains("你好世界")
    !result.contains("\\u")  // No Unicode escape sequences
  }

  @Unroll
  def "should handle different formats: #format"() {
    given:
    def extractor = new ElementExtractor()
    def htmlFile = createTempHtmlFile('''
            <html><body><p>Test</p></body></html>
        ''')
    def options = new ExtractorOptions(
        format: format
    )

    when:
    def result = extractor.extractFromFile(htmlFile, "p", options)

    then:
    result.contains("Test")

    where:
    format << ["json", "csv", "txt"]
  }

  def "should process direct HTML string"() {
    given:
    def extractor = new ElementExtractor()
    def html = '''
            <html><body>
                <div class="test">Extract from string</div>
            </body></html>
        '''
    def options = new ExtractorOptions(
        format: "json"
    )

    when:
    def result = extractor.extract(html, "div.test", options)

    then:
    result.contains("Extract from string")
  }

  private File createTempHtmlFile(String content) {
    def file = Files.createFile(tempDir.resolve("test.html")).toFile()
    file.setText(content, "UTF-8")
    return file
  }
}
