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
 * Integration tests for the ParseCommand class.
 *
 * These tests verify the end-to-end execution of the parse command,
 * including parsing HTML files into JSON with various options.
 */
class ParseCommandIntegrationTest extends Specification {

  @TempDir
  Path tempDir

  @Timeout(10)
  // Timeout in seconds
  def "should parse HTML file to JSON using command line"() {
    given:
    def htmlFile = createTempHtmlFile("<html><body><h1>Test Document</h1><p>Hello, world!</p></body></html>")
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand("parse", htmlFile.absolutePath, outputFile.absolutePath)

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)
    json.tagName == "html"
    json.children.find { it.tagName == "body" }
    json.children[0].children.find { it.tagName == "h1" && it.text == "Test Document" }
    json.children[0].children.find { it.tagName == "p" && it.text == "Hello, world!" }
  }

  @Timeout(10)
  def "should parse HTML file with non-ASCII characters"() {
    given:
    def htmlFile = createTempHtmlFile("<html><body><p>Привіт світе</p><p>你好世界</p></body></html>")
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand("parse", htmlFile.absolutePath, outputFile.absolutePath)

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("Привіт світе")
    content.contains("你好世界")
    !content.contains("\\u")  // Should not contain Unicode escape sequences
  }

  @Timeout(10)
  def "should parse HTML file with --pretty option"() {
    given:
    def htmlFile = createTempHtmlFile("<html><body><p>Hello</p></body></html>")
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand("parse", htmlFile.absolutePath, outputFile.absolutePath, "--pretty")

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.count('\n') > 0  // Pretty-printed JSON has line breaks
  }

  @Timeout(10)
  def "should parse HTML file with --include-text=false option"() {
    given:
    def htmlFile = createTempHtmlFile("<html><body><p>Hello</p></body></html>")
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand("parse", htmlFile.absolutePath, outputFile.absolutePath, "--include-text=false")

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    !content.contains('"text"')  // No text content should be included
  }

  @Timeout(10)
  def "should handle non-existent input file gracefully"() {
    given:
    def nonExistentFile = tempDir.resolve("non-existent.html").toFile()
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand("parse", nonExistentFile.absolutePath, outputFile.absolutePath)

    then:
    exitCode == 1
    !outputFile.exists()
  }

  @Timeout(10)
  def "should handle custom encoding option"() {
    given:
    def htmlFile = createTempHtmlFile("<html><body><p>Test with encoding</p></body></html>")
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand("parse", htmlFile.absolutePath, outputFile.absolutePath, "--encoding=UTF-8")

    then:
    exitCode == 0
    outputFile.exists()
    outputFile.text.contains("Test with encoding")
  }

  private File createTempHtmlFile(String content) {
    def file = Files.createFile(tempDir.resolve("test.html")).toFile()
    file.setText(content, "UTF-8")
    return file
  }

  private int executeCommand(String... args) {
    def app = new WebPageAnalyzer()
    return new CommandLine(app).execute(args)
  }
}
