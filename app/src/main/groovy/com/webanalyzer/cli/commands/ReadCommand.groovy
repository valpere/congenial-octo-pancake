package com.webanalyzer.cli.commands

import com.webanalyzer.core.fetcher.FetchOptions
import com.webanalyzer.core.fetcher.WebPageFetcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Command to retrieve a web page by URL and save it as HTML.
 * This updated version uses the enhanced WebPageFetcher with additional options.
 */
@Command(
    name = "read",
    description = "Fetch web page and save as HTML",
    mixinStandardHelpOptions = true
)
class ReadCommand implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(ReadCommand.class)

  @Parameters(index = "0", description = "URL to fetch")
  private String url

  @Parameters(index = "1", description = "Output HTML file path")
  private String outputFile

  @Option(names = ["--dynamic"], description = "Render JavaScript (requires Selenium)")
  private boolean dynamic = false

  @Option(names = ["--wait"], description = "Time to wait for dynamic content to load in ms")
  private int wait = 5000

  @Option(names = ["--timeout"], description = "Connection timeout in seconds")
  private int timeout = 30

  @Option(names = ["--user-agent"], description = "Custom user agent")
  private String userAgent = "WebPageAnalyzer/1.0"

  @Option(names = ["--encoding"], description = "Character encoding for output")
  private String encoding = "UTF-8"

  @Option(names = ["--wait-for-selector"], description = "CSS selector to wait for when using dynamic mode")
  private String waitForSelector

  @Option(names = ["--execute-js"], description = "Custom JavaScript to execute after page load")
  private String executeJs

  @Option(names = ["--headers"], description = "Custom HTTP headers in format 'name1=value1,name2=value2'")
  private String headers

  @Override
  Integer call() throws Exception {
    // Basic URL validation
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      url = "https://" + url
      logger.info("Added https:// prefix to URL: ${url}")
    }

    logger.info("Fetching web page: ${url}")

    try {
      // Parse custom headers if provided
      Map<String, String> headerMap = [:]
      if (headers) {
        headers.split(",").each { header ->
          def parts = header.split("=", 2)
          if (parts.length == 2) {
            headerMap[parts[0].trim()] = parts[1].trim()
          }
        }
      }

      // Configure fetch options with all the enhanced parameters
      def options = new FetchOptions(
          dynamic: dynamic,
          wait: wait,
          timeout: timeout * 1000,
          userAgent: userAgent,
          encoding: encoding,
          waitForSelector: waitForSelector,
          customJavaScript: executeJs,
          headers: headerMap
      )

      // Fetch the web page
      def fetcher = new WebPageFetcher()
      def html = fetcher.fetchPage(url, options)

      // Save to file with proper encoding
      def output = new File(outputFile)
      output.setText(html, encoding)

      logger.info("Successfully downloaded page from ${url} to ${outputFile}")
      System.out.println("Successfully downloaded page from ${url} to ${outputFile}")
      return 0

    } catch (Exception e) {
      logger.error("Error fetching web page: ${e.message}", e)
      System.err.println("Error fetching web page: ${e.message}")
      return 1
    }
  }
}
