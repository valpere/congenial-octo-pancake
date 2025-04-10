package com.webanalyzer.cli.commands

import com.webanalyzer.core.extractor.ElementExtractor
import com.webanalyzer.core.extractor.ExtractorOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.nio.charset.Charset
import java.util.concurrent.Callable

/**
 * Command to extract elements from HTML file using CSS selectors.
 * Handles UTF-8 and other encodings specified via the --encoding parameter.
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

  @Option(names = ["--include-html"], description = "Include HTML content in output")
  private boolean includeHtml = false

  @Option(names = ["--pretty"], description = "Format JSON output with indentation")
  private boolean pretty = false

  @Option(names = ["--encoding"], description = "File encoding")
  private String encoding = "UTF-8"

  @Override
  Integer call() throws Exception {
    logger.info("Extracting elements from: ${inputFile} using selector: ${selector}")

    // Validate encoding
    validateEncoding(encoding)

    try {
      File input = new File(inputFile)
      if (!input.exists()) {
        logger.error("Input file does not exist: ${inputFile}")
        System.err.println("Error: Input file does not exist: ${inputFile}")
        return 1
      }

      // Configure extraction options
      ExtractorOptions options = new ExtractorOptions(
          encoding: encoding,
          format: format,
          attributes: attributes ? attributes.split(",").collect { it.trim() } : [],
          includeHtml: includeHtml,
          prettyPrint: pretty
      )

      // Perform extraction
      ElementExtractor extractor = new ElementExtractor()
      String result = extractor.extractFromFile(input, selector, options)

      // Write to output file with proper encoding
      File output = new File(outputFile)
      output.setText(result, encoding)

      logger.info("Successfully extracted elements to ${outputFile}")
      System.out.println("Successfully extracted ${format.toUpperCase()} data to ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error extracting elements: ${e.message}", e)
      System.err.println("Error extracting elements: ${e.message}")
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
}
