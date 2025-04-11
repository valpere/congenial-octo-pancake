package com.webanalyzer.cli.commands

import com.webanalyzer.core.analyzer.AnalyzerOptions
import com.webanalyzer.core.analyzer.HtmlAnalyzer
import groovy.json.JsonBuilder
import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.nio.charset.Charset
import java.util.concurrent.Callable

/**
 * Command to generate statistics about an HTML document.
 * Handles UTF-8 and other encodings specified via the --encoding parameter.
 */
@Command(
    name = "stats",
    description = "Generate statistics about HTML document",
    mixinStandardHelpOptions = true
)
class StatsCommand implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(StatsCommand.class)

  // Configure JsonGenerator to prevent Unicode escaping
  private static final JsonGenerator generator = new JsonGenerator.Options()
      .disableUnicodeEscaping()
      .build()

  @Parameters(index = "0", description = "Input HTML file path")
  private String inputFile

  @Parameters(index = "1", description = "Output file path for statistics")
  private String outputFile

  @Option(names = ["--format"], description = "Output format (json, txt)")
  private String format = "json"

  @Option(names = ["--encoding"], description = "File encoding")
  private String encoding = "UTF-8"

  @Option(names = ["--include"], description = "Comma-separated list of stats to include (all, basic, elements, links, structure, content, performance)")
  private String include = "all"

  @Override
  Integer call() throws Exception {
    System.out.println("[DEBUG] Starting StatsCommand execution")
    logger.info("Generating statistics for: ${inputFile}")

    // Validate encoding
    try {
      validateEncoding(encoding)
    } catch (Exception e) {
      System.err.println("[DEBUG] Encoding validation failed: " + e.getMessage())
      throw e
    }

    try {
      System.out.println("[DEBUG] Checking input file: ${inputFile}")
      File input = new File(inputFile)
      if (!input.exists()) {
        System.err.println("[DEBUG] Input file not found")
        logger.error("Input file does not exist: ${inputFile}")
        System.err.println("Error: Input file does not exist: ${inputFile}")
        return 1
      }

      // Parse include options
      System.out.println("[DEBUG] Parsing include options: ${include}")
      AnalyzerOptions options
      try {
        options = parseOptions()
        System.out.println("[DEBUG] Options parsed successfully")
      } catch (Exception e) {
        System.err.println("[DEBUG] Error parsing options: " + e.getMessage())
        throw e
      }

      // Generate statistics using two approaches
      System.out.println("[DEBUG] Starting statistics generation")
      Map<String, Object> stats

      try {
        // Try using HtmlAnalyzer first
        System.out.println("[DEBUG] Trying to use HtmlAnalyzer")
        HtmlAnalyzer analyzer = new HtmlAnalyzer()
        stats = analyzer.analyzeFile(input, options)
        System.out.println("[DEBUG] HtmlAnalyzer successful")
      } catch (Exception e) {
        // Fallback to direct JSoup parsing
        System.err.println("[DEBUG] HtmlAnalyzer failed: " + e.getMessage())
        System.out.println("[DEBUG] Falling back to direct JSoup parsing")
        Document document = Jsoup.parse(input, encoding)
        stats = generateBasicStats(document)
      }

      // Format output based on requested format
      System.out.println("[DEBUG] Formatting output as ${format}")
      String outputContent

      try {
        if (format.toLowerCase() == "json") {
          System.out.println("[DEBUG] Creating JSON output")
          // Fixed approach: Use the generator directly
          outputContent = generator.toJson(stats)
          System.out.println("[DEBUG] JSON serialization successful")

          try {
            outputContent = JsonOutput.prettyPrint(outputContent)
            System.out.println("[DEBUG] Pretty printing successful")
          } catch (Exception e) {
            System.err.println("[DEBUG] Pretty printing failed: " + e.getMessage())
            // Keep the non-pretty output if pretty printing fails
          }
        } else {
          System.out.println("[DEBUG] Creating text output")
          outputContent = formatTextOutput(stats)
        }
      } catch (Exception e) {
        System.err.println("[DEBUG] Output formatting failed: " + e.getMessage())
        throw e
      }

      // Write to output file with proper encoding
      System.out.println("[DEBUG] Writing to output file: ${outputFile}")
      try {
        File output = new File(outputFile)
        output.setText(outputContent, encoding)
        System.out.println("[DEBUG] File written successfully")
      } catch (Exception e) {
        System.err.println("[DEBUG] File write failed: " + e.getMessage())
        throw e
      }

      logger.info("Successfully generated statistics to ${outputFile}")
      System.out.println("Successfully generated statistics to ${outputFile}")
      return 0

    } catch (Exception e) {
      System.err.println("[DEBUG] Execution failed with: " + e.getClass().getName() + ": " + e.getMessage())
      e.printStackTrace(System.err)
      logger.error("Error generating statistics: ${e.message}", e)
      System.err.println("Error generating statistics: ${e.message}")
      return 1
    }
  }

  /**
   * Validate the specified encoding is supported.
   */
  private static void validateEncoding(String encoding) {
    try {
      Charset.forName(encoding)
    } catch (Exception e) {
      logger.error("Unsupported encoding: ${encoding}", e)
      throw new IllegalArgumentException("Unsupported encoding: ${encoding}")
    }
  }

  /**
   * Parse include options and create an AnalyzerOptions object.
   */
  private AnalyzerOptions parseOptions() {
    def options = new AnalyzerOptions(encoding: encoding)

    // Handle different include options
    if (include.toLowerCase() == "all") {
      options.includeAll = true
    } else {
      options.includeAll = false

      def includes = include.split(",").collect { it.trim().toLowerCase() }

      if (includes.contains("basic")) {
        options.includeBasicInfo = true
      }
      if (includes.contains("elements")) {
        options.includeElements = true
      }
      if (includes.contains("links")) {
        options.includeLinks = true
      }
      if (includes.contains("structure")) {
        options.includeStructure = true
      }
      if (includes.contains("content")) {
        options.includeContent = true
      }
      if (includes.contains("performance")) {
        options.includePerformance = true
      }
    }

    return options
  }

  /**
   * Generate basic statistics for a document.
   */
  private Map<String, Object> generateBasicStats(Document document) {
    def stats = [:]

    // Basic info
    stats.basicInfo = [:]
    stats.basicInfo.title = document.title()
    stats.basicInfo.charset = document.charset().name()
    stats.basicInfo.language = document.select("html").attr("lang") ?: "Not specified"

    // Element counts
    stats.elements = [:]
    stats.elements.totalElements = document.getAllElements().size()

    // Content stats
    stats.content = [:]
    stats.content.textLength = document.text().length()
    stats.content.wordCount = document.text().split(/\s+/).length

    return stats
  }

  /**
   * Format statistics as text output.
   */
  private String formatTextOutput(Map<String, Object> stats) {
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
      if (stats.elements.containsKey("totalElements")) {
        result.append("  Total Elements: ${stats.elements.totalElements}\n")
      }

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

    // Simple content stats
    if (stats.containsKey("content")) {
      result.append("Content:\n")
      stats.content.each { key, value ->
        result.append("  ${key.capitalize()}: ${value}\n")
      }
    }

    return result.toString()
  }
}