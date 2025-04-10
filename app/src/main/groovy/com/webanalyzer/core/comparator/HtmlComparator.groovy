package com.webanalyzer.core.comparator

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Service class for comparing HTML documents.
 * This class identifies differences between HTML documents using various comparison strategies.
 */
class HtmlComparator {
  private static final Logger logger = LoggerFactory.getLogger(HtmlComparator.class)

  // Configure JsonGenerator to prevent Unicode escaping
  private static final JsonGenerator generator = new JsonGenerator.Options()
      .disableUnicodeEscaping()
      .build()

  /**
   * Compare two HTML documents.
   *
   * @param html1 First HTML content
   * @param html2 Second HTML content
   * @param options Comparison options
   * @return Comparison results
   * @throws ComparatorException if an error occurs during comparison
   */
  Map<String, Object> compare(String html1, String html2, ComparisonOptions options) {
    logger.debug("Comparing HTML content (${html1.length()} chars vs ${html2.length()} chars)")

    try {
      Document doc1 = Jsoup.parse(html1)
      Document doc2 = Jsoup.parse(html2)

      return compareDocuments(doc1, doc2, options)
    } catch (Exception e) {
      logger.error("Error comparing HTML: ${e.message}", e)
      throw new ComparatorException("Failed to compare HTML: ${e.message}", e)
    }
  }

  /**
   * Compare two HTML files.
   *
   * @param file1 First HTML file
   * @param file2 Second HTML file
   * @param options Comparison options
   * @return Comparison results
   * @throws ComparatorException if an error occurs during comparison
   */
  Map<String, Object> compareFiles(File file1, File file2, ComparisonOptions options) {
    logger.debug("Comparing HTML files: ${file1.absolutePath} vs ${file2.absolutePath}")

    try {
      Document doc1 = Jsoup.parse(file1, options.encoding)
      Document doc2 = Jsoup.parse(file2, options.encoding)

      return compareDocuments(doc1, doc2, options)
    } catch (Exception e) {
      logger.error("Error comparing HTML files: ${e.message}", e)
      throw new ComparatorException("Failed to compare HTML files: ${e.message}", e)
    }
  }

  /**
   * Compare two HTML documents and generate a detailed report of differences.
   *
   * @param doc1 First HTML document
   * @param doc2 Second HTML document
   * @param options Comparison options
   * @return Map containing comparison details and differences
   */
  private Map<String, Object> compareDocuments(Document doc1, Document doc2, ComparisonOptions options) {
    def result = [:]

    // Basic comparison info
    result.comparison = [
        mode            : options.mode,
        selector        : options.selector ?: "all elements",
        ignoreAttributes: options.ignoreAttributes
    ]

    // Apply selector if specified
    Elements elements1
    Elements elements2

    if (options.selector) {
      elements1 = doc1.select(options.selector)
      elements2 = doc2.select(options.selector)
      logger.debug("Selected ${elements1.size()} elements from first document and ${elements2.size()} elements from second document using selector: ${options.selector}")
    } else {
      elements1 = doc1.getAllElements()
      elements2 = doc2.getAllElements()
    }

    // Perform comparison based on mode
    switch (options.mode.toLowerCase()) {
      case "structure":
        result.differences = compareStructure(doc1, doc2, options.ignoreAttributes)
        break
      case "visual":
        result.differences = compareVisual(doc1, doc2)
        break
      case "content":
      default:
        result.differences = compareContent(doc1, doc2, options.selector, options.ignoreAttributes)
    }

    // Add summary information
    result.summary = [
        totalDifferences : result.differences.size(),
        differencesByType: result.differences.groupBy { it.type }.collectEntries { type, diffs ->
          [(type): diffs.size()]
        }
    ]

    return result
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

    // Compare character encoding
    if (doc1.charset() != doc2.charset()) {
      differences << [
          type       : "Charset",
          description: "Document character encodings differ",
          details    : "First: ${doc1.charset().name()}, Second: ${doc2.charset().name()}"
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

    // Compare head elements (metadata, scripts, styles)
    compareHeadElements(doc1, doc2, differences)

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

    // Compare link destinations
    def hrefs1 = links1.collect { it.attr("href") }.sort()
    def hrefs2 = links2.collect { it.attr("href") }.sort()

    def uniqueHrefs1 = hrefs1 - hrefs2
    def uniqueHrefs2 = hrefs2 - hrefs1

    if (uniqueHrefs1) {
      differences << [
          type       : "UniqueLinks",
          description: "Links that exist only in the first document",
          details    : "Count: ${uniqueHrefs1.size()}, First ${Math.min(5, uniqueHrefs1.size())} links: ${uniqueHrefs1.take(5).join(', ')}${uniqueHrefs1.size() > 5 ? '...' : ''}"
      ]
    }

    if (uniqueHrefs2) {
      differences << [
          type       : "UniqueLinks",
          description: "Links that exist only in the second document",
          details    : "Count: ${uniqueHrefs2.size()}, First ${Math.min(5, uniqueHrefs2.size())} links: ${uniqueHrefs2.take(5).join(', ')}${uniqueHrefs2.size() > 5 ? '...' : ''}"
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

    // Compare image sources
    def sources1 = images1.collect { it.attr("src") }.sort()
    def sources2 = images2.collect { it.attr("src") }.sort()

    def uniqueSources1 = sources1 - sources2
    def uniqueSources2 = sources2 - sources1

    if (uniqueSources1) {
      differences << [
          type       : "UniqueImages",
          description: "Images that exist only in the first document",
          details    : "Count: ${uniqueSources1.size()}, First ${Math.min(5, uniqueSources1.size())} sources: ${uniqueSources1.take(5).join(', ')}${uniqueSources1.size() > 5 ? '...' : ''}"
      ]
    }

    if (uniqueSources2) {
      differences << [
          type       : "UniqueImages",
          description: "Images that exist only in the second document",
          details    : "Count: ${uniqueSources2.size()}, First ${Math.min(5, uniqueSources2.size())} sources: ${uniqueSources2.take(5).join(', ')}${uniqueSources2.size() > 5 ? '...' : ''}"
      ]
    }

    // Compare specific elements if selector is provided
    if (selector) {
      compareSpecificElements(elements1, elements2, attributesToIgnore, differences)
    }

    return differences
  }

  /**
   * Compare specific elements between two sets of elements.
   */
  private void compareSpecificElements(Elements elements1, Elements elements2, List<String> attributesToIgnore, List differences) {
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

  /**
   * Compare head elements between two documents.
   */
  private void compareHeadElements(Document doc1, Document doc2, List differences) {
    // Compare meta tags
    def meta1 = doc1.select("head > meta")
    def meta2 = doc2.select("head > meta")

    if (meta1.size() != meta2.size()) {
      differences << [
          type       : "MetaCount",
          description: "Different number of meta tags",
          details    : "First: ${meta1.size()}, Second: ${meta2.size()}"
      ]
    }

    // Compare stylesheet links
    def styles1 = doc1.select("head > link[rel=stylesheet]")
    def styles2 = doc2.select("head > link[rel=stylesheet]")

    if (styles1.size() != styles2.size()) {
      differences << [
          type       : "StylesheetCount",
          description: "Different number of stylesheet links",
          details    : "First: ${styles1.size()}, Second: ${styles2.size()}"
      ]
    }

    // Compare script tags
    def scripts1 = doc1.select("head > script")
    def scripts2 = doc2.select("head > script")

    if (scripts1.size() != scripts2.size()) {
      differences << [
          type       : "ScriptCount",
          description: "Different number of script tags in head",
          details    : "First: ${scripts1.size()}, Second: ${scripts2.size()}"
      ]
    }
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

    // Compare classes
    def classes1 = collectClasses(doc1)
    def classes2 = collectClasses(doc2)

    if (classes1.keySet() != classes2.keySet()) {
      def uniqueClasses1 = classes1.keySet() - classes2.keySet()
      def uniqueClasses2 = classes2.keySet() - classes1.keySet()

      if (uniqueClasses1) {
        differences << [
            type       : "UniqueClasses",
            description: "Classes that exist only in the first document",
            details    : "Classes: ${uniqueClasses1.join(', ')}"
        ]
      }

      if (uniqueClasses2) {
        differences << [
            type       : "UniqueClasses",
            description: "Classes that exist only in the second document",
            details    : "Classes: ${uniqueClasses2.join(', ')}"
        ]
      }
    }

    // Compare class usage
    classes1.keySet().intersect(classes2.keySet()).each { className ->
      if (classes1[className] != classes2[className]) {
        differences << [
            type       : "ClassUsage",
            description: "Different usage count for class '${className}'",
            details    : "First: ${classes1[className]}, Second: ${classes2[className]}"
        ]
      }
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

  /**
   * Collect CSS classes used in the document.
   * Returns a map of class names to count of elements using that class.
   */
  private static Map<String, Integer> collectClasses(Document doc) {
    def classes = [:]
    doc.select("[class]").each { element ->
      element.classNames().each { className ->
        classes[className] = (classes[className] ?: 0) + 1
      }
    }
    return classes
  }

  /**
   * Format comparison results as text.
   */
  String formatAsText(Map<String, Object> results) {
    def sb = new StringBuilder()
    sb.append("HTML Comparison Results\n")
    sb.append("======================\n\n")

    sb.append("Comparison Details:\n")
    sb.append("  Mode: ${results.comparison.mode}\n")
    sb.append("  Selector: ${results.comparison.selector}\n")
    sb.append("  Ignored Attributes: ${results.comparison.ignoreAttributes.join(', ') ?: 'none'}\n\n")

    sb.append("Summary:\n")
    sb.append("  Total Differences: ${results.summary.totalDifferences}\n")
    results.summary.differencesByType.each { type, count ->
      sb.append("  ${type}: ${count}\n")
    }
    sb.append("\n")

    sb.append("Differences:\n")
    results.differences.each { diff ->
      sb.append("- ${diff.type}: ${diff.description}\n")
      if (diff.location) {
        sb.append("  Location: ${diff.location}\n")
      }
      if (diff.details) {
        sb.append("  Details: ${diff.details}\n")
      }
      sb.append("\n")
    }

    return sb.toString()
  }

  /**
   * Format comparison results as JSON.
   * This method always uses the JsonGenerator with Unicode escaping disabled
   * to ensure proper handling of non-ASCII characters.
   */
  String formatAsJson(Map<String, Object> results, boolean prettyPrint) {
    // Always use the generator with Unicode escaping disabled
    String jsonString = generator.toJson(results)

    if (prettyPrint) {
      return JsonOutput.prettyPrint(jsonString)
    } else {
      return jsonString
    }
  }
}
