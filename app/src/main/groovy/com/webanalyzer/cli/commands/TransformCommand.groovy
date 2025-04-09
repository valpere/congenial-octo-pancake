package com.webanalyzer.cli.commands

import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Command to transform HTML to another format.
 */
@Command(
    name = "transform",
    description = "Transform HTML to another format",
    mixinStandardHelpOptions = true
)
class TransformCommand implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(TransformCommand.class)

  @Parameters(index = "0", description = "Input HTML file path")
  private String inputFile

  @Parameters(index = "1", description = "Output file path")
  private String outputFile

  @Option(names = ["--format"], description = "Output format (markdown, plain, json)")
  private String format = "markdown"

  @Option(names = ["--preserve-links"], description = "Maintain hyperlinks in output")
  private boolean preserveLinks = true

  @Option(names = ["--include-images"], description = "Include image references")
  private boolean includeImages = true

  @Override
  Integer call() throws Exception {
    logger.info("Transforming HTML file: ${inputFile} to ${format} format")

    try {
      File input = new File(inputFile)
      if (!input.exists()) {
        logger.error("Input file does not exist: ${inputFile}")
        System.err.println("Error: Input file does not exist: ${inputFile}")
        return 1
      }

      // Parse HTML document
      Document document = Jsoup.parse(input, "UTF-8")
      String transformed

      // Transform to specified format
      switch (format.toLowerCase()) {
        case "markdown":
          transformed = toMarkdown(document)
          break
        case "plain":
          transformed = toPlainText(document)
          break
        case "json":
          transformed = toJsonText(document)
          break
        default:
          logger.error("Unsupported output format: ${format}")
          System.err.println("Error: Unsupported output format: ${format}")
          return 1
      }

      // Write output
      File output = new File(outputFile)
      output.text = transformed

      logger.info("Successfully transformed HTML to ${format}: ${outputFile}")
      System.out.println("Successfully transformed HTML to ${format}: ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error transforming HTML: ${e.message}", e)
      System.err.println("Error transforming HTML: ${e.message}")
      return 1
    }
  }

  /**
   * Convert HTML to Markdown format.
   */
  private String toMarkdown(Document document) {
    StringBuilder markdown = new StringBuilder()

    // Document title as heading
    if (!document.title().isEmpty()) {
      markdown.append("# ${document.title()}\n\n")
    }

    // Process body content
    processElementToMarkdown(document.body(), markdown, 0)

    return markdown.toString()
  }

  /**
   * Recursively process an element to Markdown.
   */
  private void processElementToMarkdown(Element element, StringBuilder markdown, int headingLevel) {
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
        markdown.append("${element.text().trim()}\n\n")
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
        markdown.append("`${element.text().trim()}`")
        break

      case "a":
        if (preserveLinks) {
          markdown.append("[${element.text().trim()}](${element.attr("href")})")
        } else {
          markdown.append(element.text().trim())
        }
        break

      case "img":
        if (includeImages) {
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

      default:
        // Process child nodes - text and child elements
        for (def node : element.childNodes()) {
          if (node instanceof TextNode) {
            String text = node.text().trim()
            if (!text.isEmpty()) {
              markdown.append(text).append(" ")
            }
          } else if (node instanceof Element) {
            processElementToMarkdown(node, markdown, headingLevel)
          }
        }
    }
  }

  /**
   * Convert HTML to plain text format.
   */
  private String toPlainText(Document document) {
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
          if (preserveLinks) {
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
          if (includeImages) {
            text.append("[Image: ${element.attr("alt") ?: "image"}]")
          }
          break
      }
    }

    return text.toString()
  }

  /**
   * Convert HTML to JSON text representation.
   */
  private String toJsonText(Document document) {
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
      if (links && preserveLinks) {
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
    if (includeImages) {
      document.select("img").each { img ->
        result.content << [
            type : "image",
            src  : img.attr("src"),
            alt  : img.attr("alt"),
            title: img.attr("title")
        ]
      }
    }

    // Convert to JSON string with pretty formatting
    return JsonOutput.prettyPrint(JsonOutput.toJson(result))
  }
}