package com.webanalyzer.core.analyzer

import groovy.json.JsonGenerator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Service class for analyzing HTML documents and generating statistics.
 * This class handles various types of HTML analysis and produces statistical results.
 */
class HtmlAnalyzer {
  private static final Logger logger = LoggerFactory.getLogger(HtmlAnalyzer.class)

  // Configure JsonGenerator to prevent Unicode escaping
  private static final JsonGenerator generator = new JsonGenerator.Options()
      .disableUnicodeEscaping()
      .build()

  /**
   * Analyze an HTML document and generate statistics.
   *
   * @param html The HTML content to analyze
   * @param options Analysis options
   * @return Analysis results as a Map
   * @throws AnalyzerException if an error occurs during analysis
   */
  Map<String, Object> analyze(String html, AnalyzerOptions options) {
    logger.debug("Analyzing HTML content (${html.length()} characters)")

    try {
      Document document = Jsoup.parse(html)
      return analyzeDocument(document, options)
    } catch (Exception e) {
      logger.error("Error analyzing HTML: ${e.message}", e)
      throw new AnalyzerException("Failed to analyze HTML: ${e.message}", e)
    }
  }

  /**
   * Analyze an HTML file and generate statistics.
   *
   * @param inputFile The HTML file to analyze
   * @param options Analysis options
   * @return Analysis results as a Map
   * @throws AnalyzerException if an error occurs during analysis
   */
  Map<String, Object> analyzeFile(File inputFile, AnalyzerOptions options) {
    logger.debug("Analyzing HTML file: ${inputFile.absolutePath}")

    try {
      Document document = Jsoup.parse(inputFile, options.encoding)
      return analyzeDocument(document, options)
    } catch (Exception e) {
      logger.error("Error analyzing HTML file: ${e.message}", e)
      throw new AnalyzerException("Failed to analyze HTML file: ${e.message}", e)
    }
  }

  /**
   * Generate statistics for an HTML document.
   *
   * @param document The parsed HTML document
   * @param options Analysis options
   * @return A map of statistics
   */
  private Map<String, Object> analyzeDocument(Document document, AnalyzerOptions options) {
    def stats = [:]

    // Include requested analysis types based on options
    if (options.includeBasicInfo || options.includeAll) {
      stats.putAll(analyzeBasicInfo(document))
    }

    if (options.includeElements || options.includeAll) {
      stats.putAll(analyzeElements(document))
    }

    if (options.includeLinks || options.includeAll) {
      stats.putAll(analyzeLinks(document))
    }

    if (options.includeStructure || options.includeAll) {
      stats.putAll(analyzeStructure(document))
    }

    if (options.includeContent || options.includeAll) {
      stats.putAll(analyzeContent(document))
    }

    if (options.includePerformance || options.includeAll) {
      stats.putAll(analyzePerformance(document))
    }

    return stats
  }

  /**
   * Analyze basic document information.
   */
  private Map<String, Object> analyzeBasicInfo(Document document) {
    def info = [:]

    info.title = document.title()
    info.doctype = document.documentType()?.toString() ?: "None"
    info.charset = document.charset().name()
    info.language = document.select("html").attr("lang") ?: "Not specified"
    info.baseUri = document.baseUri()

    // Extract metadata
    def metadata = [:]
    document.select("meta").each { meta ->
      if (meta.hasAttr("name") && meta.hasAttr("content")) {
        metadata[meta.attr("name")] = meta.attr("content")
      } else if (meta.hasAttr("property") && meta.hasAttr("content")) {
        metadata[meta.attr("property")] = meta.attr("content")
      }
    }

    if (!metadata.isEmpty()) {
      info.metadata = metadata
    }

    return [basicInfo: info]
  }

  /**
   * Analyze element statistics.
   */
  private Map<String, Object> analyzeElements(Document document) {
    def elements = [:]

    def allElements = document.getAllElements()
    elements.totalElements = allElements.size()

    // Count by tag name
    def tagCounts = [:]
    allElements.each { element ->
      def tag = element.tagName().toLowerCase()
      tagCounts[tag] = (tagCounts[tag] ?: 0) + 1
    }
    elements.elementsByTag = tagCounts

    // Count form elements
    elements.forms = document.select("form").size()
    elements.inputFields = document.select("input").size()
    elements.selectFields = document.select("select").size()
    elements.textareas = document.select("textarea").size()
    elements.buttons = document.select("button").size()

    // Count media elements
    elements.images = document.select("img").size()
    elements.videos = document.select("video").size()
    elements.audios = document.select("audio").size()

    // Count script and style elements
    elements.scripts = document.select("script").size()
    elements.externalScripts = document.select("script[src]").size()
    elements.inlineScripts = elements.scripts - elements.externalScripts

    elements.styleSheets = document.select("link[rel=stylesheet]").size()
    elements.inlineStyles = document.select("style").size()

    return [elements: elements]
  }

  /**
   * Analyze links in the document.
   */
  private Map<String, Object> analyzeLinks(Document document) {
    def linkAnalysis = [:]

    def links = document.select("a[href]")
    linkAnalysis.totalLinks = links.size()

    def linkTypes = [
        internal  : 0,
        external  : 0,
        mailto    : 0,
        javascript: 0,
        anchor    : 0,
        other     : 0
    ]

    def externalDomains = new HashSet<String>()

    links.each { link ->
      def href = link.attr("href").trim()

      if (href.startsWith("#")) {
        linkTypes.anchor++
      } else if (href.startsWith("mailto:")) {
        linkTypes.mailto++
      } else if (href.startsWith("javascript:")) {
        linkTypes.javascript++
      } else if (href.startsWith("http://") || href.startsWith("https://")) {
        linkTypes.external++

        try {
          def url = new URL(href)
          externalDomains.add(url.host)
        } catch (Exception e) {
          // Ignore malformed URLs
        }
      } else if (href) {
        linkTypes.internal++
      } else {
        linkTypes.other++
      }
    }

    linkAnalysis.linkTypes = linkTypes

    if (!externalDomains.isEmpty()) {
      linkAnalysis.externalDomains = externalDomains.toList()
    }

    return [links: linkAnalysis]
  }

  /**
   * Analyze document structure.
   */
  private Map<String, Object> analyzeStructure(Document document) {
    def structure = [:]

    // Calculate DOM depth
    def maxDepth = 0
    def calculateDepth
    calculateDepth = { element, depth ->
      if (depth > maxDepth) maxDepth = depth
      element.children().each { child ->
        calculateDepth(child, depth + 1)
      }
    }
    calculateDepth(document.body(), 0)
    structure.maxDOMDepth = maxDepth

    // Calculate average nesting level
    def totalNestingLevels = 0
    def elementCount = 0
    def calculateAverageNesting
    calculateAverageNesting = { element, depth ->
      elementCount++
      totalNestingLevels += depth
      element.children().each { child ->
        calculateAverageNesting(child, depth + 1)
      }
    }
    calculateAverageNesting(document.body(), 0)
    structure.averageNestingLevel = elementCount > 0 ? (totalNestingLevels / elementCount) : 0

    // Identify element distribution by level
    def elementsByLevel = [:]
    def distributeElements
    distributeElements = { element, depth ->
      elementsByLevel[depth] = (elementsByLevel[depth] ?: 0) + 1
      element.children().each { child ->
        distributeElements(child, depth + 1)
      }
    }
    distributeElements(document.body(), 0)
    structure.elementsByLevel = elementsByLevel

    // Identify the most deeply nested element
    def deepestElement = ""
    def findDeepestElement
    findDeepestElement = { element, depth ->
      if (depth == maxDepth) {
        deepestElement = element.tagName()
        return true
      }
      for (def child : element.children()) {
        if (findDeepestElement(child, depth + 1)) {
          return true
        }
      }
      return false
    }
    findDeepestElement(document.body(), 0)
    structure.deepestElement = deepestElement

    // Count significant structural elements
    structure.sections = document.select("section").size()
    structure.divs = document.select("div").size()
    structure.articles = document.select("article").size()
    structure.headers = document.select("header").size()
    structure.footers = document.select("footer").size()
    structure.navs = document.select("nav").size()

    return [structure: structure]
  }

  /**
   * Analyze content statistics.
   */
  private Map<String, Object> analyzeContent(Document document) {
    def content = [:]

    // Text statistics
    def text = document.text()
    content.textLength = text.length()
    content.wordCount = text.split(/\s+/).length

    // Calculate content/code ratio
    def htmlSize = document.outerHtml().length()
    content.contentCodeRatio = htmlSize > 0 ? (text.length() / htmlSize) : 0

    // Heading structure
    def headings = [:]
    (1..6).each { level ->
      headings["h${level}"] = document.select("h${level}").size()
    }
    content.headings = headings

    // Lists
    content.orderedLists = document.select("ol").size()
    content.unorderedLists = document.select("ul").size()
    content.listItems = document.select("li").size()

    // Tables
    content.tables = document.select("table").size()
    content.tableRows = document.select("tr").size()
    content.tableCells = document.select("td, th").size()

    // Images
    def images = document.select("img")
    def imagesWithAlt = images.count { it.hasAttr("alt") && !it.attr("alt").isEmpty() }
    content.imagesWithAlt = imagesWithAlt
    content.imagesWithoutAlt = images.size() - imagesWithAlt

    return [content: content]
  }

  /**
   * Analyze performance-related aspects.
   */
  private Map<String, Object> analyzePerformance(Document document) {
    def performance = [:]

    // Script loading
    def asyncScripts = document.select("script[async]").size()
    def deferScripts = document.select("script[defer]").size()
    def normalScripts = document.select("script:not([async]):not([defer])").size()

    performance.scripts = [
        async   : asyncScripts,
        defer   : deferScripts,
        blocking: normalScripts
    ]

    // Large images without dimensions
    def largeImagesWithoutDimensions = document.select("img:not([width]):not([height])").size()
    performance.largeImagesWithoutDimensions = largeImagesWithoutDimensions

    // Inline CSS and JavaScript
    performance.inlineStyles = document.select("style").size()
    performance.inlineScripts = document.select("script:not([src])").size()

    // Resource hints
    performance.preload = document.select("link[rel=preload]").size()
    performance.prefetch = document.select("link[rel=prefetch]").size()
    performance.preconnect = document.select("link[rel=preconnect]").size()

    // Modern image formats
    performance.modernImageFormats = document.select("picture, source[type^=image/webp], source[type^=image/avif]").size()

    // Lazy loading
    performance.lazyLoadedImages = document.select("img[loading=lazy]").size()

    return [performance: performance]
  }

  /**
   * Format statistics as text output.
   */
  String formatAsText(Map<String, Object> stats) {
    def result = new StringBuilder()
    result.append("Web Page Statistics\n")
    result.append("=================\n\n")

    // Format basic information
    if (stats.containsKey("basicInfo")) {
      result.append("Document Information:\n")
      stats.basicInfo.each { key, value ->
        if (key == "metadata") {
          result.append("  Metadata:\n")
          value.each { metaKey, metaValue ->
            result.append("    ${metaKey}: ${metaValue}\n")
          }
        } else {
          result.append("  ${key.capitalize()}: ${value}\n")
        }
      }
      result.append("\n")
    }

    // Format element statistics
    if (stats.containsKey("elements")) {
      result.append("Element Counts:\n")
      result.append("  Total Elements: ${stats.elements.totalElements}\n")

      if (stats.elements.containsKey("elementsByTag")) {
        result.append("  Top 10 Elements by Tag:\n")
        stats.elements.elementsByTag.sort { -it.value }.take(10).each { tag, count ->
          result.append("    ${tag}: ${count}\n")
        }
      }

      // Other element statistics
      def elementTypes = stats.elements.findAll { key, value ->
        key != "totalElements" && key != "elementsByTag"
      }

      if (elementTypes) {
        result.append("  Other Element Types:\n")
        elementTypes.each { key, value ->
          result.append("    ${key.capitalize()}: ${value}\n")
        }
      }

      result.append("\n")
    }

    // Format link analysis
    if (stats.containsKey("links")) {
      result.append("Link Analysis:\n")
      result.append("  Total Links: ${stats.links.totalLinks}\n")

      if (stats.links.containsKey("linkTypes")) {
        stats.links.linkTypes.each { type, count ->
          result.append("  ${type.capitalize()}: ${count}\n")
        }
      }

      if (stats.links.containsKey("externalDomains")) {
        result.append("  External Domains: ${stats.links.externalDomains.size()}\n")
        result.append("  External Domain List:\n")
        stats.links.externalDomains.take(10).each { domain ->
          result.append("    ${domain}\n")
        }

        if (stats.links.externalDomains.size() > 10) {
          result.append("    ... and ${stats.links.externalDomains.size() - 10} more\n")
        }
      }

      result.append("\n")
    }

    // Format structure analysis
    if (stats.containsKey("structure")) {
      result.append("Structure:\n")
      result.append("  Maximum DOM Depth: ${stats.structure.maxDOMDepth}\n")

      if (stats.structure.containsKey("averageNestingLevel")) {
        result.append("  Average Nesting Level: ${String.format("%.2f", stats.structure.averageNestingLevel)}\n")
      }

      if (stats.structure.containsKey("deepestElement")) {
        result.append("  Deepest Element: ${stats.structure.deepestElement}\n")
      }

      // Other structure statistics
      def structureTypes = stats.structure.findAll { key, value ->
        key != "maxDOMDepth" && key != "averageNestingLevel" &&
            key != "deepestElement" && key != "elementsByLevel"
      }

      if (structureTypes) {
        result.append("  Structural Elements:\n")
        structureTypes.each { key, value ->
          result.append("    ${key.capitalize()}: ${value}\n")
        }
      }

      result.append("\n")
    }

    // Format content analysis
    if (stats.containsKey("content")) {
      result.append("Content:\n")
      result.append("  Text Length: ${stats.content.textLength} characters\n")
      result.append("  Word Count: ${stats.content.wordCount} words\n")

      if (stats.content.containsKey("contentCodeRatio")) {
        result.append("  Content/Code Ratio: ${String.format("%.2f", stats.content.contentCodeRatio * 100)}%\n")
      }

      if (stats.content.containsKey("headings")) {
        result.append("  Heading Distribution:\n")
        stats.content.headings.each { heading, count ->
          result.append("    ${heading.toUpperCase()}: ${count}\n")
        }
      }

      result.append("\n")
    }

    // Format performance analysis
    if (stats.containsKey("performance")) {
      result.append("Performance Considerations:\n")

      if (stats.performance.containsKey("scripts")) {
        result.append("  Script Loading:\n")
        stats.performance.scripts.each { type, count ->
          result.append("    ${type.capitalize()}: ${count}\n")
        }
      }

      // Other performance metrics
      def perfMetrics = stats.performance.findAll { key, value -> key != "scripts" }

      if (perfMetrics) {
        result.append("  Other Performance Metrics:\n")
        perfMetrics.each { key, value ->
          result.append("    ${key.capitalize()}: ${value}\n")
        }
      }
    }

    return result.toString()
  }
}
