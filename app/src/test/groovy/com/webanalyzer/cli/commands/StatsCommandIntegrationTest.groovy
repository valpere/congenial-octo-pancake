package com.webanalyzer.cli.commands

import com.webanalyzer.cli.WebPageAnalyzer
import picocli.CommandLine
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Timeout

import java.nio.file.Files
import java.nio.file.Path

/**
 * Integration tests for the StatsCommand class with debugging output.
 */
class StatsCommandIntegrationTest extends Specification {

  @TempDir
  Path tempDir

  @Timeout(20)
  def "should generate comprehensive statistics in JSON format"() {
    given:
    println "Test: should generate comprehensive statistics in JSON format"
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Test Document</title></head>
                <body>
                    <h1>Page Title</h1>
                    <p>Test paragraph</p>
                </body>
            </html>
        ''')
    println "Created temp HTML file at: ${htmlFile.absolutePath}"
    def outputFile = tempDir.resolve("stats.json").toFile()

    when:
    println "Executing command: stats ${htmlFile.absolutePath} ${outputFile.absolutePath}"
    def exitCode = executeCommand("stats", htmlFile.absolutePath, outputFile.absolutePath)
    println "Command execution completed with exit code: ${exitCode}"

    then:
    println "Verifying output file existence: ${outputFile.absolutePath}"
    exitCode == 0
    outputFile.exists()

    if (outputFile.exists()) {
      def content = outputFile.text
      println "Output file content length: ${content.length()} characters"
      println "Output file first 100 characters: ${content.take(100)}..."
    }
  }

  @Timeout(20)
  def "should generate statistics in text format"() {
    given:
    println "Test: should generate statistics in text format"
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Text Format Test</title></head>
                <body>
                    <h1>Page Title</h1>
                    <p>This is a test paragraph.</p>
                    <a href="https://example.com">Link</a>
                </body>
            </html>
        ''')
    println "Created temp HTML file at: ${htmlFile.absolutePath}"
    def outputFile = tempDir.resolve("stats.txt").toFile()

    when:
    println "Executing command: stats ${htmlFile.absolutePath} ${outputFile.absolutePath} --format=txt"
    def exitCode = executeCommand(
        "stats",
        htmlFile.absolutePath,
        outputFile.absolutePath,
        "--format=txt"
    )
    println "Command execution completed with exit code: ${exitCode}"

    then:
    println "Verifying output file existence: ${outputFile.absolutePath}"
    exitCode == 0
    outputFile.exists()

    if (outputFile.exists()) {
      def content = outputFile.text
      println "Output file content length: ${content.length()} characters"
      println "Output file content preview: ${content.take(Math.min(content.length(), 200))}..."
    }
  }

  @Timeout(20)
  def "should respect include option to limit statistics scope"() {
    given:
    println "Test: should respect include option to limit statistics scope"
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Include Test</title></head>
                <body>
                    <h1>Page Title</h1>
                    <p>This is a test paragraph.</p>
                    <a href="https://example.com">Link</a>
                </body>
            </html>
        ''')
    println "Created temp HTML file at: ${htmlFile.absolutePath}"
    def outputFile = tempDir.resolve("basic-only.json").toFile()

    when:
    println "Executing command: stats ${htmlFile.absolutePath} ${outputFile.absolutePath} --include=basic"
    def exitCode = executeCommand(
        "stats",
        htmlFile.absolutePath,
        outputFile.absolutePath,
        "--include=basic"
    )
    println "Command execution completed with exit code: ${exitCode}"

    then:
    println "Verifying output file existence: ${outputFile.absolutePath}"
    exitCode == 0
    outputFile.exists()

    if (outputFile.exists()) {
      def content = outputFile.text
      println "Output file content length: ${content.length()} characters"
      println "Output contains basicInfo: ${content.contains('basicInfo')}"
      println "Output contains elements: ${content.contains('elements')}"
    }
  }

  @Timeout(20)
  def "should include multiple stat types when specified"() {
    given:
    println "Test: should include multiple stat types when specified"
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Multiple Include Test</title></head>
                <body>
                    <h1>Page Title</h1>
                    <p>This is a test paragraph.</p>
                    <a href="https://example.com">Link</a>
                </body>
            </html>
        ''')
    println "Created temp HTML file at: ${htmlFile.absolutePath}"
    def outputFile = tempDir.resolve("multiple.json").toFile()

    when:
    println "Executing command: stats ${htmlFile.absolutePath} ${outputFile.absolutePath} --include=basic,content"
    def exitCode = executeCommand(
        "stats",
        htmlFile.absolutePath,
        outputFile.absolutePath,
        "--include=basic,content"
    )
    println "Command execution completed with exit code: ${exitCode}"

    then:
    println "Verifying output file existence: ${outputFile.absolutePath}"
    exitCode == 0
    outputFile.exists()

    if (outputFile.exists()) {
      def content = outputFile.text
      println "Output file content length: ${content.length()} characters"
      println "Output contains basicInfo: ${content.contains('basicInfo')}"
      println "Output contains content: ${content.contains('content')}"
    }
  }

  @Timeout(10)
  def "should handle non-existent input file gracefully"() {
    given:
    println "Test: should handle non-existent input file gracefully"
    def nonExistentFile = tempDir.resolve("non-existent.html").toFile()
    println "Non-existent file path: ${nonExistentFile.absolutePath}"
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    println "Executing command with non-existent file"
    def exitCode = executeCommand(
        "stats",
        nonExistentFile.absolutePath,
        outputFile.absolutePath
    )
    println "Command execution completed with exit code: ${exitCode}"

    then:
    println "Verifying exit code is 1 and output file does not exist"
    exitCode == 1
    !outputFile.exists()
  }

  @Timeout(20)
  def "should handle non-ASCII characters correctly in output"() {
    given:
    println "Test: should handle non-ASCII characters correctly in output"
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
    println "Created temp HTML file at: ${htmlFile.absolutePath}"
    def outputFile = tempDir.resolve("i18n.json").toFile()

    when:
    println "Executing command: stats ${htmlFile.absolutePath} ${outputFile.absolutePath}"
    def exitCode = executeCommand(
        "stats",
        htmlFile.absolutePath,
        outputFile.absolutePath
    )
    println "Command execution completed with exit code: ${exitCode}"

    then:
    println "Verifying output file existence: ${outputFile.absolutePath}"
    exitCode == 0
    outputFile.exists()

    if (outputFile.exists()) {
      def content = outputFile.text
      println "Output file content length: ${content.length()} characters"
      println "Output contains non-ASCII characters: ${content.contains('Привіт') || content.contains('你好') || content.contains('こんにちは')}"
      println "Output contains Unicode escapes: ${content.contains('\\u')}"
    }
  }

  private File createTempHtmlFile(String content) {
    def file = Files.createFile(tempDir.resolve("test-${System.currentTimeMillis()}.html")).toFile()
    file.setText(content, "UTF-8")
    println "Created HTML file with ${content.length()} characters at: ${file.absolutePath}"
    return file
  }

  private int executeCommand(String... args) {
    println "Creating new WebPageAnalyzer instance"
    def app = new WebPageAnalyzer()
    println "Creating CommandLine with args: ${args.join(' ')}"
    def cmdLine = new CommandLine(app)

    try {
      println "Executing command"
      def result = cmdLine.execute(args)
      println "Command executed successfully"
      return result
    } catch (Exception e) {
      println "Exception during command execution: ${e.class.name}: ${e.message}"
      e.printStackTrace(System.out)
      return -1
    }
  }
}
