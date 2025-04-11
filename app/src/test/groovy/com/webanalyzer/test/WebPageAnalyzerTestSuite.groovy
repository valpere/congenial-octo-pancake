package com.webanalyzer.test

import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite
import org.junit.platform.suite.api.SuiteDisplayName

/**
 * Test suite for the Web Page Analyzer application.
 *
 * This class organizes all unit and integration tests into a cohesive suite
 * that can be executed together to verify the entire application.
 */
@Suite
@SuiteDisplayName("Web Page Analyzer Test Suite")
@SelectPackages(["com.webanalyzer.core", "com.webanalyzer.cli.commands", "com.webanalyzer.test"])
class WebPageAnalyzerTestSuite {
  // The suite is defined by the annotations above
}
