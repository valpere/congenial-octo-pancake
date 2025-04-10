package com.webanalyzer.test

import com.webanalyzer.core.analyzer.AnalyzerOptions
import com.webanalyzer.core.analyzer.HtmlAnalyzer
import com.webanalyzer.core.extractor.ElementExtractor
import com.webanalyzer.core.extractor.ExtractorOptions
import com.webanalyzer.core.parser.HtmlParser
import com.webanalyzer.core.transformer.HtmlTransformer
import com.webanalyzer.core.transformer.TransformOptions
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Specialized test for UTF-8 character handling across all components.
 *
 * This test focuses on verifying that non-ASCII characters are properly preserved
 * throughout processing and not converted to Unicode escape sequences in JSON output.
 */
class Utf8HandlingTest extends Specification {

  @TempDir
  Path tempDir

  // Test content with a variety of non-ASCII characters from different languages
  static final String UTF8_TEST_HTML = '''<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>UTF-8 Характер Тест</title>
</head>
<body>
    <h1>UTF-8 Character Test / Тест символов UTF-8</h1>
    
    <div id="cyrillic" class="language-section">
        <h2>Cyrillic / Кириллица</h2>
        <p>Русский: Все люди рождаются свободными и равными в своем достоинстве и правах.</p>
        <p>Українська: Всі люди народжуються вільними і рівними у своїй гідності та правах.</p>
        <p>Беларуская: Усе людзі нараджаюцца свабоднымі і роўнымі ў сваёй годнасці і правах.</p>
    </div>
    
    <div id="asian" class="language-section">
        <h2>Asian Languages / 亚洲语言</h2>
        <p>简体中文: 人人生而自由，在尊严和权利上一律平等。</p>
        <p>繁體中文: 人人生而自由，在尊嚴和權利上一律平等。</p>
        <p>日本語: すべての人間は、生まれながらにして自由であり、かつ、尊厳と権利とについて平等である。</p>
        <p>한국어: 모든 인간은 태어날 때부터 자유로우며 그 존엄과 권리에 있어 동등하다.</p>
    </div>
    
    <div id="other" class="language-section">
        <h2>Other Scripts / أبجديات أخرى</h2>
        <p>العربية: يولد جميع الناس أحرارًا متساوين في الكرامة والحقوق.</p>
        <p>עברית: כל בני האדם נולדו בני חורין ושווים בערכם ובזכויותיהם.</p>
        <p>Ελληνικά: Όλοι οι άνθρωποι γεννιούνται ελεύθεροι και ίσοι στην αξιοπρέπεια και τα δικαιώματα.</p>
        <p>हिन्दी: सभी मनुष्य जन्म से स्वतंत्र और गरिमा और अधिकारों में समान होते हैं।</p>
        <p>ไทย: มนุษย์ทั้งหลายเกิดมามีอิสระและเสมอภาคกันในศักดิ์ศรีและสิทธิ</p>
    </div>
    
    <div id="special" class="language-section">
        <h2>Special Characters / Caractères Spéciaux</h2>
        <p>Symbols: ©®™§¶†‡♠♣♥♦☺☻♫♪♂♀★☆☼♡</p>
        <p>Math: ∑∏√∞≠≈∆∇∫≤≥±÷×µ∂∩∪Ω</p>
        <p>Currency: €£¥$¢ƒ₹₽₩₦₺₴₸₼</p>
    </div>
</body>
</html>'''

  def "should preserve UTF-8 characters in HTML parser output"() {
    given:
    def htmlFile = createTempHtmlFile(UTF8_TEST_HTML)
    def parser = new HtmlParser()

    when:
    def jsonResult = parser.parseToJson(htmlFile, StandardCharsets.UTF_8, true, false)

    then:
    // Check for the presence of various non-ASCII characters in the output
    jsonResult.contains("UTF-8 Характер Тест")
    jsonResult.contains("Кириллица")
    jsonResult.contains("Українська")
    jsonResult.contains("亚洲语言")
    jsonResult.contains("日本語")
    jsonResult.contains("한국어")
    jsonResult.contains("العربية")
    jsonResult.contains("Ελληνικά")
    jsonResult.contains("हिन्दी")

    // Check that Unicode escape sequences are not present
    !jsonResult.contains("\\u")
  }

  def "should preserve UTF-8 characters when transforming to different formats"() {
    given:
    def htmlFile = createTempHtmlFile(UTF8_TEST_HTML)
    def transformer = new HtmlTransformer()
    def options = new TransformOptions()

    when:
    def markdownResult = transformer.transformFile(htmlFile, "markdown", options)
    def jsonResult = transformer.transformFile(htmlFile, "json", options)
    def plainResult = transformer.transformFile(htmlFile, "plain", options)

    then:
    // Verify characters in Markdown output
    markdownResult.contains("UTF-8 Характер Тест")
    markdownResult.contains("简体中文")
    markdownResult.contains("Ελληνικά")

    // Verify characters in JSON output
    jsonResult.contains("UTF-8 Характер Тест")
    jsonResult.contains("한국어")
    jsonResult.contains("العربية")
    !jsonResult.contains("\\u")

    // Verify characters in plain text output
    plainResult.contains("UTF-8 Характер Тест")
    plainResult.contains("हिन्दी")
    plainResult.contains("ไทย")
  }

  def "should preserve UTF-8 characters in element extraction"() {
    given:
    def htmlFile = createTempHtmlFile(UTF8_TEST_HTML)
    def extractor = new ElementExtractor()
    def options = new ExtractorOptions(format: "json")

    when:
    def cyrillicResult = extractor.extractFromFile(htmlFile, "#cyrillic p", options)
    def asianResult = extractor.extractFromFile(htmlFile, "#asian p", options)
    def otherResult = extractor.extractFromFile(htmlFile, "#other p", options)

    then:
    // Cyrillic content extraction
    cyrillicResult.contains("Русский")
    cyrillicResult.contains("Українська")
    cyrillicResult.contains("Беларуская")

    // Asian language content extraction
    asianResult.contains("简体中文")
    asianResult.contains("繁體中文")
    asianResult.contains("日本語")
    asianResult.contains("한국어")

    // Other scripts content extraction
    otherResult.contains("العربية")
    otherResult.contains("עברית")
    otherResult.contains("Ελληνικά")
    otherResult.contains("हिन्दी")

    // No Unicode escape sequences
    !cyrillicResult.contains("\\u")
    !asianResult.contains("\\u")
    !otherResult.contains("\\u")
  }

  def "should parse UTF-8 JSON output correctly"() {
    given:
    def htmlFile = createTempHtmlFile(UTF8_TEST_HTML)
    def parser = new HtmlParser()

    when:
    def jsonString = parser.parseToJson(htmlFile, StandardCharsets.UTF_8, true, false)
    def jsonSlurper = new JsonSlurper()
    def parsedJson = jsonSlurper.parseText(jsonString)

    then:
    // Verify we can correctly parse the JSON with UTF-8 characters
    parsedJson != null

    // Retrieve values from the parsed JSON structure
    def title = parsedJson.children.find { it.tagName == "head" }?.children?.find { it.tagName == "title" }?.text

    // Verify the retrieved values contain the correct UTF-8 characters
    title == "UTF-8 Характер Тест"
  }

  def "should preserve UTF-8 characters in analyzer output"() {
    given:
    def htmlFile = createTempHtmlFile(UTF8_TEST_HTML)
    def analyzer = new HtmlAnalyzer()
    def options = new AnalyzerOptions(includeAll: true)

    when:
    def stats = analyzer.analyzeFile(htmlFile, options)

    then:
    // Convert to JSON to check character preservation
    def jsonGenerator = new groovy.json.JsonGenerator.Options()
        .disableUnicodeEscaping()
        .build()
    def jsonResult = jsonGenerator.toJson(stats)

    // Verify UTF-8 characters are preserved
    jsonResult.contains("UTF-8 Характер Тест") // Title
    jsonResult.contains("Кириллица") // Heading
    jsonResult.contains("Українська") // Text content
    jsonResult.contains("简体中文") // Asian text

    // Verify no Unicode escape sequences
    !jsonResult.contains("\\u")
  }

  private File createTempHtmlFile(String content) {
    def file = Files.createFile(tempDir.resolve("utf8-test.html")).toFile()
    file.setText(content, "UTF-8")
    return file
  }
}
