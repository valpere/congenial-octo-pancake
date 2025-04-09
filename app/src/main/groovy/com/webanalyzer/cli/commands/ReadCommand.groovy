package com.webanalyzer.cli.commands

import com.webanalyzer.core.fetcher.WebPageFetcher
import com.webanalyzer.core.fetcher.FetchOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Command to retrieve a web page by URL and save it as HTML.
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
    
    @Override
    Integer call() throws Exception {
        // Basic URL validation
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url
            logger.info("Added https:// prefix to URL: ${url}")
        }
        
        logger.info("Fetching web page: ${url}")
        
        try {
            // Configure fetch options
            def options = new FetchOptions(
                dynamic: dynamic,
                wait: wait,
                timeout: timeout * 1000,
                userAgent: userAgent
            )
            
            // Fetch the web page
            def fetcher = new WebPageFetcher()
            def html = fetcher.fetchPage(url, options)
            
            // Save to file
            def output = new File(outputFile)
            output.text = html
            
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
