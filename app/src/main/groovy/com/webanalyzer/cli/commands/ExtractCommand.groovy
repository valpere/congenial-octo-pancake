package com.webanalyzer.cli.commands

import groovy.json.JsonBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Command to extract elements from HTML file using CSS selectors.
 */
@Command(
    name = "extract",
    description = "Extract elements from HTML using CSS selectors",
    mixinStandardHelpOptions = true
)
class ExtractCommand implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(ExtractCommand.class)

  @Parameters(index = "0", description = "Input HTML file path")
  private String inputFile

  @Parameters(index = "1", description = "CSS selector")
  private String selector

  @Parameters(index = "2", description = "Output file path")
  private String outputFile

  @Option(names = ["--format"], description = "Output format (json, csv, txt)")
  private String format = "json"

  @Option(names = ["--attributes"], description = "Comma-separated list of attributes to extract")
  private String attributes = ""

  @Override
  Integer call() throws Exception {
    logger.info("Extracting elements from: ${inputFile} using selector: ${selector}")

    try {
      File input = new File(inputFile)
      if (!input.exists()) {
        logger.error("Input file does not exist: ${inputFile}")
        System.err.println("Error: Input file does not exist: ${inputFile}")
        return 1
      }

      // Parse HTML document
      Document document = Jsoup.parse(input, "UTF-8")

      // Select elements
      Elements elements = document.select(selector)
      logger.info("Found ${elements.size()} elements matching selector: ${selector}")
      System.out.println("Found ${elements.size()} elements matching selector: ${selector}")

      // Extract data
      List<String> attributeList = attributes ? attributes.split(",").collect { it.trim() } : []

      def result = elements.collect { element ->
        def item = [text: element.text()]

        if (attributeList) {
          attributeList.each { attr ->
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

        return item
      }

      // Output data in requested format
      File output = new File(outputFile)
      switch (format.toLowerCase()) {
        case "json":
          output.text = new JsonBuilder(result).toPrettyString()
          break
        case "csv":
          // Get all possible headers
          def headers = ["text"] + result.collectMany { it.keySet() }.unique() - "text"

          // Create CSV content
          def csv = [headers.join(",")]
          result.each { item ->
            def row = headers.collect { header ->
              def value = item[header] ?: ""
              // Escape quotes and wrap in quotes if contains comma
              if (value.contains(",") || value.contains("\"")) {
                "\"${value.replace('"', '""')}\""
              } else {
                value
              }
            }
            csv << row.join(",")
          }
          output.text = csv.join("\n")
          break
        case "txt":
        default:
          output.text = result.collect { item ->
            "TEXT: ${item.text}\n" + item.findAll { k, v -> k != "text" }.collect { k, v ->
              "${k.toUpperCase()}: ${v}"
            }.join("\n") + "\n----------"
          }.join("\n")
      }

      logger.info("Successfully extracted elements to ${outputFile}")
      System.out.println("Successfully extracted elements to ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error extracting elements: ${e.message}", e)
      System.err.println("Error extracting elements: ${e.message}")
      return 1
    }
  }
}
