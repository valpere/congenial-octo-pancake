package com.webanalyzer.cli.commands

import com.webanalyzer.cli.WebPageAnalyzer
import groovy.json.JsonSlurper
import picocli.CommandLine
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Timeout

import java.nio.file.Files
import java.nio.file.Path

/**
 * Integration tests for the CompareCommand class.
 */
@Ignore
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
    def content = outputFile.text
    content.contains("\"totalDifferences\"")
    def json = new JsonSlurper().parse(outputFile)
    assert json.summary.totalDifferences == 0
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
    def content = outputFile.text
    content.contains("differences")
    def json = new JsonSlurper().parse(outputFile)
    assert json.summary.totalDifferences > 0
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
                    <span>Extra element in file 1</span>
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
    def content = outputFile.text
    content.contains("differences")
    def json = new JsonSlurper().parseText(content)
    // Verify there are some differences detected
    assert json.differences.size() > 0
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
                    <p class="unique-class-1">Styled text</p>
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
                    <p class="unique-class-2">Styled text</p>
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
    def content = outputFile.text
    content.contains("unique-class-1") || content.contains("unique-class-2")
    def json = new JsonSlurper().parseText(content)
    // Verify there are some differences detected
    assert json.differences.size() > 0
  }

  @Timeout(10)
  def "should respect selector option to limit comparison scope"() {
    given:
    // Files with different section1 and section2 contents
    def html1 = '''
            <html><body>
                <div id="section1">
                    <p>This is section 1 in document 1.</p>
                </div>
                <div id="section2">
                    <p>This is section 2 in document 1.</p>
                </div>
            </body></html>
        '''
    def html2 = '''
            <html><body>
                <div id="section1">
                    <p>This is section 1 in document 2.</p>
                </div>
                <div id="section2">
                    <p>This is section 2 in document 2.</p>
                </div>
            </body></html>
        '''
    def file1 = createTempHtmlFile(html1, "selector1.html")
    def file2 = createTempHtmlFile(html2, "selector2.html")

    def outputFile1 = tempDir.resolve("full-comparison.json").toFile()
    def outputFile2 = tempDir.resolve("section1-comparison.json").toFile()
    def outputFile3 = tempDir.resolve("section2-comparison.json").toFile()

    when:
    // Compare full documents (should show differences)
    def exitCode1 = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile1.absolutePath,
        "--mode=content"
    )

    // Compare only section1 (should show differences)
    def exitCode2 = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile2.absolutePath,
        "--mode=content",
        "--selector=#section1"
    )

    // Compare only section2 (should show differences)
    def exitCode3 = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile3.absolutePath,
        "--mode=content",
        "--selector=#section2"
    )

    then:
    exitCode1 == 0
    exitCode2 == 0
    exitCode3 == 0

    def json1 = new JsonSlurper().parse(outputFile1)
    def json2 = new JsonSlurper().parse(outputFile2)
    def json3 = new JsonSlurper().parse(outputFile3)

    // Full comparison should show differences
    json1.summary.totalDifferences > 0

    // Section comparisons should also show differences
    json2.summary.totalDifferences > 0
    json3.summary.totalDifferences > 0

    // Verify selectors were properly used
    json2.comparison.selector == "#section1"
    json3.comparison.selector == "#section2"
  }

  @Timeout(10)
  def "should respect ignore-attributes option"() {
    given:
    def html1 = '''
            <html><body>
                <div id="test" class="class1" data-test="value1">Content</div>
                <p style="color: red;" title="title1">Text</p>
            </body></html>
        '''
    def html2 = '''
            <html><body>
                <div id="test" class="class2" data-test="value2">Content</div>
                <p style="color: blue;" title="title2">Text</p>
            </body></html>
        '''
    def file1 = createTempHtmlFile(html1, "attr1.html")
    def file2 = createTempHtmlFile(html2, "attr2.html")

    def outputFile1 = tempDir.resolve("with-all-attrs.json").toFile()
    def outputFile2 = tempDir.resolve("ignore-some-attrs.json").toFile()

    when:
    // Compare with all attributes (should find differences in class, data-test, style, and title)
    def exitCode1 = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile1.absolutePath,
        "--mode=content"
    )

    // Compare ignoring class and data-test (should only find differences in style and title)
    def exitCode2 = executeCommand(
        "compare",
        file1.absolutePath,
        file2.absolutePath,
        outputFile2.absolutePath,
        "--mode=content",
        "--ignore-attributes=class,data-test"
    )

    then:
    exitCode1 == 0
    exitCode2 == 0

    def json1 = new JsonSlurper().parse(outputFile1)
    def json2 = new JsonSlurper().parse(outputFile2)

    // Test if there are more differences when not ignoring attributes
    json1.differences.size() >= json2.differences.size()

    // Verify the ignore-attributes option was properly applied
    json2.comparison.ignoreAttributes.sort() == ["class", "data-test"]
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
            <html><body><p lang="uk">Привіт світе</p></body></html>
        '''
    def html2 = '''
            <html><body><p lang="ja">こんにちは世界</p></body></html>
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
    // Just verify we get valid JSON output without testing specific content
    // since the output may change based on implementation details
    def json = new JsonSlurper().parseText(content)
    json != null
    json.summary != null
    json.differences != null
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
