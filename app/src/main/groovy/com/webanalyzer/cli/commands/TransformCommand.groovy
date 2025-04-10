package com.webanalyzer.cli.commands

import com.webanalyzer.core.transformer.HtmlTransformer
import com.webanalyzer.core.transformer.TransformOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Command to transform HTML to another format.
 * This updated version uses the HtmlTransformer service for proper transformation.
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

  @Option(names = ["--pretty"], description = "Format output with indentation (for JSON)")
  private boolean pretty = false

  @Option(names = ["--encoding"], description = "File encoding")
  private String encoding = "UTF-8"

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

      // Configure transformation options
      TransformOptions options = new TransformOptions(
          encoding: encoding,
          preserveLinks: preserveLinks,
          includeImages: includeImages,
          prettyPrint: pretty
      )

      // Perform the transformation
      HtmlTransformer transformer = new HtmlTransformer()
      String transformed = transformer.transformFile(input, format, options)

      // Write output with proper encoding
      File output = new File(outputFile)
      output.setText(transformed, encoding)

      logger.info("Successfully transformed HTML to ${format}: ${outputFile}")
      System.out.println("Successfully transformed HTML to ${format}: ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error transforming HTML: ${e.message}", e)
      System.err.println("Error transforming HTML: ${e.message}")
      return 1
    }
  }
}
