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

    // Validate input file
    def input = new File(inputFile)
    if (!input.exists()) {
      logger.error("Input file does not exist: ${inputFile}")
      System.err.println("Error: Input file does not exist: ${inputFile}")
      return 1
    }

    try {
      // Parse the HTML and convert to JSON
      def parser = new HtmlParser()
      def jsonResult = parser.parseToJson(
          input,
          Charset.forName(encoding),
          includeText
      )

      // Write to output file
      def output = new File(outputFile)
      if (pretty) {
        output.text = jsonResult.toPrettyString()
      } else {
        output.text = jsonResult.toString()
      }

      logger.info("Successfully parsed HTML to JSON: ${outputFile}")
      System.out.println("Successfully parsed HTML to JSON: ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error parsing HTML: ${e.message}", e)
      System.err.println("Error parsing HTML: ${e.message}")
      return 1
    }
  }
}
