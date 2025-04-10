package com.webanalyzer.cli.commands

import com.webanalyzer.core.comparator.ComparisonOptions
import com.webanalyzer.core.comparator.HtmlComparator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.nio.charset.Charset
import java.util.concurrent.Callable

/**
 * Command to compare two HTML files and identify differences.
 * Handles UTF-8 and other encodings specified via the --encoding parameter.
 */
@Command(
    name = "compare",
    description = "Compare two HTML files and identify differences",
    mixinStandardHelpOptions = true
)
class CompareCommand implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(CompareCommand.class)

  @Parameters(index = "0", description = "First HTML file path")
  private String file1

  @Parameters(index = "1", description = "Second HTML file path")
  private String file2

  @Parameters(index = "2", description = "Output file path for comparison results")
  private String outputFile

  @Option(names = ["--mode"], description = "Comparison mode (structure, content, visual)")
  private String mode = "content"

  @Option(names = ["--selector"], description = "Limit comparison to elements matching selector")
  private String selector

  @Option(names = ["--ignore-attributes"], description = "Comma-separated list of attributes to ignore")
  private String ignoreAttributes = ""

  @Option(names = ["--format"], description = "Output format (json, txt)")
  private String format = "json"

  @Option(names = ["--encoding"], description = "File encoding")
  private String encoding = "UTF-8"

  @Option(names = ["--pretty"], description = "Format JSON output with indentation")
  private boolean pretty = true

  @Override
  Integer call() throws Exception {
    logger.info("Comparing HTML files: ${file1} and ${file2}")

    // Validate encoding
    validateEncoding(encoding)

    try {
      // Validate input files
      File f1 = new File(file1)
      File f2 = new File(file2)

      if (!f1.exists()) {
        logger.error("First input file does not exist: ${file1}")
        System.err.println("Error: First input file does not exist: ${file1}")
        return 1
      }

      if (!f2.exists()) {
        logger.error("Second input file does not exist: ${file2}")
        System.err.println("Error: Second input file does not exist: ${file2}")
        return 1
      }

      // Configure comparison options
      ComparisonOptions options = new ComparisonOptions(
          mode: mode,
          selector: selector,
          ignoreAttributes: ignoreAttributes ? ignoreAttributes.split(",").collect { it.trim() } : [],
          encoding: encoding
      )

      // Perform the comparison
      HtmlComparator comparator = new HtmlComparator()
      Map<String, Object> results = comparator.compareFiles(f1, f2, options)

      // Format and write the results
      String outputContent
      if (format.toLowerCase() == "json") {
        outputContent = comparator.formatAsJson(results, pretty)
      } else {
        outputContent = comparator.formatAsText(results)
      }

      // Write output file with proper encoding
      File output = new File(outputFile)
      output.setText(outputContent, encoding)

      logger.info("Successfully wrote comparison results to ${outputFile}")
      System.out.println("Found ${results.summary.totalDifferences} differences. Results written to ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error comparing HTML files: ${e.message}", e)
      System.err.println("Error comparing HTML files: ${e.message}")
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
