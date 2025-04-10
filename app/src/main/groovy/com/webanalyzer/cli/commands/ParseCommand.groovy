package com.webanalyzer.cli.commands

import com.webanalyzer.core.parser.HtmlParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.nio.charset.Charset
import java.util.concurrent.Callable

/**
 * Command to parse an HTML file and convert its DOM to JSON format.
 * Handles UTF-8 and other encodings specified via the --encoding parameter.
 */
@Command(
    name = "parse",
    description = "Parse HTML file and convert DOM to JSON",
    mixinStandardHelpOptions = true
)
class ParseCommand implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(ParseCommand.class)

  @Parameters(index = "0", description = "Input HTML file path")
  private String inputFile

  @Parameters(index = "1", description = "Output JSON file path")
  private String outputFile

  @Option(names = ["--pretty"], description = "Format JSON with indentation")
  private boolean pretty = false

  @Option(names = ["--include-text"], description = "Include text content in JSON")
  private boolean includeText = true

  @Option(names = ["--encoding"], description = "File encoding")
  private String encoding = "UTF-8"

  @Override
  Integer call() throws Exception {
    logger.info("Parsing HTML file: ${inputFile}")

    // Validate encoding
    validateEncoding(encoding)

    // Validate input file
    def input = new File(inputFile)
    if (!input.exists()) {
      logger.error("Input file does not exist: ${inputFile}")
      System.err.println("Error: Input file does not exist: ${inputFile}")
      return 1
    }

    try {
      // Use the HtmlParser service to handle the parsing and JSON conversion
      HtmlParser parser = new HtmlParser()
      String jsonResult = parser.parseToJson(
          input,
          Charset.forName(encoding),
          includeText,
          pretty
      )

      // Write the output with explicit encoding (using the consistent approach)
      File output = new File(outputFile)
      output.setText(jsonResult, encoding)

      logger.info("Successfully parsed HTML to JSON: ${outputFile}")
      System.out.println("Successfully parsed HTML to JSON: ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error parsing HTML: ${e.message}", e)
      System.err.println("Error parsing HTML: ${e.message}")
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
