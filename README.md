# Web Page Analyzer

A command-line tool written in Groovy for retrieving, parsing, analyzing, and transforming web pages.

## Features

- Parse HTML files to JSON DOM representation
- Fetch web pages by URL (static or dynamic)
- Extract elements using CSS selectors
- Generate statistics about web pages
- Compare HTML documents
- Transform HTML to other formats

## Requirements

- Java 21
- Gradle 8.0.13

## Building the Project

Clone this repository and build the project using Gradle:

```bash
./gradlew build
```

To create a distributable package:

```bash
./gradlew dist
```

This will create a standalone JAR with all dependencies and wrapper scripts in `build/distribution/`.

## Usage

### Using the Gradle run task:

```bash
./gradlew run --args="parse input.html output.json"
```

### Using the distribution package:

```bash
cd build/distribution
./web-page-analyzer parse input.html output.json
```

## Commands

### Parse HTML to JSON

Parses an HTML file and converts its DOM structure to JSON.

```bash
web-page-analyzer parse <input-html-file> <output-json-file> [--pretty] [--include-text=true|false] [--encoding=UTF-8]
```

Options:
- `--pretty` - Format the JSON output with indentation
- `--include-text` - Include text content in the JSON (default: true)
- `--encoding` - File encoding (default: UTF-8)

### Fetch Web Page

Retrieves a web page by URL and saves it as an HTML file.

```bash
web-page-analyzer read <url> <output-html-file> [--dynamic] [--wait=5000] [--timeout=30] [--user-agent="..."]
```

Options:
- `--dynamic` - Use a headless browser to render JavaScript (default: false)
- `--wait` - Time to wait for dynamic content to load in ms (default: 5000)
- `--timeout` - Connection timeout in seconds (default: 30)
- `--user-agent` - Custom user agent string

### Extract Elements

Extract specific elements from an HTML file using CSS selectors.

```bash
web-page-analyzer extract <input-html-file> <css-selector> <output-file> [--format=json|csv|txt] [--attributes=attr1,attr2,...]
```

Options:
- `--format` - Output format (json, csv, txt) (default: json)
- `--attributes` - Comma-separated list of attributes to extract (default: all)

### Generate Statistics

Generate statistics about an HTML document.

```bash
web-page-analyzer stats <input-html-file> <output-file> [--format=json|txt]
```

Options:
- `--format` - Output format (json, txt) (default: json)

## Project Structure

```
web-page-analyzer/
├── src/
│   ├── main/
│   │   ├── groovy/
│   │   │   └── com/webanalyzer/
│   │   │       ├── cli/ (Command line interface)
│   │   │       │   ├── WebPageAnalyzer.groovy (Main class)
│   │   │       │   └── commands/ (Command implementations)
│   │   │       └── core/ (Core functionality)
│   │   │           ├── parser/ (HTML parsing)
│   │   │           ├── fetcher/ (Web page fetching)
│   │   │           ├── transformer/ (Format transformations)
│   │   │           └── analyzer/ (Analysis tools)
│   │   └── resources/
│   │       └── logback.xml (Logging configuration)
│   └── test/
│       └── groovy/
│           └── com/webanalyzer/ (Test classes)
├── build.gradle
├── settings.gradle
└── README.md
```

## Adding Dynamic Page Support

To enable the dynamic page fetching feature (with JavaScript rendering), add the following dependencies to your `build.gradle`:

```groovy
dependencies {
    // For dynamic page rendering
    implementation 'org.seleniumhq.selenium:selenium-java:4.17.0'
    implementation 'org.seleniumhq.selenium:selenium-chrome-driver:4.17.0'
}
```

You'll also need to have Chrome installed and the appropriate ChromeDriver for your Chrome version.

## License

[MIT License](LICENSE)
