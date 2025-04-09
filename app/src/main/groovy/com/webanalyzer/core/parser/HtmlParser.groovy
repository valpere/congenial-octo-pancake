package com.webanalyzer.core.parser

import groovy.json.JsonBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.Charset

/**
 * Handles HTML parsing and DOM to JSON conversion.
 */
class HtmlParser {
    private static final Logger logger = LoggerFactory.getLogger(HtmlParser.class)
    
    /**
     * Parse an HTML file and convert its DOM to a JSON structure.
     *
     * @param file The HTML file to parse
     * @param charset The character encoding to use
     * @param includeText Whether to include text content in the JSON
     * @return A JsonBuilder containing the DOM structure
     */
    JsonBuilder parseToJson(File file, Charset charset, boolean includeText) {
        logger.debug("Parsing file: ${file.absolutePath} with charset: ${charset.displayName()}")
        
        Document document = Jsoup.parse(file, charset.name())
        def domMap = elementToJson(document.root(), includeText)
        
        return new JsonBuilder(domMap)
    }
    
    /**
     * Parse HTML text and convert its DOM to a JSON structure.
     *
     * @param html The HTML string to parse
     * @param includeText Whether to include text content in the JSON
     * @return A JsonBuilder containing the DOM structure
     */
    JsonBuilder parseHtmlToJson(String html, boolean includeText) {
        logger.debug("Parsing HTML string (length: ${html.length()})")
        
        Document document = Jsoup.parse(html)
        def domMap = elementToJson(document.root(), includeText)
        
        return new JsonBuilder(domMap)
    }
    
    /**
     * Recursively convert a DOM element to a JSON-compatible map structure.
     *
     * @param element The DOM element to convert
     * @param includeText Whether to include text content
     * @return A map representing the element and its children
     */
    private Map elementToJson(Element element, boolean includeText) {
        def result = [
            tagName: element.tagName(),
            attributes: element.attributes().collectEntries { attr ->
                [(attr.key): attr.value]
            }
        ]
        
        if (includeText && !element.ownText().isEmpty()) {
            result.text = element.ownText()
        }
        
        // Only include children array if there are child elements
        if (element.children().size() > 0) {
            result.children = element.children().collect { child ->
                elementToJson(child, includeText)
            }
        }
        
        return result
    }
}
