package com.webanalyzer.core.analyzer

import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for the HtmlAnalyzer class.
 *
 * These tests verify the HTML analysis functionality that generates
 * various statistics about HTML documents.
 */
class HtmlAnalyzerTest extends Specification {

  @TempDir
  Path tempDir

  def "should analyze basic document information"() {
    given:
    def analyzer = new HtmlAnalyzer()
    def htmlFile = createTempHtmlFile('''
            <!DOCTYPE html>
            <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="description" content="Test page">
                    <title>Test Document</title>
                </head>
                <body>
                    <h1>Test Heading</h1>
                    <p>This is a paragraph.</p>
                </body>
            </html>
        ''')
    def options = new AnalyzerOptions(
        includeAll: false,
        includeBasicInfo: true
    )

    when:
    def result = analyzer.analyzeFile(htmlFile, options)

    then:
    result.containsKey('basicInfo')
    result.basicInfo.title == 'Test Document'
    result.basicInfo.language == 'en'
    result.basicInfo.charset == 'UTF-8'
    result.basicInfo.metadata.description == 'Test page'
    !result.containsKey('elements')
    !result.containsKey('links')
  }

  def "should analyze element statistics"() {
    given:
    def analyzer = new HtmlAnalyzer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <h1>Title</h1>
                    <p>Paragraph 1</p>
                    <p>Paragraph 2</p>
                    <div>
                        <img src="image.jpg" alt="Image">
                        <img src="image2.jpg">
                    </div>
                    <form>
                        <input type="text">
                        <button>Submit</button>
                    </form>
                </body>
            </html>
        ''')
    def options = new AnalyzerOptions(
        includeAll: false,
        includeElements: true
    )

    when:
    def result = analyzer.analyzeFile(htmlFile, options)

    then:
    result.containsKey('elements')
    result.elements.totalElements > 0
    result.elements.elementsByTag.h1 == 1
    result.elements.elementsByTag.p == 2
    result.elements.elementsByTag.div == 1
    result.elements.images == 2
    result.elements.inputFields == 1
    result.elements.buttons == 1
    result.elements.forms == 1
  }

  def "should analyze link statistics"() {
    given:
    def analyzer = new HtmlAnalyzer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <a href="https://example.com">External Link</a>
                    <a href="/internal">Internal Link</a>
                    <a href="#section">Anchor Link</a>
                    <a href="mailto:test@example.com">Email Link</a>
                    <a href="javascript:void(0)">JavaScript Link</a>
                </body>
            </html>
        ''')
    def options = new AnalyzerOptions(
        includeAll: false,
        includeLinks: true
    )

    when:
    def result = analyzer.analyzeFile(htmlFile, options)

    then:
    result.containsKey('links')
    result.links.totalLinks == 5
    result.links.linkTypes.external == 1
    result.links.linkTypes.internal == 1
    result.links.linkTypes.anchor == 1
    result.links.linkTypes.mailto == 1
    result.links.linkTypes.javascript == 1
    result.links.externalDomains.contains('example.com')
  }

  def "should analyze structure statistics"() {
    given:
    def analyzer = new HtmlAnalyzer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <div>
                        <div>
                            <div>
                                <p>Deeply nested paragraph</p>
                            </div>
                        </div>
                    </div>
                    <section>
                        <article>
                            <header>
                                <h1>Article Title</h1>
                            </header>
                            <p>Article content</p>
                            <footer>
                                <p>Footer content</p>
                            </footer>
                        </article>
                    </section>
                </body>
            </html>
        ''')
    def options = new AnalyzerOptions(
        includeAll: false,
        includeStructure: true
    )

    when:
    def result = analyzer.analyzeFile(htmlFile, options)

    then:
    result.containsKey('structure')
    result.structure.maxDOMDepth > 0
    result.structure.averageNestingLevel > 0
    result.structure.sections == 1
    result.structure.articles == 1
    result.structure.headers == 1
    result.structure.footers == 1
    result.structure.divs == 3
  }

  def "should analyze content statistics"() {
    given:
    def analyzer = new HtmlAnalyzer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <body>
                    <h1>Main Heading</h1>
                    <h2>Subheading 1</h2>
                    <p>This is the first paragraph with some text.</p>
                    <h2>Subheading 2</h2>
                    <p>This is the second paragraph with more text.</p>
                    <ul>
                        <li>Item 1</li>
                        <li>Item 2</li>
                    </ul>
                    <table>
                        <tr>
                            <th>Header 1</th>
                            <th>Header 2</th>
                        </tr>
                        <tr>
                            <td>Cell 1</td>
                            <td>Cell 2</td>
                        </tr>
                    </table>
                    <img src="image.jpg" alt="Image with alt">
                    <img src="image2.jpg">
                </body>
            </html>
        ''')
    def options = new AnalyzerOptions(
        includeAll: false,
        includeContent: true
    )

    when:
    def result = analyzer.analyzeFile(htmlFile, options)

    then:
    result.containsKey('content')
    result.content.textLength > 0
    result.content.wordCount > 0
    result.content.contentCodeRatio > 0
    result.content.headings.h1 == 1
    result.content.headings.h2 == 2
    result.content.unorderedLists == 1
    result.content.listItems == 2
    result.content.tables == 1
    result.content.tableRows == 2
    result.content.tableCells == 4
    result.content.imagesWithAlt == 1
    result.content.imagesWithoutAlt == 1
  }

  def "should analyze performance statistics"() {
    given:
    def analyzer = new HtmlAnalyzer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <head>
                    <script src="external.js"></script>
                    <script>console.log('inline');</script>
                    <script async src="async.js"></script>
                    <script defer src="defer.js"></script>
                    <link rel="preload" href="styles.css" as="style">
                    <link rel="prefetch" href="next-page.html">
                    <link rel="preconnect" href="https://example.com">
                </head>
                <body>
                    <img src="large.jpg">
                    <img src="small.jpg" width="100" height="100">
                    <img src="lazy.jpg" loading="lazy">
                    <picture>
                        <source srcset="image.webp" type="image/webp">
                        <img src="image.jpg" alt="Modern format">
                    </picture>
                    <style>body { color: red; }</style>
                </body>
            </html>
        ''')
    def options = new AnalyzerOptions(
        includeAll: false,
        includePerformance: true
    )

    when:
    def result = analyzer.analyzeFile(htmlFile, options)

    then:
    result.containsKey('performance')
    result.performance.scripts.async == 1
    result.performance.scripts.defer == 1
    result.performance.scripts.blocking == 2
    result.performance.largeImagesWithoutDimensions == 3
    result.performance.inlineStyles == 1
    result.performance.inlineScripts == 1
    result.performance.preload == 1
    result.performance.prefetch == 1
    result.performance.preconnect == 1
    result.performance.modernImageFormats == 2
    result.performance.lazyLoadedImages == 1
  }

  def "should analyze full document with all options"() {
    given:
    def analyzer = new HtmlAnalyzer()
    def htmlFile = createTempHtmlFile('''
            <!DOCTYPE html>
            <html lang="en">
                <head>
                    <title>Full Analysis</title>
                    <meta name="description" content="Complete test">
                </head>
                <body>
                    <h1>Page Title</h1>
                    <p>Sample text.</p>
                    <a href="https://example.com">Link</a>
                </body>
            </html>
        ''')
    def options = new AnalyzerOptions(
        includeAll: true
    )

    when:
    def result = analyzer.analyzeFile(htmlFile, options)

    then:
    result.containsKey('basicInfo')
    result.containsKey('elements')
    result.containsKey('links')
    result.containsKey('structure')
    result.containsKey('content')
    result.containsKey('performance')
  }

  def "should analyze HTML string directly"() {
    given:
    def analyzer = new HtmlAnalyzer()
    def html = '''
            <html>
                <head><title>String Analysis</title></head>
                <body><p>Analyze this string</p></body>
            </html>
        '''
    def options = new AnalyzerOptions(
        includeBasicInfo: true,
        includeAll: false
    )

    when:
    def result = analyzer.analyze(html, options)

    then:
    result.containsKey('basicInfo')
    result.basicInfo.title == 'String Analysis'
  }

  @Unroll
  def "should respect include options for #section"() {
    given:
    def analyzer = new HtmlAnalyzer()
    def htmlFile = createTempHtmlFile('''
            <html>
                <head><title>Test</title></head>
                <body><p>Test</p></body>
            </html>
        ''')
    def options = new AnalyzerOptions(
        includeAll: false,
        includeBasicInfo: basicInfo,
        includeElements: elements,
        includeLinks: links,
        includeStructure: structure,
        includeContent: content,
        includePerformance: performance
    )

    when:
    def result = analyzer.analyzeFile(htmlFile, options)

    then:
    result.containsKey(section) == shouldContain

    where:
    section       | basicInfo | elements | links | structure | content | performance || shouldContain
    'basicInfo'   | true      | false    | false | false     | false   | false       || true
    'elements'    | false     | true     | false | false     | false   | false       || true
    'links'       | false     | false    | true  | false     | false   | false       || true
    'structure'   | false     | false    | false | true      | false   | false       || true
    'content'     | false     | false    | false | false     | true    | false       || true
    'performance' | false     | false    | false | false     | false   | true        || true
    'basicInfo'   | false     | false    | false | false     | false   | false       || false
  }

  private File createTempHtmlFile(String content) {
    def file = Files.createFile(tempDir.resolve("test.html")).toFile()
    file.setText(content, "UTF-8")
    return file
  }
}
