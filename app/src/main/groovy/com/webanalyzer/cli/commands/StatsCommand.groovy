package com.webanalyzer.cli.commands

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Command to generate statistics about an HTML document.
 */
@Command(
    name = "stats",
    description = "Generate statistics about HTML document",
    mixinStandardHelpOptions = true
)
class StatsCommand implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(StatsCommand.class)
    
    @Parameters(index = "0", description = "Input HTML file path")
    private String inputFile
    
    @Parameters(index = "1", description = "Output file path for statistics")
    private String outputFile
    
    @Option(names = ["--format"], description = "Output format (json, txt)")
    private String format = "json"
    
    @Override
    Integer call() throws Exception {
        logger.info("Generating statistics for: ${inputFile}")
        
        try {
            File input = new File(inputFile)
            if (!input.exists()) {
                logger.error("Input file does not exist: ${inputFile}")
                System.err.println("Error: Input file does not exist: ${inputFile}")
                return 1
            }
            
            // Parse HTML document
            Document document = Jsoup.parse(input, "UTF-8")
            
            // Calculate statistics
            def stats = [:]
            
            // Basic document info
            stats.title = document.title()
            stats.doctype = document.documentType()?.toString() ?: "None"
            stats.charset = document.charset().name()
            stats.fileSize = input.length()
            
            // Element counts
            def allElements = document.getAllElements()
            stats.totalElements = allElements.size()
            
            // Count by tag name
            def tagCounts = [:]
            allElements.each { element ->
                def tag = element.tagName().toLowerCase()
                tagCounts[tag] = (tagCounts[tag] ?: 0) + 1
            }
            stats.elementsByTag = tagCounts
            
            // Link analysis
            def links = document.select("a[href]")
            stats.totalLinks = links.size()
            
            def linkTypes = [
                internal: 0,
                external: 0,
                mailto: 0,
                javascript: 0,
                anchor: 0,
                other: 0
            ]
            
            links.each { link ->
                def href = link.attr("href").trim()
                if (href.startsWith("#")) {
                    linkTypes.anchor++
                } else if (href.startsWith("mailto:")) {
                    linkTypes.mailto++
                } else if (href.startsWith("javascript:")) {
                    linkTypes.javascript++
                } else if (href.startsWith("http://") || href.startsWith("https://")) {
                    linkTypes.external++
                } else if (href) {
                    linkTypes.internal++
                } else {
                    linkTypes.other++
                }
            }
            stats.linkTypes = linkTypes
            
            // Images
            stats.totalImages = document.select("img").size()
            
            // Form elements
            stats.forms = document.select("form").size()
            stats.inputFields = document.select("input").size()
            
            // Scripts and styles
            stats.scripts = document.select("script").size()
            stats.styleSheets = document.select("link[rel=stylesheet]").size()
            stats.inlineStyles = document.select("style").size()
            
            // DOM depth
            def maxDepth = 0
            def calculateDepth
            calculateDepth = { element, depth ->
                if (depth > maxDepth) maxDepth = depth
                element.children().each { child ->
                    calculateDepth(child, depth + 1)
                }
            }
            calculateDepth(document.body(), 0)
            stats.maxDOMDepth = maxDepth
            
            // Text statistics
            def text = document.text()
            stats.textLength = text.length()
            stats.wordCount = text.split(/\s+/).length
            
            // Output data in requested format
            File output = new File(outputFile)
            if (format.toLowerCase() == "json") {
                output.text = new JsonBuilder(stats).toPrettyString()
            } else {
                // Text format
                def result = new StringBuilder()
                result.append("Web Page Statistics\n")
                result.append("=================\n\n")
                
                result.append("Document Information:\n")
                result.append("  Title: ${stats.title}\n")
                result.append("  Doctype: ${stats.doctype}\n")
                result.append("  Charset: ${stats.charset}\n")
                result.append("  File Size: ${stats.fileSize} bytes\n\n")
                
                result.append("Element Counts:\n")
                result.append("  Total Elements: ${stats.totalElements}\n")
                result.append("  Top 10 Elements by Tag:\n")
                stats.elementsByTag.sort { -it.value }.take(10).each { tag, count ->
                    result.append("    ${tag}: ${count}\n")
                }
                result.append("\n")
                
                result.append("Link Analysis:\n")
                result.append("  Total Links: ${stats.totalLinks}\n")
                stats.linkTypes.each { type, count ->
                    result.append("  ${type.capitalize()}: ${count}\n")
                }
                result.append("\n")
                
                result.append("Other Elements:\n")
                result.append("  Images: ${stats.totalImages}\n")
                result.append("  Forms: ${stats.forms}\n")
                result.append("  Input Fields: ${stats.inputFields}\n")
                result.append("  Scripts: ${stats.scripts}\n")
                result.append("  Stylesheets: ${stats.styleSheets}\n")
                result.append("  Inline Styles: ${stats.inlineStyles}\n\n")
                
                result.append("Structure:\n")
                result.append("  Maximum DOM Depth: ${stats.maxDOMDepth}\n\n")
                
                result.append("Content:\n")
                result.append("  Text Length: ${stats.textLength} characters\n")
                result.append("  Word Count: ${stats.wordCount} words\n")
                
                output.text = result.toString()
            }
            
            logger.info("Successfully generated statistics to ${outputFile}")
            System.out.println("Successfully generated statistics to ${outputFile}")
            return 0
            
        } catch (Exception e) {
            logger.error("Error generating statistics: ${e.message}", e)
            System.err.println("Error generating statistics: ${e.message}")
            return 1
        }
    }
}
