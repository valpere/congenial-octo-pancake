package com.webanalyzer.core.transformer

import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for the HtmlTransformer class.
 *
 * These tests verify HTML transformation to various formats (Markdown, plain text, JSON)
 * with proper character encoding and formatting.
 */
class HtmlTransformerTest extends Specification {

  @TempDir
  Path tempDir

  def "should transform HTML to Markdown"() {
    given:
    def transformer = new HtmlTransformer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Test Document</title></head>
                <body>
                    <h1>Test Heading</h1>
                    <p>This is a paragraph.</p>
                    <ul>
                        <li>Item 1</li>
                        <li>Item 2</li>
                    </ul>
                </body>
            </html>
        ''')
    def options = new TransformOptions(preserveLinks: true, includeImages: true)

    when:
    def result = transformer.transformFile(htmlFile, "markdown", options)

    then:
    result.contains("# Test Document")
    result.contains("# Test Heading")
    result.contains("This is a paragraph.")
    result.contains("* Item 1")
    result.contains("* Item 2")
  }

  def "should transform HTML to plain text"() {
    given:
    def transformer = new HtmlTransformer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Test Document</title></head>
                <body>
                    <h1>Test Heading</h1>
                    <p>This is a paragraph.</p>
                    <a href="https://example.com">Link</a>
                </body>
            </html>
        ''')
    def options = new TransformOptions(preserveLinks: true)

    when:
    def result = transformer.transformFile(htmlFile, "plain", options)

    then:
    result.contains("TEST DOCUMENT")
    result.contains("TEST HEADING")
    result.contains("This is a paragraph.")
    result.contains("Link [https://example.com]")
  }

  def "should not include links in output when preserveLinks is false"() {
    given:
    def transformer = new HtmlTransformer()
    def htmlFile = createTempHtmlFile('''
            <html><body>
                <a href="https://example.com">Link</a>
            </body></html>
        ''')
    def options = new TransformOptions(preserveLinks: false)

    when:
    def result = transformer.transformFile(htmlFile, "plain", options)

    then:
    result.contains("Link")
    !result.contains("https://example.com")
  }

  def "should transform HTML to JSON"() {
    given:
    def transformer = new HtmlTransformer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Test Document</title></head>
                <body>
                    <h1>Test Heading</h1>
                    <p>This is a paragraph.</p>
                </body>
            </html>
        ''')
    def options = new TransformOptions()

    when:
    def result = transformer.transformFile(htmlFile, "json", options)

    then:
    result.contains('"title":"Test Document"')
    result.contains('"type":"heading"')
    result.contains('"text":"Test Heading"')
    result.contains('"type":"paragraph"')
    result.contains('"text":"This is a paragraph."')
  }

  def "should preserve non-ASCII characters in transformations"() {
    given:
    def transformer = new HtmlTransformer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Test Document</title></head>
                <body>
                    <h1>Привіт світе</h1>
                    <p>你好世界</p>
                </body>
            </html>
        ''')
    def options = new TransformOptions()

    when:
    def markdownResult = transformer.transformFile(htmlFile, "markdown", options)
    def plainResult = transformer.transformFile(htmlFile, "plain", options)
    def jsonResult = transformer.transformFile(htmlFile, "json", options)

    then:
    markdownResult.contains("# Привіт світе")
    markdownResult.contains("你好世界")
    plainResult.contains("ПРИВЕТ МИР")
    plainResult.contains("你好世界")
    jsonResult.contains("Привіт світе")
    jsonResult.contains("你好世界")
    !jsonResult.contains("\\u")  // No Unicode escape sequences
  }

  def "should throw exception for unsupported format"() {
    given:
    def transformer = new HtmlTransformer()
    def htmlFile = createTempHtmlFile("<html><body><p>Text</p></body></html>")
    def options = new TransformOptions()

    when:
    transformer.transformFile(htmlFile, "invalid-format", options)

    then:
    thrown(TransformerException)
  }

  def "should process direct HTML string"() {
    given:
    def transformer = new HtmlTransformer()
    def html = '''
            <html>
                <head><title>String Test</title></head>
                <body><p>Transform from string</p></body>
            </html>
        '''
    def options = new TransformOptions()

    when:
    def result = transformer.transform(html, "markdown", options)

    then:
    result.contains("# String Test")
    result.contains("Transform from string")
  }

  @Unroll
  def "should properly handle images for #format format with includeImages=#includeImages"() {
    given:
    def transformer = new HtmlTransformer()
    def htmlFile = createTempHtmlFile('''
            <html><body>
                <img src="test.jpg" alt="Test Image" />
            </body></html>
        ''')
    def options = new TransformOptions(includeImages: includeImages)

    when:
    def result = transformer.transformFile(htmlFile, format, options)

    then:
    if (includeImages) {
      assert result.contains("test.jpg")
    } else {
      assert !result.contains("test.jpg")
    }

    where:
    format     | includeImages
    "markdown" | true
    "markdown" | false
    "plain"    | true
    "plain"    | false
    "json"     | true
    "json"     | false
  }

  private File createTempHtmlFile(String content) {
    def file = Files.createFile(tempDir.resolve("test.html")).toFile()
    file.setText(content, "UTF-8")
    return file
  }
}
