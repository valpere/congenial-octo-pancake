package com.webanalyzer.cli.commands

import com.webanalyzer.cli.WebPageAnalyzer
import picocli.CommandLine
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Timeout

import java.nio.file.Path

/**
 * Integration tests for the ReadCommand class.
 *
 * These tests verify the end-to-end execution of the read command,
 * including fetching web pages both statically and dynamically.
 */
class ReadCommandIntegrationTest extends Specification {

  @TempDir
  Path tempDir

  @Timeout(60)
  // This test involves network I/O
  def "should fetch static web page using read command"() {
    given:
    def outputFile = tempDir.resolve("example.html").toFile()

    when:
    def exitCode = executeCommand("read", "https://example.com", outputFile.absolutePath)

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("<h1>Example Domain</h1>")
  }

  @Timeout(60)
  def "should fetch dynamic web page using read command with --dynamic option"() {
    given:
    def outputFile = tempDir.resolve("dynamic.html").toFile()

    when:
    def exitCode = executeCommand("read", "https://www.google.com", outputFile.absolutePath, "--dynamic", "--wait=3000")

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("<html")
    content.contains("</html>")
    content.contains("google")
  }

  @Timeout(30)
  def "should handle invalid URL gracefully"() {
    given:
    def outputFile = tempDir.resolve("error.html").toFile()

    when:
    def exitCode = executeCommand("read", "invalid-url", outputFile.absolutePath)

    then:
    exitCode == 1
    !outputFile.exists()
  }

  @Timeout(30)
  def "should apply custom user agent when specified"() {
    given:
    def outputFile = tempDir.resolve("user-agent.html").toFile()
    def customAgent = "WebPageAnalyzer-Test/1.0"

    when:
    def exitCode = executeCommand("read", "https://httpbin.org/user-agent", outputFile.absolutePath, "--user-agent=${customAgent}")

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains(customAgent)
  }

  @Timeout(30)
  def "should apply custom timeout when specified"() {
    given:
    def outputFile = tempDir.resolve("timeout.html").toFile()

    when:
    def exitCode = executeCommand("read", "https://example.com", outputFile.absolutePath, "--timeout=15")

    then:
    exitCode == 0
    outputFile.exists()
    outputFile.text.contains("Example Domain")
  }

  @Timeout(60)
  def "should handle custom headers when specified"() {
    given:
    def outputFile = tempDir.resolve("headers.html").toFile()
    def headers = "X-Test-Header=Test-Value,Accept-Language=en-US"

    when:
    def exitCode = executeCommand("read", "https://httpbin.org/headers", outputFile.absolutePath, "--headers=${headers}")

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("X-Test-Header")
    content.contains("Test-Value")
    content.contains("Accept-Language")
    content.contains("en-US")
  }

  @Timeout(60)
  def "should wait for selector when specified"() {
    given:
    def outputFile = tempDir.resolve("wait-selector.html").toFile()

    when:
    def exitCode = executeCommand(
        "read",
        "https://www.google.com",
        outputFile.absolutePath,
        "--dynamic",
        "--wait-for-selector=input[name='q']"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains('name="q"')
  }

  private int executeCommand(String... args) {
    def app = new WebPageAnalyzer()
    return new CommandLine(app).execute(args)
  }
}
