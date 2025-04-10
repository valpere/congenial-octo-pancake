# Web Page Analyzer Testing Guide

This document outlines the testing strategy and procedures for the Web Page Analyzer project. It provides guidance on running tests, extending the test suite, and maintaining test coverage in accordance with project requirements.

## Testing Strategy

The Web Page Analyzer testing strategy follows a comprehensive approach with multiple layers:

1. **Unit Tests** - Test individual components in isolation
2. **Integration Tests** - Test end-to-end command flows
3. **Specialized Tests** - Focus on key functionality like UTF-8 handling

The project requirement specifies a minimum of 80% test coverage, which is enforced through the JaCoCo code coverage tool.

## Test Structure

Tests are organized into the following categories:

- **Core Service Tests** - Located in `src/test/groovy/com/webanalyzer/core/*Test.groovy`
- **Command Integration Tests** - Located in `src/test/groovy/com/webanalyzer/cli/commands/*IntegrationTest.groovy`
- **Special Case Tests** - Located in `src/test/groovy/com/webanalyzer/test/*Test.groovy`
- **Sample HTML Files** - Located in `src/test/resources/sample-html/*.html`

## Running Tests

### Running All Tests

To run all tests with coverage reporting:

```bash
./gradlew testWithCoverage
```

This will:
1. Execute all tests
2. Generate a JaCoCo coverage report
3. Verify that coverage meets the minimum 80% threshold
4. Output the location of the HTML coverage report

### Running Specific Test Categories

To run only unit tests:

```bash
./gradlew test --tests "com.webanalyzer.core.*Test"
```

To run only integration tests:

```bash
./gradlew test --tests "com.webanalyzer.cli.commands.*IntegrationTest"
```

To run a specific test class:

```bash
./gradlew test --tests "com.webanalyzer.core.parser.HtmlParserTest"
```

## Sample HTML Files

The test suite includes a variety of sample HTML files designed to test different aspects of the application:

- `simple.html` - Basic HTML structure
- `complex-structure.html` - Deeply nested DOM hierarchy
- `international.html` - Multi-language content with various character sets
- `malformed.html` - HTML with syntax errors to test error handling
- `javascript.html` - Dynamic content rendered with JavaScript
- `forms.html` - Various form structures and input types
- `tables.html` - Complex table structures
- `seo-test.html` - SEO-related elements and metadata

These files are automatically generated when running tests, but can also be manually created by running:

```bash
./gradlew createSampleHtmlFiles
```

## Adding New Tests

### Adding a Unit Test

1. Create a new test class in the appropriate package under `src/test/groovy/com/webanalyzer/core/`
2. Extend `spock.lang.Specification`
3. Follow the naming convention `*Test.groovy`
4. Include the new test class in the `WebPageAnalyzerTestSuite.groovy` file

Example:

```groovy
package com.webanalyzer.core.myfeature

import spock.lang.Specification

class MyFeatureTest extends Specification {
    def "should perform expected operation"() {
        given:
        def myFeature = new MyFeature()
        
        when:
        def result = myFeature.operate()
        
        then:
        result == expectedResult
    }
}
```

### Adding an Integration Test

1. Create a new test class in `src/test/groovy/com/webanalyzer/cli/commands/`
2. Extend `spock.lang.Specification`
3. Follow the naming convention `*IntegrationTest.groovy`
4. Include the new test class in the `WebPageAnalyzerTestSuite.groovy` file

Example:

```groovy
package com.webanalyzer.cli.commands

import com.webanalyzer.cli.WebPageAnalyzer
import picocli.CommandLine
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class MyCommandIntegrationTest extends Specification {
    @TempDir
    Path tempDir
    
    def "should execute command successfully"() {
        given:
        def outputFile = tempDir.resolve("output.txt").toFile()
        
        when:
        def exitCode = executeCommand("my-command", "arg1", outputFile.absolutePath)
        
        then:
        exitCode == 0
        outputFile.exists()
        outputFile.text.contains("Expected output")
    }
    
    private int executeCommand(String... args) {
        def app = new WebPageAnalyzer()
        return new CommandLine(app).execute(args)
    }
}
```

## Testing Best Practices

1. **Test Independence** - Each test should be independent and not rely on the state created by other tests
2. **Meaningful Assertions** - Test specific behaviors with clear assertions
3. **Test Edge Cases** - Include tests for boundary conditions and error handling
4. **UTF-8 Handling** - Pay special attention to proper UTF-8 character handling
5. **Resource Cleanup** - Use `@TempDir` for file operations to ensure automatic cleanup
6. **Timeouts** - Use `@Timeout` annotations for tests that may hang
7. **Descriptive Names** - Use descriptive method names in the format `"should do something when condition"`

## Common Test Utilities

### TempDir Usage

The `@TempDir` annotation provides a temporary directory that is automatically created before each test and deleted after the test completes:

```groovy
@TempDir
Path tempDir

def "should write to file"() {
    given:
    def tempFile = tempDir.resolve("test.txt").toFile()
    
    when:
    tempFile.text = "Test content"
    
    then:
    tempFile.exists()
    tempFile.text == "Test content"
}
```

### File Creation Helper

A common pattern for creating temporary HTML files for testing:

```groovy
private File createTempHtmlFile(String content) {
    def file = Files.createFile(tempDir.resolve("test.html")).toFile()
    file.setText(content, "UTF-8")
    return file
}
```

### Command Execution Helper

For integration tests that execute CLI commands:

```groovy
private int executeCommand(String... args) {
    def app = new WebPageAnalyzer()
    return new CommandLine(app).execute(args)
}
```

## Troubleshooting Common Test Issues

### Tests Hanging

If tests seem to hang, check:
- Timeouts for dynamic content loading
- Browser/WebDriver initialization and cleanup
- Network connectivity for tests that access external resources

Solution: Add `@Timeout` annotations to limit test execution time:

```groovy
@Timeout(30)  // Timeout in seconds
def "should not hang"() {
    // Test code
}
```

### Character Encoding Issues

For tests involving international characters:
- Ensure test files are created with UTF-8 encoding
- Check that file reading operations specify UTF-8
- Verify JSON processing preserves UTF-8 characters

### Flaky Tests

For tests that occasionally fail:
- Add appropriate wait times for dynamic elements
- Use more specific selectors
- Add better error logging
- Consider using retrying test extensions

## Continuous Integration

The test suite is designed to run in continuous integration environments. The JaCoCo XML report (`build/reports/coverage/coverage.xml`) can be used with CI tools to track coverage metrics over time.

## Version Control Considerations

- Do not commit generated test artifacts to version control
- Keep sample HTML files in the repository
- Document test failures in issue reports with detailed reproduction steps
