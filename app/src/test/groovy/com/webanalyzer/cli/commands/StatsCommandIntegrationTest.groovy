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
 * Integration tests for the StatsCommand class.
 *
 * These tests verify the end-to-end execution of the stats command,
 * which generates statistics about HTML documents.
 */
class StatsCommandIntegrationTest extends Specification {

  @TempDir
  Path tempDir

  @Timeout(10)
  def "should generate comprehensive statistics in JSON format"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <!DOCTYPE html>
            <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="description" content="Test page">
                    <title>Test Document</title>
                    <link rel="stylesheet" href="styles.css">
                    <script src="script.js"></script>
                </head>
                <body>
                    <header>
                        <h1>Page Title</h1>
                        <nav>
                            <ul>
                                <li><a href="/">Home</a></li>
                                <li><a href="/about">About</a></li>
                                <li><a href="/contact">Contact</a></li>
                            </ul>
                        </nav>
                    </header>
                    <main>
                        <section>
                            <h2>Section 1</h2>
                            <p>This is the first paragraph with some text.</p>
                            <p>This is the second paragraph with more text.</p>
                        </section>
                        <section>
                            <h2>Section 2</h2>
                            <ul>
                                <li>Item 1</li>
                                <li>Item 2</li>
                                <li>Item 3</li>
                            </ul>
                        </section>
                        <aside>
                            <h3>Related Links</h3>
                            <a href="https://example.com">External Link</a>
                            <a href="/internal">Internal Link</a>
                        </aside>
                    </main>
                    <footer>
                        <p>Copyright 2023</p>
                    </footer>
                </body>
            </html>
        ''')
    def outputFile = tempDir.resolve("stats.json").toFile()

    when:
    def exitCode = executeCommand("stats", htmlFile.absolutePath, outputFile.absolutePath)

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)

    // Verify basic info is present
    json.basicInfo.title == "Test Document"
    json.basicInfo.language == "en"
    json.basicInfo.metadata.description == "Test page"

    // Verify elements analysis is present
    json.elements.totalElements > 0
    json.elements.elementsByTag.h1 == 1
    json.elements.elementsByTag.h2 == 2
    json.elements.elementsByTag.p >= 3
    json.elements.elementsByTag.a >= 5
    json.elements.elementsByTag.li >= 6

    // Verify links analysis is present
    json.links.totalLinks >= 5
    json.links.linkTypes.external >= 1
    json.links.linkTypes.internal >= 4

    // Verify structure analysis is present
    json.structure.maxDOMDepth > 0
    json.structure.sections == 2
    json.structure.headers == 1
    json.structure.footers == 1
    json.structure.navs == 1

    // Verify content analysis is present
    json.content.textLength > 0
    json.content.wordCount > 0
    json.content.headings.h1 == 1
    json.content.headings.h2 == 2
    json.content.headings.h3 == 1
    json.content.unorderedLists == 2
    json.content.listItems >= 6
  }

  @Timeout(10)
  def "should generate statistics in text format"() {
    given:
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
    def outputFile = tempDir.resolve("stats.txt").toFile()

    when:
    def exitCode = executeCommand(
        "stats",
        htmlFile.absolutePath,
        outputFile.absolutePath,
        "--format=txt"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("Web Page Statistics")
    content.contains("Text Format Test")
    content.contains("Element Counts:")
    content.contains("Link Analysis:")
    // Text format should be readable and structured
    content.contains("h1")
    content.contains("Page Title")
  }

  @Timeout(10)
  def "should respect include option to limit statistics scope"() {
    given:
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
    def outputFile = tempDir.resolve("basic-only.json").toFile()

    when:
    def exitCode = executeCommand(
        "stats",
        htmlFile.absolutePath,
        outputFile.absolutePath,
        "--include=basic"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)

    // Should include only basic info
    json.containsKey("basicInfo")
    json.basicInfo.title == "Include Test"

    // Should not include other sections
    !json.containsKey("elements")
    !json.containsKey("links")
    !json.containsKey("structure")
    !json.containsKey("content")
    !json.containsKey("performance")
  }

  @Timeout(10)
  def "should include multiple stat types when specified"() {
    given:
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
    def outputFile = tempDir.resolve("multiple.json").toFile()

    when:
    def exitCode = executeCommand(
        "stats",
        htmlFile.absolutePath,
        outputFile.absolutePath,
        "--include=basic,links,content"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)

    // Should include specified sections
    json.containsKey("basicInfo")
    json.containsKey("links")
    json.containsKey("content")

    // Should not include other sections
    !json.containsKey("elements")
    !json.containsKey("structure")
    !json.containsKey("performance")
  }

  @Timeout(10)
  def "should handle non-existent input file gracefully"() {
    given:
    def nonExistentFile = tempDir.resolve("non-existent.html").toFile()
    def outputFile = tempDir.resolve("output.json").toFile()

    when:
    def exitCode = executeCommand(
        "stats",
        nonExistentFile.absolutePath,
        outputFile.absolutePath
    )

    then:
    exitCode == 1
    !outputFile.exists()
  }

  @Timeout(10)
  def "should handle non-ASCII characters correctly in output"() {
    given:
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Internationalization Test</title></head>
                <body>
                    <h1>Привет мир</h1>
                    <p>你好世界</p>
                    <p>こんにちは世界</p>
                </body>
            </html>
        ''')
    def outputFile = tempDir.resolve("i18n.json").toFile()

    when:
    def exitCode = executeCommand(
        "stats",
        htmlFile.absolutePath,
        outputFile.absolutePath
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("Привет мир")
    content.contains("你好世界")
    content.contains("こんにちは世界")
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
