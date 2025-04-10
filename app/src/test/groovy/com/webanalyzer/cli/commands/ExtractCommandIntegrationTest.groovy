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
 * Integration tests for the ExtractCommand class.
 *
 * These tests verify the end-to-end execution of the extract command,
 * including element extraction using CSS selectors and various output formats.
 */
class ExtractCommandIntegrationTest extends Specification {

  @TempDir
  Path tempDir

  @Timeout(10)
  def "should extract elements using CSS selector and output as JSON"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html><body>
                <div class="product">
                    <h2>Product 1</h2>
                    <p class="price">$19.99</p>
                </div>
                <div class="product">
                    <h2>Product 2</h2>
                    <p class="price">$29.99</p>
                </div>
            </body></html>
        ''')
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand("extract", htmlFile.absolutePath, "div.product", outputFile.absolutePath)

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)
    json.size() == 2
    json[0].text.contains("Product 1")
    json[0].text.contains('$19.99')
    json[1].text.contains('Product 2')
    json[1].text.contains('$29.99')
  }

  @Timeout(10)
  def "should extract elements with specified attributes"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html><body>
                <a href="https://example.com" title="Example" rel="nofollow" class="external">Link 1</a>
                <a href="https://test.com" title="Test" rel="nofollow" class="external">Link 2</a>
            </body></html>
        ''')
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand(
        "extract",
        htmlFile.absolutePath,
        "a.external",
        outputFile.absolutePath,
        "--attributes=href,title"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)
    json.size() == 2
    json[0].href == "https://example.com"
    json[0].title == "Example"
    !json[0].containsKey("rel")  // Should not include attributes that weren't specified
    json[1].href == "https://test.com"
    json[1].title == "Test"
  }

  @Timeout(10)
  def "should extract elements and output as CSV"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html><body>
                <table>
                    <tr>
                        <td data-id="1" data-name="First">Cell 1</td>
                        <td data-id="2" data-name="Second">Cell 2</td>
                    </tr>
                </table>
            </body></html>
        ''')
    def outputFile = tempDir.resolve("output.csv").toFile()

    when:
    def exitCode = executeCommand(
        "extract",
        htmlFile.absolutePath,
        "td",
        outputFile.absolutePath,
        "--format=csv"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def lines = outputFile.readLines()
    lines.size() > 1
    lines[0].split(",").contains("text")
    lines[0].split(",").contains("data-id")
    lines[0].split(",").contains("data-name")
    lines[1].contains("Cell 1")
    lines[1].contains("1")
    lines[1].contains("First")
    lines[2].contains("Cell 2")
    lines[2].contains("2")
    lines[2].contains("Second")
  }

  @Timeout(10)
  def "should extract elements and output as text"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html><body>
                <article>
                    <h1>Article Title</h1>
                    <p>Article content.</p>
                </article>
            </body></html>
        ''')
    def outputFile = tempDir.resolve("output.txt").toFile()

    when:
    def exitCode = executeCommand(
        "extract",
        htmlFile.absolutePath,
        "article",
        outputFile.absolutePath,
        "--format=txt"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("TEXT: Article Title")
    content.contains("Article content.")
  }

  @Timeout(10)
  def "should include HTML content when requested"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html><body>
                <div class="container">
                    <p>This is <strong>formatted</strong> content.</p>
                </div>
            </body></html>
        ''')
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand(
        "extract",
        htmlFile.absolutePath,
        "div.container",
        outputFile.absolutePath,
        "--include-html=true"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)
    json[0].html.contains("<p>This is <strong>formatted</strong> content.</p>")
  }

  @Timeout(10)
  def "should handle pretty-printing JSON when requested"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html><body>
                <p>Simple paragraph</p>
            </body></html>
        ''')
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand(
        "extract",
        htmlFile.absolutePath,
        "p",
        outputFile.absolutePath,
        "--pretty"
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
    def nonExistentFile = tempDir.resolve("non-existent.html").toFile()
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand(
        "extract",
        nonExistentFile.absolutePath,
        "p",
        outputFile.absolutePath
    )

    then:
    exitCode == 1
    !outputFile.exists()
  }

  @Timeout(10)
  def "should handle non-ASCII characters correctly"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html><body>
                <p lang="ru">Привіт світе</p>
                <p lang="zh">你好世界</p>
            </body></html>
        ''')
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand(
        "extract",
        htmlFile.absolutePath,
        "p",
        outputFile.absolutePath
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("Привіт світе")
    content.contains("你好世界")
    !content.contains("\\u")  // No Unicode escape sequences
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
