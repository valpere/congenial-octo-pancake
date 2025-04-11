package com.webanalyzer.core.transformer

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Service class for transforming HTML to various output formats.
 * This class handles conversions from HTML to Markdown, plain text, and JSON
 * with proper character encoding preservation.
 */
class HtmlTransformer {
  private static final Logger logger = LoggerFactory.getLogger(HtmlTransformer.class)

  // Configure JsonGenerator to prevent Unicode escaping
  private static final JsonGenerator generator = new JsonGenerator.Options()
      .disableUnicodeEscaping()
      .build()

  /**
   * Transform an HTML document to the specified format.
   *
   * @param html The HTML content to transform
   * @param format The output format ("markdown", "plain", or "json")
   * @param options Additional options for the transformation
   * @return The transformed content
   * @throws TransformerException if an error occurs during transformation
   */
  String transform(String html, String format, TransformOptions options) {
    logger.debug("Transforming HTML (${html.length()} characters) to ${format}")

    try {
      Document document = Jsoup.parse(html)

      switch (format.toLowerCase()) {
        case "markdown":
          return toMarkdown(document, options)
        case "plain":
          return toPlainText(document, options)
        case "json":
          return toJsonText(document, options)
        default:
          throw new TransformerException("Unsupported format: ${format}")
      }
    } catch (TransformerException e) {
      // Re-throw transformer exceptions directly
      throw e
    } catch (Exception e) {
      logger.error("Error transforming HTML: ${e.message}", e)
      throw new TransformerException("Failed to transform HTML: ${e.message}", e)
    }
  }

  /**
   * Transform an HTML file to the specified format.
   *
   * @param inputFile The HTML file to transform
   * @param format The output format ("markdown", "plain", or "json")
   * @param options Additional options for the transformation
   * @return The transformed content
   * @throws TransformerException if an error occurs during transformation
   */
  String transformFile(File inputFile, String format, TransformOptions options) {
    logger.debug("Transforming HTML file: ${inputFile.absolutePath} to ${format}")

    try {
      Document document = Jsoup.parse(inputFile, options.encoding)

      switch (format.toLowerCase()) {
        case "markdown":
          return toMarkdown(document, options)
        case "plain":
          return toPlainText(document, options)
        case "json":
          return toJsonText(document, options)
        default:
          throw new TransformerException("Unsupported format: ${format}")
      }
    } catch (TransformerException e) {
      // Re-throw transformer exceptions directly
      throw e
    } catch (Exception e) {
      logger.error("Error transforming HTML file: ${e.message}", e)
      throw new TransformerException("Failed to transform HTML file: ${e.message}", e)
    }
  }

  /**
   * Convert HTML to Markdown format.
   */
  private String toMarkdown(Document document, TransformOptions options) {
    StringBuilder markdown = new StringBuilder()

    // Document title as heading
    if (!document.title().isEmpty()) {
      markdown.append("# ${document.title()}\n\n")
    }

    // Process body content
    processElementToMarkdown(document.body(), markdown, 0, options)

    return markdown.toString()
  }

  /**
   * Recursively process an element to Markdown.
   */
  private void processElementToMarkdown(Element element, StringBuilder markdown, int headingLevel, TransformOptions options) {
    // Skip script and style elements
    if (element.tagName() in ["script", "style", "head", "noscript"]) {
      return
    }

    // Process by tag type
    switch (element.tagName()) {
      case "h1": case "h2": case "h3": case "h4": case "h5": case "h6":
        int level = element.tagName().charAt(1) as int
        markdown.append("\n${'#' * level} ${element.text().trim()}\n\n")
        break

      case "p":
        // Check if there are any child elements
        if (element.children().isEmpty()) {
          markdown.append("${element.text().trim()}\n\n")
        } else {
          // Process child nodes for paragraph
          element.childNodes().each { node ->
            if (node instanceof TextNode) {
              String text = node.text().trim()
              if (!text.isEmpty()) {
                markdown.append(text)
              }
            } else if (node instanceof Element) {
              processInlineElement(node, markdown, options)
            }
          }
          markdown.append("\n\n")
        }
        break

      case "ul":
        markdown.append("\n")
        element.select("> li").each { li ->
          markdown.append("* ${li.text().trim()}\n")
        }
        markdown.append("\n")
        break

      case "ol":
        markdown.append("\n")
        element.select("> li").eachWithIndex { li, idx ->
          markdown.append("${idx + 1}. ${li.text().trim()}\n")
        }
        markdown.append("\n")
        break

      case "blockquote":
        markdown.append("\n")
        element.text().split("\n").each { line ->
          markdown.append("> ${line.trim()}\n")
        }
        markdown.append("\n")
        break

      case "pre":
        markdown.append("\n```\n${element.text().trim()}\n```\n\n")
        break

      case "code":
        if (element.parent()?.tagName() != "pre") {
          markdown.append("`${element.text().trim()}`")
        }
        break

      case "a":
        if (options.preserveLinks) {
          markdown.append("[${element.text().trim()}](${element.attr("href")})")
        } else {
          markdown.append(element.text().trim())
        }
        break

      case "img":
        if (options.includeImages) {
          String alt = element.hasAttr("alt") ? element.attr("alt") : element.hasAttr("title") ? element.attr("title") : "image"
          markdown.append("![${alt}](${element.attr("src")})")
        }
        break

      case "hr":
        markdown.append("\n---\n\n")
        break

      case "br":
        markdown.append("\n")
        break

      case "strong":
      case "b":
        markdown.append("**${element.text().trim()}**")
        break

      case "em":
      case "i":
        markdown.append("*${element.text().trim()}*")
        break

      default:
        // Process child nodes - text and child elements
        for (def node : element.childNodes()) {
          if (node instanceof TextNode) {
            String text = node.text().trim()
            if (!text.isEmpty()) {
              markdown.append(text).append(" ")
            }
          } else if (node instanceof Element) {
            processElementToMarkdown(node, markdown, headingLevel, options)
          }
        }
    }
  }

  /**
   * Process inline element for Markdown.
   */
  private void processInlineElement(Element element, StringBuilder markdown, TransformOptions options) {
    switch (element.tagName()) {
      case "strong":
      case "b":
        markdown.append("**${element.text().trim()}**")
        break
      case "em":
      case "i":
        markdown.append("*${element.text().trim()}*")
        break
      case "code":
        markdown.append("`${element.text().trim()}`")
        break
      case "a":
        if (options.preserveLinks) {
          markdown.append("[${element.text().trim()}](${element.attr("href")})")
        } else {
          markdown.append(element.text().trim())
        }
        break
      default:
        markdown.append(element.text().trim())
    }
  }

  /**
   * Convert HTML to plain text format.
   */
  private String toPlainText(Document document, TransformOptions options) {
    StringBuilder text = new StringBuilder()

    // Add title
    if (!document.title().isEmpty()) {
      text.append("${document.title().toUpperCase()}\n\n")
    }

    // Extract and format text
    document.body().select("*").each { element ->
      switch (element.tagName()) {
        case "h1": case "h2": case "h3": case "h4": case "h5": case "h6":
          text.append("\n${element.text().trim().toUpperCase()}\n\n")
          break

        case "p":
          text.append("${element.ownText().trim()}\n\n")
          break

        case "li":
          text.append("- ${element.ownText().trim()}\n")
          break

        case "a":
          if (options.preserveLinks) {
            text.append("${element.text().trim()} [${element.attr("href")}]")
          } else {
            text.append(element.text().trim())
          }
          break

        case "br":
          text.append("\n")
          break

        case "hr":
          text.append("\n----------\n\n")
          break

        case "img":
          if (options.includeImages) {
            text.append("[Image: ${element.attr("alt") ?: "image"}]")
          }
          break
      }
    }

    return text.toString()
  }

  /**
   * Convert HTML to JSON text representation with proper character encoding.
   */
  private String toJsonText(Document document, TransformOptions options) {
    def result = [:]

    // Basic document info
    result.title = document.title()
    result.charset = document.charset().name()
    result.baseUri = document.baseUri()

    // Extract content
    result.content = []

    // Process headings
    document.select("h1, h2, h3, h4, h5, h6").each { heading ->
      result.content << [
          type : "heading",
          level: heading.tagName().charAt(1) as int,
          text : heading.text()
      ]
    }

    // Process paragraphs
    document.select("p").each { p ->
      def item = [type: "paragraph", text: p.text()]

      // Add links if present
      def links = p.select("a")
      if (links && options.preserveLinks) {
        item.links = links.collect { link ->
          [text: link.text(), url: link.attr("href")]
        }
      }

      result.content << item
    }

    // Process lists
    document.select("ul, ol").each { list ->
      def items = list.select("li").collect { it.text() }
      result.content << [
          type : list.tagName() == "ul" ? "unordered_list" : "ordered_list",
          items: items
      ]
    }

    // Process images
    if (options.includeImages) {
      document.select("img").each { img ->
        result.content << [
            type : "image",
            src  : img.attr("src"),
            alt  : img.attr("alt"),
            title: img.attr("title")
        ]
      }
    }

    // Use custom generator to preserve UTF-8 characters
    String jsonString = generator.toJson(result)

    // Apply pretty printing if requested
    if (options.prettyPrint) {
      return JsonOutput.prettyPrint(jsonString)
    } else {
      return jsonString
    }
  }
}
