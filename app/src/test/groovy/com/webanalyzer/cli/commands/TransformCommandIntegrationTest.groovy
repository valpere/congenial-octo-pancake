package com.webanalyzer.cli.commands

import com.webanalyzer.cli.WebPageAnalyzer
import groovy.json.JsonSlurper
import picocli.CommandLine
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Timeout

import java.nio.file.Files
import java.nio.file.Path

/**
 * Integration tests for the TransformCommand class.
 *
 * These tests verify the end-to-end execution of the transform command,
 * which converts HTML to various formats (Markdown, plain text, JSON).
 */
class TransformCommandIntegrationTest extends Specification {

  @TempDir
  Path tempDir

  @Timeout(10)
  def "should transform HTML to Markdown"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Markdown Test</title></head>
                <body>
                    <h1>Markdown Heading</h1>
                    <p>This is a <strong>paragraph</strong> with formatting.</p>
                    <ul>
                        <li>Item 1</li>
                        <li>Item 2</li>
                        <li>Item 3</li>
                    </ul>
                    <p>Here's a <a href="https://example.com">link</a> to a website.</p>
                    <blockquote>
                        <p>This is a blockquote.</p>
                    </blockquote>
                    <pre><code>function test() {
  console.log("Hello, world!");
}</code></pre>
                </body>
            </html>
        ''')
    def outputFile = tempDir.resolve('output.md').toFile()

    when:
    def exitCode = executeCommand(
        'transform',
        htmlFile.absolutePath,
        outputFile.absolutePath,
        '--format=markdown'
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text

    // Title became heading
    content.contains('# Markdown Test')

    // Heading preserved
    content.contains('# Markdown Heading')

    // Paragraph with formatting
    content.contains('This is a **paragraph** with formatting')

    // List items preserved
    content.contains('* Item 1')
    content.contains('* Item 2')
    content.contains('* Item 3')

    // Link preserved
    content.contains('[link](https://example.com)')

    // Blockquote preserved
    content.contains('> This is a blockquote')

    // Code block preserved
    content.contains('```')
    content.contains('function test()')
    content.contains('```')
  }

  @Timeout(10)
  def "should transform HTML to plain text"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Plain Text Test</title></head>
                <body>
                    <h1>Plain Text Heading</h1>
                    <p>This is a <strong>paragraph</strong> with formatting.</p>
                    <ul>
                        <li>Item 1</li>
                        <li>Item 2</li>
                    </ul>
                    <p>Here's a <a href="https://example.com">link</a> to a website.</p>
                </body>
            </html>
        ''')
    def outputFile = tempDir.resolve('output.txt').toFile()

    when:
    def exitCode = executeCommand(
        'transform',
        htmlFile.absolutePath,
        outputFile.absolutePath,
        '--format=plain'
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text

    // Title preserved (usually upper case in plain text)
    content.contains('PLAIN TEXT TEST')

    // Heading preserved
    content.contains('PLAIN TEXT HEADING')

    // Basic text content preserved (but no markup)
    content.contains('This is a paragraph with formatting')

    // List items preserved (often with dashes or bullet points)
    content.contains('Item 1')
    content.contains('Item 2')

    // Link preserved (with URL in brackets or similar notation)
    content.contains('link')
    content.contains('https://example.com')
  }

  @Timeout(10)
  def "should transform HTML to JSON"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>JSON Test</title></head>
                <body>
                    <h1>JSON Heading</h1>
                    <p>This is a paragraph.</p>
                    <p>This is another paragraph.</p>
                </body>
            </html>
        ''')
    def outputFile = tempDir.resolve('output.json').toFile()

    when:
    def exitCode = executeCommand(
        'transform',
        htmlFile.absolutePath,
        outputFile.absolutePath,
        '--format=json'
    )

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)

    // Title preserved in JSON structure
    json.title == 'JSON Test'

    // Content array with structured data
    json.content.size() >= 3

    // Heading preserved in JSON structure
    json.content.find { it.type == 'heading' && it.text == 'JSON Heading' }

    // Paragraphs preserved in JSON structure
    json.content.findAll { it.type == 'paragraph' }.size() >= 2
    json.content.find { it.type == 'paragraph' && it.text == 'This is a paragraph.' }
    json.content.find { it.type == 'paragraph' && it.text == 'This is another paragraph.' }
  }

  @Timeout(10)
  def "should transform HTML with all formatting options"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Format Options Test</title></head>
                <body>
                    <h1>Heading</h1>
                    <p>Here's a <a href="https://example.com">link</a> and an <img src="test.jpg" alt="test image"> image.</p>
                </body>
            </html>
        ''')
    def mdOutputWithLinks = tempDir.resolve('with-links.md').toFile()
    def mdOutputWithoutLinks = tempDir.resolve('without-links.md').toFile()
    def mdOutputWithImages = tempDir.resolve('with-images.md').toFile()
    def mdOutputWithoutImages = tempDir.resolve('without-images.md').toFile()

    when:
    def exitCode1 = executeCommand(
        'transform',
        htmlFile.absolutePath,
        mdOutputWithLinks.absolutePath,
        '--format=markdown',
        '--preserve-links=true'
    )

    def exitCode2 = executeCommand(
        'transform',
        htmlFile.absolutePath,
        mdOutputWithoutLinks.absolutePath,
        '--format=markdown',
        '--preserve-links=false'
    )

    def exitCode3 = executeCommand(
        'transform',
        htmlFile.absolutePath,
        mdOutputWithImages.absolutePath,
        '--format=markdown',
        '--include-images=true'
    )

    def exitCode4 = executeCommand(
        'transform',
        htmlFile.absolutePath,
        mdOutputWithoutImages.absolutePath,
        '--format=markdown',
        '--include-images=false'
    )

    then:
    exitCode1 == 0
    exitCode2 == 0
    exitCode3 == 0
    exitCode4 == 0

    mdOutputWithLinks.exists()
    mdOutputWithoutLinks.exists()
    mdOutputWithImages.exists()
    mdOutputWithoutImages.exists()

    mdOutputWithLinks.text.contains('[link](https://example.com)')
    !mdOutputWithoutLinks.text.contains('[link](https://example.com)')

    mdOutputWithImages.text.contains('![test image](test.jpg)')
    !mdOutputWithoutImages.text.contains('![test image](test.jpg)')
  }

  @Timeout(10)
  def "should handle pretty-printing when requested"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Pretty Print Test</title></head>
                <body>
                    <h1>Heading</h1>
                    <p>Simple paragraph</p>
                </body>
            </html>
        ''')
    def outputFile = tempDir.resolve('pretty.json').toFile()

    when:
    def exitCode = executeCommand(
        'transform',
        htmlFile.absolutePath,
        outputFile.absolutePath,
        '--format=json',
        '--pretty'
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.count('\n') > 0  // Pretty JSON has line breaks
  }

  @Timeout(10)
  def "should handle non-existent input file gracefully"() {
    given:
    def nonExistentFile = tempDir.resolve('non-existent.html').toFile()
    def outputFile = tempDir.resolve('output.md').toFile()

    when:
    def exitCode = executeCommand(
        'transform',
        nonExistentFile.absolutePath,
        outputFile.absolutePath
    )

    then:
    exitCode == 1
    !outputFile.exists()
  }

  @Timeout(10)
  def "should throw error for invalid format"() {
    given:
    def htmlFile = createTempHtmlFile('<html><body><p>Test</p></body></html>')
    def outputFile = tempDir.resolve('invalid.out').toFile()

    when:
    def exitCode = executeCommand(
        'transform',
        htmlFile.absolutePath,
        outputFile.absolutePath,
        '--format=invalid'
    )

    then:
    exitCode == 1
    !outputFile.exists()
  }

  @Timeout(10)
  def "should handle non-ASCII characters correctly"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Internationalization Test</title></head>
                <body>
                    <h1>Привіт світе</h1>
                    <p>你好世界</p>
                    <p>こんにちは世界</p>
                </body>
            </html>
        ''')
    def mdOutput = tempDir.resolve('i18n.md').toFile()
    def jsonOutput = tempDir.resolve('i18n.json').toFile()

    when:
    def exitCode1 = executeCommand(
        'transform',
        htmlFile.absolutePath,
        mdOutput.absolutePath,
        '--format=markdown'
    )

    def exitCode2 = executeCommand(
        'transform',
        htmlFile.absolutePath,
        jsonOutput.absolutePath,
        '--format=json'
    )

    then:
    exitCode1 == 0
    exitCode2 == 0

    mdOutput.exists()
    jsonOutput.exists()

    def mdContent = mdOutput.text
    def jsonContent = jsonOutput.text

    mdContent.contains('# Привіт світе')
    mdContent.contains('你好世界')
    mdContent.contains('こんにちは世界')

    jsonContent.contains('Привіт світе')
    jsonContent.contains('你好世界')
    jsonContent.contains('こんにちは世界')

    !jsonContent.contains('\\u')  // No Unicode escape sequences
  }

  private File createTempHtmlFile(String content) {
    def file = Files.createFile(tempDir.resolve('test.html')).toFile()
    file.setText(content, 'UTF-8')
    return file
  }

  private int executeCommand(String... args) {
    def app = new WebPageAnalyzer()
    return new CommandLine(app).execute(args)
  }
}
