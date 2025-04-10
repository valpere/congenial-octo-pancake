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
 * Integration tests for the CompareCommand class.
 *
 * These tests verify the end-to-end execution of the compare command,
 * which identifies differences between HTML documents.
 */
class CompareCommandIntegrationTest extends Specification {

  @TempDir
  Path tempDir

  @Timeout(10)
  def "should compare identical documents with no differences"() {
    given:
    def html = '''
            <html>
                <head><title>Identical Test</title></head>
                <body>
                    <h1>Page Title</h1>
                    <p>This content is identical in both files.</p>
                </body>
            </html>
        '''
    def file1 = createTempHtmlFile(html, "identical1.html")
    def file2 = createTempHtmlFile(html, "identical2.html")
    def outputFile = tempDir.resolve("comparison.json").toFile()

    when:
    def exitCode = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile.absolutePath
    )

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)
    json.summary.totalDifferences == 0
    json.differences.isEmpty()
  }

  @Timeout(10)
  def "should detect content differences between documents"() {
    given:
    def html1 = '''
            <html>
                <head><title>Content Test</title></head>
                <body>
                    <h1>Page Title</h1>
                    <p>This is version 1 of the content.</p>
                </body>
            </html>
        '''
    def html2 = '''
            <html>
                <head><title>Content Test</title></head>
                <body>
                    <h1>Page Title</h1>
                    <p>This is version 2 of the content.</p>
                </body>
            </html>
        '''
    def file1 = createTempHtmlFile(html1, "content1.html")
    def file2 = createTempHtmlFile(html2, "content2.html")
    def outputFile = tempDir.resolve("content-diff.json").toFile()

    when:
    def exitCode = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile.absolutePath,
        "--mode=content"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)
    json.summary.totalDifferences > 0
    json.differences.find { it.type == "TextContent" } != null
  }

  @Timeout(10)
  def "should detect structural differences between documents"() {
    given:
    def html1 = '''
            <html>
                <head><title>Structure Test</title></head>
                <body>
                    <div>
                        <h1>Title</h1>
                        <p>Paragraph</p>
                    </div>
                </body>
            </html>
        '''
    def html2 = '''
            <html>
                <head><title>Structure Test</title></head>
                <body>
                    <div>
                        <h1>Title</h1>
                    </div>
                    <p>Paragraph</p>
                </body>
            </html>
        '''
    def file1 = createTempHtmlFile(html1, "structure1.html")
    def file2 = createTempHtmlFile(html2, "structure2.html")
    def outputFile = tempDir.resolve("structure-diff.json").toFile()

    when:
    def exitCode = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile.absolutePath,
        "--mode=structure"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)
    json.summary.totalDifferences > 0
    // Depending on exact implementation, we might see ElementCount differences
    json.differences.find {
      it.type == "ElementCount" || it.type == "DOMDepth" || it.type == "Structure"
    } != null
  }

  @Timeout(10)
  def "should detect visual differences between documents"() {
    given:
    def html1 = '''
            <html>
                <head>
                    <title>Visual Test</title>
                    <style>body { color: red; }</style>
                </head>
                <body>
                    <p class="styled">Styled text</p>
                </body>
            </html>
        '''
    def html2 = '''
            <html>
                <head>
                    <title>Visual Test</title>
                    <style>body { color: blue; }</style>
                </head>
                <body>
                    <p class="different">Styled text</p>
                </body>
            </html>
        '''
    def file1 = createTempHtmlFile(html1, "visual1.html")
    def file2 = createTempHtmlFile(html2, "visual2.html")
    def outputFile = tempDir.resolve("visual-diff.json").toFile()

    when:
    def exitCode = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile.absolutePath,
        "--mode=visual"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def json = new JsonSlurper().parse(outputFile)
    json.summary.totalDifferences > 0
    // Should find class differences or style differences
    json.differences.find {
      it.type == "StylesheetDifference" || it.type == "UniqueClasses" || it.type == "ClassUsage"
    } != null
  }

  @Timeout(10)
  def "should respect selector option to limit comparison scope"() {
    given:
    def html1 = '''
            <html><body>
                <div id="section1">
                    <p>This is identical in both documents</p>
                </div>
                <div id="section2">
                    <p>This is different in document 1</p>
                </div>
            </body></html>
        '''
    def html2 = '''
            <html><body>
                <div id="section1">
                    <p>This is identical in both documents</p>
                </div>
                <div id="section2">
                    <p>This is different in document 2</p>
                </div>
            </body></html>
        '''
    def file1 = createTempHtmlFile(html1, "selector1.html")
    def file2 = createTempHtmlFile(html2, "selector2.html")
    def outputFile1 = tempDir.resolve("section1-diff.json").toFile()
    def outputFile2 = tempDir.resolve("section2-diff.json").toFile()

    when:
    def exitCode1 = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile1.absolutePath,
        "--selector=#section1"
    )

    def exitCode2 = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile2.absolutePath,
        "--selector=#section2"
    )

    then:
    exitCode1 == 0
    exitCode2 == 0
    outputFile1.exists()
    outputFile2.exists()

    def json1 = new JsonSlurper().parse(outputFile1)
    def json2 = new JsonSlurper().parse(outputFile2)

    json1.summary.totalDifferences == 0  // No differences in section1
    json2.summary.totalDifferences > 0   // Differences exist in section2
  }

  @Timeout(10)
  def "should respect ignore-attributes option"() {
    given:
    def html1 = '''
            <html><body>
                <div id="container" class="main" data-test="value">Content</div>
            </body></html>
        '''
    def html2 = '''
            <html><body>
                <div id="container" class="different" data-test="changed">Content</div>
            </body></html>
        '''
    def file1 = createTempHtmlFile(html1, "attr1.html")
    def file2 = createTempHtmlFile(html2, "attr2.html")

    def outputFile1 = tempDir.resolve("with-all-attrs.json").toFile()
    def outputFile2 = tempDir.resolve("ignore-some-attrs.json").toFile()

    when:
    def exitCode1 = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile1.absolutePath
    )

    def exitCode2 = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile2.absolutePath,
        "--ignore-attributes=class,data-test"
    )

    then:
    exitCode1 == 0
    exitCode2 == 0
    outputFile1.exists()
    outputFile2.exists()

    def json1 = new JsonSlurper().parse(outputFile1)
    def json2 = new JsonSlurper().parse(outputFile2)

    json1.summary.totalDifferences > json2.summary.totalDifferences
  }

  @Timeout(10)
  def "should output comparison results in text format"() {
    given:
    def html1 = '''
            <html><body><p>Original</p></body></html>
        '''
    def html2 = '''
            <html><body><p>Changed</p></body></html>
        '''
    def file1 = createTempHtmlFile(html1, "text1.html")
    def file2 = createTempHtmlFile(html2, "text2.html")
    def outputFile = tempDir.resolve("comparison.txt").toFile()

    when:
    def exitCode = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile.absolutePath,
        "--format=txt"
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("HTML Comparison Results")
    content.contains("Comparison Details:")
    content.contains("Summary:")
    content.contains("Differences:")
  }

  @Timeout(10)
  def "should handle non-existent input files gracefully"() {
    given:
    def existingFile = createTempHtmlFile("<html><body><p>Test</p></body></html>", "existing.html")
    def nonExistentFile = tempDir.resolve("non-existent.html").toFile()
    def outputFile = tempDir.resolve("error.json").toFile()

    when:
    def exitCode = executeCommand(
        "compare",
        existingFile.absolutePath,
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
    def html1 = '''
            <html><body><p>Привет мир</p></body></html>
        '''
    def html2 = '''
            <html><body><p>こんにちは世界</p></body></html>
        '''
    def file1 = createTempHtmlFile(html1, "i18n1.html")
    def file2 = createTempHtmlFile(html2, "i18n2.html")
    def outputFile = tempDir.resolve("i18n-diff.json").toFile()

    when:
    def exitCode = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile.absolutePath
    )

    then:
    exitCode == 0
    outputFile.exists()
    def content = outputFile.text
    content.contains("Привет мир")
    content.contains("こんにちは世界")
    !content.contains("\\u")  // No Unicode escape sequences
  }

  private File createTempHtmlFile(String content, String filename) {
    def file = Files.createFile(tempDir.resolve(filename)).toFile()
    file.setText(content, "UTF-8")
    return file
  }

  private int executeCommand(String... args) {
    def app = new WebPageAnalyzer()
    return new CommandLine(app).execute(args)
  }
}
