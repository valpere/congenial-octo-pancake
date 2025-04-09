package com.webanalyzer.cli.commands

import groovy.json.JsonBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Command to compare two HTML files and identify differences.
 */
@Command(
    name = "compare",
    description = "Compare two HTML files and identify differences",
    mixinStandardHelpOptions = true
)
class CompareCommand implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(CompareCommand.class)

  @Parameters(index = "0", description = "First HTML file path")
  private String file1

  @Parameters(index = "1", description = "Second HTML file path")
  private String file2

  @Parameters(index = "2", description = "Output file path for comparison results")
  private String outputFile

  @Option(names = ["--mode"], description = "Comparison mode (structure, content, visual)")
  private String mode = "content"

  @Option(names = ["--selector"], description = "Limit comparison to elements matching selector")
  private String selector

  @Option(names = ["--ignore-attributes"], description = "Comma-separated list of attributes to ignore")
  private String ignoreAttributes = ""

  @Option(names = ["--format"], description = "Output format (json, txt)")
  private String format = "json"

  @Override
  Integer call() throws Exception {
    logger.info("Comparing HTML files: ${file1} and ${file2}")

    try {
      // Validate input files
      File f1 = new File(file1)
      File f2 = new File(file2)

      if (!f1.exists()) {
        logger.error("First input file does not exist: ${file1}")
        System.err.println("Error: First input file does not exist: ${file1}")
        return 1
      }

      if (!f2.exists()) {
        logger.error("Second input file does not exist: ${file2}")
        System.err.println("Error: Second input file does not exist: ${file2}")
        return 1
      }

      // Parse HTML documents
      Document doc1 = Jsoup.parse(f1, "UTF-8")
      Document doc2 = Jsoup.parse(f2, "UTF-8")

      // Get elements to compare
      Elements elements1
      Elements elements2

      if (selector) {
        elements1 = doc1.select(selector)
        elements2 = doc2.select(selector)
        logger.info("Selected ${elements1.size()} elements from first document and ${elements2.size()} elements from second document using selector: ${selector}")
      } else {
        elements1 = doc1.getAllElements()
        elements2 = doc2.getAllElements()
      }

      // Parse ignore attributes
      List<String> attributesToIgnore = ignoreAttributes ? ignoreAttributes.split(",").collect { it.trim() } : []

      // Compare based on mode
      def result = [:]
      result.comparison = [
          file1           : file1,
          file2           : file2,
          mode            : mode,
          selector        : selector ?: "all elements",
          ignoreAttributes: attributesToIgnore
      ]

      switch (mode.toLowerCase()) {
        case "structure":
          result.differences = compareStructure(doc1, doc2, attributesToIgnore)
          break
        case "visual":
          result.differences = compareVisual(doc1, doc2)
          break
        case "content":
        default:
          result.differences = compareContent(doc1, doc2, selector, attributesToIgnore)
      }

      // Write output
      File output = new File(outputFile)
      if (format.toLowerCase() == "json") {
        output.text = new JsonBuilder(result).toPrettyString()
      } else {
        // Text format
        def sb = new StringBuilder()
        sb.append("HTML Comparison Results\n")
        sb.append("======================\n\n")

        sb.append("Comparison Details:\n")
        sb.append("  File 1: ${file1}\n")
        sb.append("  File 2: ${file2}\n")
        sb.append("  Mode: ${mode}\n")
        sb.append("  Selector: ${selector ?: 'all elements'}\n")
        sb.append("  Ignored Attributes: ${attributesToIgnore.join(', ') ?: 'none'}\n\n")

        sb.append("Differences:\n")
        result.differences.each { diff ->
          sb.append("- ${diff.type}: ${diff.description}\n")
          if (diff.location) {
            sb.append("  Location: ${diff.location}\n")
          }
          if (diff.details) {
            sb.append("  Details: ${diff.details}\n")
          }
          sb.append("\n")
        }

        output.text = sb.toString()
      }

      logger.info("Successfully wrote comparison results to ${outputFile}")
      System.out.println("Found ${result.differences.size()} differences. Results written to ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error comparing HTML files: ${e.message}", e)
      System.err.println("Error comparing HTML files: ${e.message}")
      return 1
    }
  }

  /**
   * Compare the structure of two documents.
   */
  private List compareStructure(Document doc1, Document doc2, List<String> attributesToIgnore) {
    def differences = []

    // Compare document structures
    if (doc1.documentType() != doc2.documentType()) {
      differences << [
          type       : "DocType",
          description: "Document types differ",
          details    : "First: ${doc1.documentType() ?: 'None'}, Second: ${doc2.documentType() ?: 'None'}"
      ]
    }

    // Compare basic document properties
    if (doc1.title() != doc2.title()) {
      differences << [
          type       : "Title",
          description: "Document titles differ",
          details    : "First: '${doc1.title()}', Second: '${doc2.title()}'"
      ]
    }

    // Compare element counts by tag
    def tagCounts1 = countElementsByTag(doc1)
    def tagCounts2 = countElementsByTag(doc2)

    def allTags = (tagCounts1.keySet() + tagCounts2.keySet()).unique()

    allTags.each { tag ->
      def count1 = tagCounts1[tag] ?: 0
      def count2 = tagCounts2[tag] ?: 0

      if (count1 != count2) {
        differences << [
            type       : "ElementCount",
            description: "Different number of <${tag}> elements",
            details    : "First: ${count1}, Second: ${count2}"
        ]
      }
    }

    // Compare DOM depth
    def depth1 = calculateMaxDepth(doc1.body())
    def depth2 = calculateMaxDepth(doc2.body())

    if (depth1 != depth2) {
      differences << [
          type       : "DOMDepth",
          description: "Different maximum DOM depth",
          details    : "First: ${depth1}, Second: ${depth2}"
      ]
    }

    return differences
  }

  /**
   * Compare the content of two documents.
   */
  private List compareContent(Document doc1, Document doc2, String selector, List<String> attributesToIgnore) {
    def differences = []

    // If a selector is provided, only compare those elements
    Elements elements1 = selector ? doc1.select(selector) : doc1.getAllElements()
    Elements elements2 = selector ? doc2.select(selector) : doc2.getAllElements()

    // Compare text content
    def text1 = doc1.text()
    def text2 = doc2.text()

    if (text1 != text2) {
      differences << [
          type       : "TextContent",
          description: "Overall text content differs",
          details    : "Character length - First: ${text1.length()}, Second: ${text2.length()}"
      ]
    }

    // Compare links
    def links1 = doc1.select("a[href]")
    def links2 = doc2.select("a[href]")

    if (links1.size() != links2.size()) {
      differences << [
          type       : "LinkCount",
          description: "Different number of links",
          details    : "First: ${links1.size()}, Second: ${links2.size()}"
      ]
    }

    // Compare images
    def images1 = doc1.select("img")
    def images2 = doc2.select("img")

    if (images1.size() != images2.size()) {
      differences << [
          type       : "ImageCount",
          description: "Different number of images",
          details    : "First: ${images1.size()}, Second: ${images2.size()}"
      ]
    }

    // Compare specific elements if selector is provided
    if (selector) {
      def elementsMap1 = mapElementsById(elements1)
      def elementsMap2 = mapElementsById(elements2)

      // Check for elements in first but not in second
      elementsMap1.each { id, element ->
        if (!elementsMap2.containsKey(id)) {
          differences << [
              type       : "MissingElement",
              description: "Element exists in first document but not in second",
              location   : generateSelector(element),
              details    : "Element: <${element.tagName()} id='${id}'>"
          ]
        } else {
          // Compare attributes
          compareElementAttributes(element, elementsMap2[id], attributesToIgnore).each { diff ->
            differences << [
                type       : "AttributeDifference",
                description: "Attribute values differ for element with id='${id}'",
                location   : generateSelector(element),
                details    : diff
            ]
          }

          // Compare text
          if (element.text() != elementsMap2[id].text()) {
            differences << [
                type       : "TextDifference",
                description: "Text content differs for element with id='${id}'",
                location   : generateSelector(element),
                details    : "First: '${element.text()}', Second: '${elementsMap2[id].text()}'"
            ]
          }
        }
      }

      // Check for elements in second but not in first
      elementsMap2.each { id, element ->
        if (!elementsMap1.containsKey(id)) {
          differences << [
              type       : "AddedElement",
              description: "Element exists in second document but not in first",
              location   : generateSelector(element),
              details    : "Element: <${element.tagName()} id='${id}'>"
          ]
        }
      }
    }

    return differences
  }

  /**
   * Compare the visual aspects of two documents.
   * Note: This is a simplified version without actual rendering.
   */
  private static List compareVisual(Document doc1, Document doc2) {
    def differences = []

    // Compare CSS styles
    def styles1 = doc1.select("style")
    def styles2 = doc2.select("style")

    if (styles1.size() != styles2.size()) {
      differences << [
          type       : "StyleCount",
          description: "Different number of style elements",
          details    : "First: ${styles1.size()}, Second: ${styles2.size()}"
      ]
    }

    // Compare external stylesheets
    def styleSheets1 = doc1.select("link[rel=stylesheet]")
    def styleSheets2 = doc2.select("link[rel=stylesheet]")

    def styleSheetUrls1 = styleSheets1.collect { it.attr("href") }.sort()
    def styleSheetUrls2 = styleSheets2.collect { it.attr("href") }.sort()

    if (styleSheetUrls1 != styleSheetUrls2) {
      differences << [
          type       : "StylesheetDifference",
          description: "Different external stylesheets",
          details    : "First: ${styleSheetUrls1.join(', ')}, Second: ${styleSheetUrls2.join(', ')}"
      ]
    }

    // Compare inline styles on elements
    def inlineStyles1 = doc1.select("[style]")
    def inlineStyles2 = doc2.select("[style]")

    if (inlineStyles1.size() != inlineStyles2.size()) {
      differences << [
          type       : "InlineStyleCount",
          description: "Different number of elements with inline styles",
          details    : "First: ${inlineStyles1.size()}, Second: ${inlineStyles2.size()}"
      ]
    }

    return differences
  }

  /**
   * Count elements by tag name.
   */
  private static Map<String, Integer> countElementsByTag(Document doc) {
    def tagCounts = [:]
    doc.getAllElements().each { element ->
      def tag = element.tagName().toLowerCase()
      tagCounts[tag] = (tagCounts[tag] ?: 0) + 1
    }
    return tagCounts
  }

  /**
   * Calculate the maximum depth of an element.
   */
  private int calculateMaxDepth(Element element) {
    if (!element.children().size()) {
      return 1
    }

    return 1 + element.children().collect { calculateMaxDepth(it) }.max()
  }

  /**
   * Map elements by their ID attribute.
   */
  private static Map<String, Element> mapElementsById(Elements elements) {
    def result = [:]
    elements.each { element ->
      if (element.hasAttr("id")) {
        result[element.attr("id")] = element
      }
    }
    return result
  }

  /**
   * Compare attributes between two elements.
   */
  private static List<String> compareElementAttributes(Element e1, Element e2, List<String> attributesToIgnore) {
    def differences = []

    // Get attributes from both elements
    def attrs1 = e1.attributes().collectEntries { [(it.key): it.value] }
    def attrs2 = e2.attributes().collectEntries { [(it.key): it.value] }

    // Remove ignored attributes
    attributesToIgnore.each {
      attrs1.remove(it)
      attrs2.remove(it)
    }

    // Check attributes in first but not in second or with different values
    attrs1.each { key, value ->
      if (!attrs2.containsKey(key)) {
        differences << "Attribute '${key}' exists in first but not in second"
      } else if (attrs2[key] != value) {
        differences << "Attribute '${key}' values differ - First: '${value}', Second: '${attrs2[key]}'"
      }
    }

    // Check attributes in second but not in first
    attrs2.each { key, value ->
      if (!attrs1.containsKey(key)) {
        differences << "Attribute '${key}' exists in second but not in first"
      }
    }

    return differences
  }

  /**
   * Generate a CSS selector for an element.
   */
  private static String generateSelector(Element element) {
    if (element.hasAttr("id")) {
      return "#${element.attr("id")}"
    }

    def selector = element.tagName()
    if (element.hasAttr("class")) {
      selector += ".${element.attr("class").replaceAll("\\s+", ".")}"
    }

    return selector
  }
}
