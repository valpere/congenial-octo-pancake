package com.webanalyzer.cli

import com.webanalyzer.cli.commands.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command

import java.util.concurrent.Callable

@Command(
    name = "web-page-analyzer",
    version = "1.0.0",
    description = "A command-line interface (CLI) tool written in Groovy that allows users to retrieve, parse, analyze, and transform web pages.",
    mixinStandardHelpOptions = true,
    subcommands = [
        ParseCommand.class,
        ReadCommand.class,
        ExtractCommand.class,
        StatsCommand.class,
        CompareCommand.class,
        TransformCommand.class
    ]
)
class WebPageAnalyzer implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(WebPageAnalyzer.class)

  /**
   * Application entry point
   */
  static void main(String[] args) {
    int exitCode = new CommandLine(new WebPageAnalyzer())
        .setExecutionExceptionHandler(new ExceptionHandler())
        .execute(args)
    System.exit(exitCode)
  }

  /**
   * When no subcommand is specified, this method is called.
   * @return Exit code
   */
  @Override
  Integer call() {
    System.out.println("Web Page Analyzer v1.0.0")
    System.out.println("Use -h or --help to see available commands")
    return 0
  }

  /**
   * Custom exception handler for better error reporting
   */
  static class ExceptionHandler implements CommandLine.IExecutionExceptionHandler {
    @Override
    int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) {
      logger.error("Execution error: ${ex.message}", ex)
      System.err.println("Error: ${ex.message}")
      return 1
    }
  }
}
