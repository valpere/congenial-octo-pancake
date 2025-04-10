package com.webanalyzer.core.fetcher

import io.github.bonigarcia.wdm.WebDriverManager
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.HttpStatus
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Enhanced service class for fetching web pages, supporting both static and dynamic content.
 * This class handles HTTP requests and browser automation for JavaScript-rendered pages.
 */
class WebPageFetcher {
  private static final Logger logger = LoggerFactory.getLogger(WebPageFetcher.class)

  /**
   * Fetch a web page from the specified URL.
   *
   * @param url The URL to fetch
   * @param options Fetch configuration options
   * @return The HTML content of the page
   * @throws IOException If there's an error fetching the page
   * @throws FetchException If there's an HTTP or browser error
   */
  String fetchPage(String url, FetchOptions options) {
    if (options.dynamic) {
      return fetchDynamicPage(url, options)
    } else {
      return fetchStaticPage(url, options)
    }
  }

  /**
   * Fetch a static web page using HTTP client.
   *
   * @param url The URL to fetch
   * @param options Fetch configuration options
   * @return The HTML content of the page
   * @throws IOException If there's an error with the connection
   * @throws FetchException If there's an HTTP error
   */
  private String fetchStaticPage(String url, FetchOptions options) {
    logger.debug("Fetching static page: ${url} with timeout: ${options.timeout}ms")

    try {
      // Create request configuration with timeout
      RequestConfig requestConfig = RequestConfig.custom()
          .setConnectTimeout(options.timeout, TimeUnit.MILLISECONDS)
          .setResponseTimeout(options.timeout, TimeUnit.MILLISECONDS)
          .build()

      // Create HTTP client with custom configuration
      CloseableHttpClient httpClient = HttpClients.custom()
          .setUserAgent(options.userAgent)
          .setDefaultRequestConfig(requestConfig)
          .build()

      try {
        HttpGet request = new HttpGet(url)

        // Add custom headers if specified
        options.headers.each { name, value ->
          request.addHeader(name, value)
        }

        return httpClient.execute(request, response -> {
          int statusCode = response.getCode()

          if (statusCode != HttpStatus.SC_OK) {
            throw new FetchException("HTTP error ${statusCode}: ${response.getReasonPhrase()}")
          }

          return EntityUtils.toString(response.getEntity(), options.encoding)
        })
      } finally {
        httpClient.close()
      }
    } catch (Exception e) {
      logger.error("Error fetching page statically: ${e.message}", e)
      throw new FetchException("Failed to fetch page: ${e.message}", e)
    }
  }

  /**
   * Fetch a dynamic web page using headless browser (Selenium).
   * This method requires Selenium WebDriver to be available in the classpath.
   *
   * @param url The URL to fetch
   * @param options Fetch configuration options
   * @return The HTML content of the page
   * @throws FetchException If dynamic fetching fails
   */
  private String fetchDynamicPage(String url, FetchOptions options) {
    logger.debug("Fetching dynamic page: ${url} with wait time: ${options.wait}ms")

    try {
      // Set up WebDriver manager to handle driver setup automatically
      WebDriverManager.chromedriver().setup()

      // Configure Chrome options for headless operation
      ChromeOptions chromeOptions = new ChromeOptions()
      chromeOptions.addArguments("--headless=new")
      chromeOptions.addArguments("--disable-gpu")
      chromeOptions.addArguments("--window-size=1920,1080")
      chromeOptions.addArguments("--disable-extensions")
      chromeOptions.addArguments("--no-sandbox")
      chromeOptions.addArguments("--disable-dev-shm-usage")

      // Add these additional options to improve reliability
      chromeOptions.addArguments("--ignore-certificate-errors")
      chromeOptions.addArguments("--disable-popup-blocking")
      chromeOptions.addArguments("--disable-notifications")

      // Set user agent if specified
      if (options.userAgent) {
        chromeOptions.addArguments("--user-agent=${options.userAgent}")
      }

      // Create the WebDriver instance
      WebDriver driver = new ChromeDriver(chromeOptions)

      try {
        // Set page load timeout
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(options.timeout))

        // Navigate to the URL
        logger.debug("Navigating to URL: ${url}")
        driver.get(url)
        logger.debug("Page loaded successfully")

        // Wait for dynamic content to load if wait time is specified
        if (options.wait > 0) {
          logger.debug("Waiting for ${options.wait}ms for dynamic content to load")
          Thread.sleep(options.wait)
        }

        // If specific wait conditions are provided, wait for them
        if (options.waitForSelector) {
          logger.debug("Waiting for selector: ${options.waitForSelector}")
          WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(options.timeout))
          wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(options.waitForSelector)))
          logger.debug("Selector found")
        }

        // Execute any custom JavaScript if provided
        if (options.customJavaScript) {
          logger.debug("Executing custom JavaScript")
          JavascriptExecutor js = (JavascriptExecutor) driver
          js.executeScript(options.customJavaScript)
          logger.debug("Custom JavaScript executed successfully")

          // Give a short time for the script to complete
          Thread.sleep(500)
        }

        // Get the rendered page source
        String pageSource = driver.getPageSource()
        logger.debug("Page source retrieved, length: ${pageSource.length()} characters")
        return pageSource
      } finally {
        // Always quit the driver to release resources
        logger.debug("Closing WebDriver")
        driver.quit()
      }
    } catch (Exception e) {
      logger.error("Error fetching page dynamically: ${e.message}", e)
      throw new FetchException("Failed to fetch dynamic page: ${e.message}", e)
    }
  }
}

/**
 * Options for configuring the page fetch.
 */
class FetchOptions {
  /**
   * Whether to use a headless browser for JavaScript rendering.
   */
  boolean dynamic = false

  /**
   * Time to wait for dynamic content to load in milliseconds.
   */
  int wait = 5000

  /**
   * Connection and request timeout in milliseconds.
   */
  int timeout = 30000

  /**
   * User agent string to use for the request.
   */
  String userAgent = "WebPageAnalyzer/1.0"

  /**
   * Character encoding to use for reading the response.
   */
  String encoding = "UTF-8"

  /**
   * CSS selector to wait for when using dynamic fetching.
   */
  String waitForSelector = null

  /**
   * Custom JavaScript to execute after page load when using dynamic fetching.
   */
  String customJavaScript = null

  /**
   * Additional HTTP headers to include in the request.
   */
  Map<String, String> headers = [:]
}

/**
 * Exception thrown when page fetching fails.
 */
class FetchException extends Exception {
  FetchException(String message) {
    super(message)
  }

  FetchException(String message, Throwable cause) {
    super(message, cause)
  }
}
