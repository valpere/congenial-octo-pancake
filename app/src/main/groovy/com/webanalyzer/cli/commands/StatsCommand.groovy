package com.webanalyzer.cli.commands

import com.webanalyzer.core.analyzer.AnalyzerOptions
import com.webanalyzer.core.analyzer.HtmlAnalyzer
import groovy.json.JsonBuilder
import groovy.json.JsonGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Command to generate statistics about an HTML document.
 * This updated version uses the HtmlAnalyzer service for analysis.
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
    logger.info("Generating statistics for: ${inputFile}")

    try {
      File input = new File(inputFile)
      if (!input.exists()) {
        logger.error("Input file does not exist: ${inputFile}")
        System.err.println("Error: Input file does not exist: ${inputFile}")
        return 1
      }

      // Parse include options
      AnalyzerOptions options = parseOptions()

      // Generate statistics using the analyzer
      HtmlAnalyzer analyzer = new HtmlAnalyzer()
      Map<String, Object> stats = analyzer.analyzeFile(input, options)

      // Format output based on requested format
      String outputContent
      if (format.toLowerCase() == "json") {
        def jsonBuilder = new JsonBuilder(stats)
        jsonBuilder.setGenerator(generator)
        outputContent = jsonBuilder.toPrettyString()
      } else {
        // Text format
        outputContent = formatTextOutput(stats)
      }

      // Write to output file with proper encoding
      File output = new File(outputFile)
      output.setText(outputContent, encoding)

      logger.info("Successfully generated statistics to ${outputFile}")
      System.out.println("Successfully generated statistics to ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error generating statistics: ${e.message}", e)
      System.err.println("Error generating statistics: ${e.message}")
      return 1
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
