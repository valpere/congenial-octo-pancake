package com.webanalyzer.core.parser

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.Charset

/**
 * Enhanced HTML parser that handles DOM to JSON conversion with proper UTF-8 character encoding.
 */
class HtmlParser {
  private static final Logger logger = LoggerFactory.getLogger(HtmlParser.class)

  // Configure JsonGenerator to prevent Unicode escaping
  private static final JsonGenerator generator = new JsonGenerator.Options()
      .disableUnicodeEscaping()
      .build()

  /**
   * Parse an HTML file and convert its DOM to a JSON structure with proper character encoding.
   *
   * @param file The HTML file to parse
   * @param charset The character encoding to use
   * @param includeText Whether to include text content in the JSON
   * @param prettyPrint Whether to format the JSON with indentation
   * @return A String containing the JSON representation of the DOM
   */
  String parseToJson(File file, Charset charset, boolean includeText, boolean prettyPrint) {
    logger.debug("Parsing file: ${file.absolutePath} with charset: ${charset.displayName()}")

    try {
      // Read the file using the specified charset
      String htmlContent = file.getText(charset.name())
      Document document = Jsoup.parse(htmlContent)

      // Convert to map and then to JSON
      return convertToJson(document, includeText, prettyPrint)
    } catch (Exception e) {
      logger.error("Error parsing HTML file: ${e.message}", e)
      throw new ParserException("Failed to parse HTML file: ${e.message}", e)
    }
  }

  /**
   * Parse HTML text and convert its DOM to a JSON structure with proper character encoding.
   *
   * @param html The HTML string to parse
   * @param includeText Whether to include text content in the JSON
   * @param prettyPrint Whether to format the JSON with indentation
   * @return A String containing the JSON representation of the DOM
   */
  String parseHtmlToJson(String html, boolean includeText, boolean prettyPrint) {
    logger.debug("Parsing HTML string (length: ${html.length()})")

    try {
      Document document = Jsoup.parse(html)

      // Convert to map and then to JSON
      return convertToJson(document, includeText, prettyPrint)
    } catch (Exception e) {
      logger.error("Error parsing HTML string: ${e.message}", e)
      throw new ParserException("Failed to parse HTML string: ${e.message}", e)
    }
  }

  /**
   * Convert a JSoup Document to JSON with proper UTF-8 handling
   */
  private String convertToJson(Document document, boolean includeText, boolean prettyPrint) {
    // Convert document to map
    def domMap = elementToMap(document.root(), includeText)

    // Convert to JSON with explicit handling for non-ASCII characters
    String jsonString = generator.toJson(domMap)

    // Apply pretty printing if requested
    return prettyPrint ? JsonOutput.prettyPrint(jsonString) : jsonString
  }

  /**
   * Recursively convert a DOM element to a JSON-compatible map structure.
   *
   * @param element The DOM element to convert
   * @param includeText Whether to include text content
   * @return A map representing the element and its children
   */
  private Map elementToMap(Element element, boolean includeText) {
    def result = [
        tagName   : element.tagName(),
        attributes: element.attributes().collectEntries { attr ->
          [(attr.key): attr.value]
        }
    ]

    if (includeText && !element.ownText().isEmpty()) {
      result.text = element.ownText()
    }

    // Only include children array if there are child elements
    if (element.children().size() > 0) {
      result.children = element.children().collect { child ->
        elementToMap(child, includeText)
      }
    }

    return result
  }
}

/**
 * Custom exception for HTML parsing errors
 */
class ParserException extends Exception {
  ParserException(String message) {
    super(message)
  }

  ParserException(String message, Throwable cause) {
    super(message, cause)
  }
}
