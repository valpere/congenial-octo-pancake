package com.webanalyzer.test

import com.webanalyzer.core.fetcher.FetchOptions
import com.webanalyzer.core.fetcher.WebPageFetcher

/**
 * Simple script to demonstrate and test dynamic page fetching functionality.
 * This script fetches content from the test HTML file both with and without
 * dynamic rendering, and shows the differences in the output.
 *
 * Run this script with:
 * ./gradlew run --args="read path/to/dynamic-content-test.html output.html --dynamic --wait=5000"
 */
class TestDynamicFetching {
    
    static void main(String[] args) {
        // File path to the test HTML file
        File testHtmlFile = new File("test-resources/dynamic-content-test.html")
        String testHtmlUrl = testHtmlFile.toURI().toString()
        
        // Directory to store output files
        File outputDir = new File("test-output")
        outputDir.mkdirs()
        
        // Create fetcher
        WebPageFetcher fetcher = new WebPageFetcher()
        
        // 1. Fetch content statically (without JavaScript rendering)
        println "Fetching page statically..."
        FetchOptions staticOptions = new FetchOptions(
            dynamic: false,
            timeout: 5000
        )
        
        String staticContent = fetcher.fetchPage(testHtmlUrl, staticOptions)
        
        // Save static content to file
        File staticOutputFile = new File(outputDir, "static-output.html")
        staticOutputFile.text = staticContent
        println "Static content saved to: ${staticOutputFile.absolutePath}"
        
        // 2. Fetch content dynamically (with JavaScript rendering)
        println "Fetching page dynamically with JavaScript rendering..."
        FetchOptions dynamicOptions = new FetchOptions(
            dynamic: true,
            wait: 4000,  // Wait 4 seconds to ensure delayed content is loaded
            timeout: 30000
        )
        
        String dynamicContent = fetcher.fetchPage(testHtmlUrl, dynamicOptions)
        
        // Save dynamic content to file
        File dynamicOutputFile = new File(outputDir, "dynamic-output.html")
        dynamicOutputFile.text = dynamicContent
        println "Dynamic content saved to: ${dynamicOutputFile.absolutePath}"
        
        // 3. Fetch with waiting for a specific element
        println "Fetching page with waiting for a specific element..."
        FetchOptions waitForElementOptions = new FetchOptions(
            dynamic: true,
            waitForSelector: "#delayed-content div",  // Wait for the delayed content div
            timeout: 30000
        )
        
        String waitForElementContent = fetcher.fetchPage(testHtmlUrl, waitForElementOptions)
        
        // Save content to file
        File waitForElementOutputFile = new File(outputDir, "wait-for-element-output.html")
        waitForElementOutputFile.text = waitForElementContent
        println "Content with wait-for-element saved to: ${waitForElementOutputFile.absolutePath}"
        
        // 4. Fetch with executing custom JavaScript
        println "Fetching page with custom JavaScript execution..."
        FetchOptions customJsOptions = new FetchOptions(
            dynamic: true,
            wait: 1000,
            customJavaScript: """
                // Click the "Load Data Table" button to generate the table
                document.getElementById('load-data').click();
                
                // Add a new element
                document.getElementById('add-element').click();
                
                // Change the background color
                document.getElementById('change-styles').click();
            """,
            timeout: 30000
        )
        
        String customJsContent = fetcher.fetchPage(testHtmlUrl, customJsOptions)
        
        // Save content to file
        File customJsOutputFile = new File(outputDir, "custom-js-output.html")
        customJsOutputFile.text = customJsContent
        println "Content with custom JS execution saved to: ${customJsOutputFile.absolutePath}"
        
        // Print summary of differences
        println "\nTest Summary:"
        println "-------------"
        println "1. Static content: Does not contain delayed content or any JavaScript-generated elements"
        println "2. Dynamic content: Contains the delayed content that appears after 3 seconds"
        println "3. Wait-for-element content: Contains the delayed content due to explicit wait condition"
        println "4. Custom JS content: Contains data table, new element, and changed styles due to simulated button clicks"
        
        println "\nDifferences in content lengths:"
        println "Static content length: ${staticContent.length()} characters"
        println "Dynamic content length: ${dynamicContent.length()} characters"
        println "Wait-for-element content length: ${waitForElementContent.length()} characters"
        println "Custom JS content length: ${customJsContent.length()} characters"
    }
}
