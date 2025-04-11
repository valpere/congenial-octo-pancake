package com.webanalyzer.cli.commands

import com.webanalyzer.core.comparator.ComparisonOptions
import com.webanalyzer.core.comparator.HtmlComparator
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

/**
 * Unit tests for the CompareCommand functionality.
 */
class CompareCommandTest extends Specification {

  @TempDir
  Path tempDir

  def "should compare HTML files with content mode"() {
    given:
    def html1 = "<html><body><p>Content 1</p></body></html>"
    def html2 = "<html><body><p>Content 2</p></body></html>"
    def file1 = createTempFile(html1, "file1.html")
    def file2 = createTempFile(html2, "file2.html")
    def outputFile = tempDir.resolve("output.json").toFile()

    def comparator = new HtmlComparator()
    def options = new ComparisonOptions(mode: "content")

    when:
    def result = comparator.compareFiles(file1, file2, options)
    def json = comparator.formatAsJson(result, true)
    outputFile.text = json

    then:
    result.summary.totalDifferences > 0
    outputFile.exists()
    outputFile.text.contains("differences")
  }

  def "should compare HTML files with structure mode"() {
    given:
    def html1 = "<html><body><div><p>Content</p></div></body></html>"
    def html2 = "<html><body><p>Content</p></body></html>"
    def file1 = createTempFile(html1, "file1.html")
    def file2 = createTempFile(html2, "file2.html")
    def outputFile = tempDir.resolve("output.json").toFile()

    def comparator = new HtmlComparator()
    def options = new ComparisonOptions(mode: "structure")

    when:
    def result = comparator.compareFiles(file1, file2, options)
    def json = comparator.formatAsJson(result, true)
    outputFile.text = json

    then:
    result.summary.totalDifferences > 0
    outputFile.exists()
    outputFile.text.contains("differences")
  }

  def "should respect selector option"() {
    given:
    def html1 = '''
        <html><body>
            <div id="section1"><p>Same content</p></div>
            <div id="section2"><p>Different 1</p></div>
        </body></html>
    '''
    def html2 = '''
        <html><body>
            <div id="section1"><p>Same content</p></div>
            <div id="section2"><p>Different 2</p></div>
        </body></html>
    '''
    def file1 = createTempFile(html1, "file1.html")
    def file2 = createTempFile(html2, "file2.html")

    def comparator = new HtmlComparator()
    def optionsSection1 = new ComparisonOptions(mode: "content", selector: "#section1")
    def optionsSection2 = new ComparisonOptions(mode: "content", selector: "#section2")

    when:
    def resultSection1 = comparator.compareFiles(file1, file2, optionsSection1)
    def resultSection2 = comparator.compareFiles(file1, file2, optionsSection2)

    then:
    // Section1 should have fewer differences than section2
    resultSection1.summary.totalDifferences < resultSection2.summary.totalDifferences
  }

  def "should respect ignore-attributes option"() {
    given:
    def html1 = '<html><body><div class="class1" data-test="value1">Content</div></body></html>'
    def html2 = '<html><body><div class="class2" data-test="value2">Content</div></body></html>'
    def file1 = createTempFile(html1, "file1.html")
    def file2 = createTempFile(html2, "file2.html")

    def comparator = new HtmlComparator()
    def optionsWithAll = new ComparisonOptions(mode: "content", ignoreAttributes: [])
    def optionsIgnoring = new ComparisonOptions(mode: "content", ignoreAttributes: ["class", "data-test"])

    when:
    def resultWithAll = comparator.compareFiles(file1, file2, optionsWithAll)
    def resultIgnoring = comparator.compareFiles(file1, file2, optionsIgnoring)

    then:
    // More differences when not ignoring attributes
    resultWithAll.summary.totalDifferences > resultIgnoring.summary.totalDifferences
  }

  def "should handle non-ASCII characters"() {
    given:
    def html1 = '<html><body><p>Привіт світе</p></body></html>'
    def html2 = '<html><body><p>こんにちは世界</p></body></html>'
    def file1 = createTempFile(html1, "file1.html")
    def file2 = createTempFile(html2, "file2.html")
    def outputFile = tempDir.resolve("i18n.json").toFile()

    def comparator = new HtmlComparator()
    def options = new ComparisonOptions(mode: "content")

    when:
    def result = comparator.compareFiles(file1, file2, options)
    def json = comparator.formatAsJson(result, true)
    outputFile.text = json

    then:
    result.summary.totalDifferences > 0
    !json.contains("\\u") // No Unicode escape sequences
  }

  private File createTempFile(String content, String filename) {
    def file = tempDir.resolve(filename).toFile()
    file.text = content
    return file
  }
}
