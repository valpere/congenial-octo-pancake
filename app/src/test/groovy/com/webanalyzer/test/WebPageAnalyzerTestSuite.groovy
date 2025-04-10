package com.webanalyzer.test

import com.webanalyzer.cli.commands.*
import com.webanalyzer.core.analyzer.HtmlAnalyzerTest
import com.webanalyzer.core.comparator.HtmlComparatorTest
import com.webanalyzer.core.extractor.ElementExtractorTest
import com.webanalyzer.core.fetcher.WebPageFetcherTest
import com.webanalyzer.core.parser.HtmlParserTest
import com.webanalyzer.core.transformer.HtmlTransformerTest
import org.junit.platform.runner.JUnitPlatform
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite
import org.junit.runner.RunWith

/**
 * Test suite for the Web Page Analyzer application.
 *
 * This class organizes all unit and integration tests into a cohesive suite
 * that can be executed together to verify the entire application.
 */
@RunWith(JUnitPlatform.class)
@Suite
@SelectClasses([
    // Core service unit tests
    HtmlParserTest.class,
    WebPageFetcherTest.class,
    ElementExtractorTest.class,
    HtmlTransformerTest.class,
    HtmlAnalyzerTest.class,
    HtmlComparatorTest.class,

    // CLI command integration tests
    ParseCommandIntegrationTest.class,
    ReadCommandIntegrationTest.class,
    ExtractCommandIntegrationTest.class,
    StatsCommandIntegrationTest.class,
    CompareCommandIntegrationTest.class,
    TransformCommandIntegrationTest.class,

    // Special case tests
    Utf8HandlingTest.class
])
class WebPageAnalyzerTestSuite {
  // The suite is defined by the annotations above
}
