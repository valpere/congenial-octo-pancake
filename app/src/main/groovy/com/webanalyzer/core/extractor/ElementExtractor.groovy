package com.webanalyzer.core.extractor

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Service class for extracting elements from HTML documents using CSS selectors.
 * This class handles extraction and formatting of results in various output formats.
 */
class ElementExtractor {
  private static final Logger logger = LoggerFactory.getLogger(ElementExtractor.class)

  // Configure JsonGenerator to prevent Unicode escaping
  private static final JsonGenerator generator = new JsonGenerator.Options()
      .disableUnicodeEscaping()
      .build()

  /**
   * Extract elements from HTML content using a CSS selector.
   *
   * @param html The HTML content to extract from
   * @param selector The CSS selector to use for extraction
   * @param options Extraction options
   * @return The extracted content in the specified format
   * @throws ExtractorException if an error occurs during extraction
   */
  String extract(String html, String selector, ExtractorOptions options) {
    logger.debug("Extracting elements from HTML content using selector: ${selector}")

    try {
      Document document = Jsoup.parse(html)
      Elements elements = document.select(selector)
      logger.debug("Found ${elements.size()} elements matching selector: ${selector}")

      return formatExtractedElements(elements, options)
    } catch (Exception e) {
      logger.error("Error extracting elements: ${e.message}", e)
      throw new ExtractorException("Failed to extract elements: ${e.message}", e)
    }
  }

  /**
   * Extract elements from an HTML file using a CSS selector.
   *
   * @param inputFile The HTML file to extract from
   * @param selector The CSS selector to use for extraction
   * @param options Extraction options
   * @return The extracted content in the specified format
   * @throws ExtractorException if an error occurs during extraction
   */
  String extractFromFile(File inputFile, String selector, ExtractorOptions options) {
    logger.debug("Extracting elements from HTML file: ${inputFile.absolutePath} using selector: ${selector}")

    try {
      Document document = Jsoup.parse(inputFile, options.encoding)
      Elements elements = document.select(selector)
      logger.debug("Found ${elements.size()} elements matching selector: ${selector}")

      return formatExtractedElements(elements, options)
    } catch (Exception e) {
      logger.error("Error extracting elements from file: ${e.message}", e)
      throw new ExtractorException("Failed to extract elements from file: ${e.message}", e)
    }
  }

  /**
   * Format extracted elements according to the specified output format.
   *
   * @param elements The extracted elements
   * @param options Extraction options
   * @return Formatted output string
   */
  private String formatExtractedElements(Elements elements, ExtractorOptions options) {
    // Extract data from elements
    def extractedData = elements.collect { element ->
      def item = [text: element.text()]

      // Extract specified attributes or all attributes
      if (options.attributes && !options.attributes.isEmpty()) {
        options.attributes.each { attr ->
          if (element.hasAttr(attr)) {
            item[attr] = element.attr(attr)
          }
        }
      } else {
        // If no attributes specified, extract all
        element.attributes().each { attribute ->
          item[attribute.key] = attribute.value
        }
      }

      // Extract HTML content if requested
      if (options.includeHtml) {
        item.html = element.outerHtml()
      }

      return item
    }

    // Format the data according to the requested output format
    switch (options.format.toLowerCase()) {
      case "json":
        return formatAsJson(extractedData, options.prettyPrint)
      case "csv":
        return formatAsCsv(extractedData)
      case "txt":
      default:
        return formatAsText(extractedData)
    }
  }

  /**
   * Format data as JSON.
   * Always use the custom JsonGenerator to preserve UTF-8 characters.
   */
  private String formatAsJson(List<Map<String, String>> data, boolean prettyPrint) {
    // Always use the custom generator to preserve UTF-8 characters
    String jsonString = generator.toJson(data)

    if (prettyPrint) {
      return JsonOutput.prettyPrint(jsonString)
    } else {
      return jsonString
    }
  }

  /**
   * Format data as CSV.
   */
  private String formatAsCsv(List<Map<String, String>> data) {
    if (data.isEmpty()) {
      return ""
    }

    // Get all possible headers from all items
    def allKeys = data.collectMany { it.keySet() }.unique()

    // Ensure "text" is the first column
    def headers = ["text"]
    headers.addAll(allKeys.findAll { it != "text" })

    // Create CSV content
    def csv = [headers.join(",")]

    data.each { item ->
      def row = headers.collect { header ->
        def value = item[header] ?: ""
        // Escape quotes and wrap in quotes if contains comma or quotes
        if (value.contains(",") || value.contains("\"")) {
          "\"${value.replace('"', '""')}\""
        } else {
          value
        }
      }
      csv << row.join(",")
    }

    return csv.join("\n")
  }

  /**
   * Format data as plain text.
   */
  private String formatAsText(List<Map<String, String>> data) {
    return data.collect { item ->
      def result = new StringBuilder()
      result.append("TEXT: ${item.text}\n")

      // Append all other attributes
      item.findAll { k, v -> k != "text" && k != "html" }.each { k, v ->
        result.append("${k.toUpperCase()}: ${v}\n")
      }

      // Append HTML content if present
      if (item.containsKey("html")) {
        result.append("HTML:\n${item.html}\n")
      }

      result.append("----------\n")
      return result.toString()
    }.join("\n")
  }
}
