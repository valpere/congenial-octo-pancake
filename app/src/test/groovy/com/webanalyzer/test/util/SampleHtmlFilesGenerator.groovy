package com.webanalyzer.test.util

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Helper class to generate sample HTML files for testing.
 *
 * This class creates a variety of HTML files to test different aspects
 * of the Web Page Analyzer, including complex DOM structures, internationalization,
 * and malformed HTML.
 */
class SampleHtmlFilesGenerator {

  static void main(String[] args) {
    def generator = new SampleHtmlFilesGenerator()

    // Create output directory
    def outputDir = Paths.get("src/test/resources/sample-html")
    Files.createDirectories(outputDir)

    // Generate sample files
    generator.generateSimpleHtml(outputDir)
    generator.generateComplexStructureHtml(outputDir)
    generator.generateInternationalHtml(outputDir)
    generator.generateMalformedHtml(outputDir)
    generator.generateJavaScriptHtml(outputDir)
    generator.generateFormHtml(outputDir)
    generator.generateTableHtml(outputDir)
    generator.generateSeoTestHtml(outputDir)

    println "Sample HTML files generated in ${outputDir.toAbsolutePath()}"
  }

  /**
   * Generate a simple HTML file for basic testing.
   */
  void generateSimpleHtml(Path outputDir) {
    def html = '''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simple HTML Test</title>
    <meta name="description" content="A simple HTML file for testing">
</head>
<body>
    <header>
        <h1>Simple HTML Test Page</h1>
        <nav>
            <ul>
                <li><a href="/">Home</a></li>
                <li><a href="/about">About</a></li>
                <li><a href="/contact">Contact</a></li>
            </ul>
        </nav>
    </header>
    
    <main>
        <section>
            <h2>Test Section</h2>
            <p>This is a paragraph of text in the main content area.</p>
            <p>This is another paragraph with some <strong>bold text</strong> and <em>italic text</em>.</p>
            
            <h3>Subsection</h3>
            <p>More text in a subsection.</p>
            
            <a href="https://example.com">External Link</a>
        </section>
    </main>
    
    <footer>
        <p>&copy; 2023 Test Company</p>
    </footer>
</body>
</html>'''

    writeFile(outputDir, "simple.html", html)
  }

  /**
   * Generate HTML with complex DOM structure for testing structural analysis.
   */
  void generateComplexStructureHtml(Path outputDir) {
    def html = '''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Complex Structure Test</title>
    <meta name="description" content="An HTML file with complex DOM structure">
    <link rel="stylesheet" href="styles.css">
    <script src="script.js"></script>
</head>
<body>
    <header class="site-header">
        <div class="logo-container">
            <img src="logo.png" alt="Logo" class="logo">
        </div>
        <nav class="main-nav">
            <ul class="nav-list">
                <li class="nav-item"><a href="/" class="nav-link">Home</a></li>
                <li class="nav-item dropdown">
                    <a href="/products" class="nav-link">Products</a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-item"><a href="/products/category1">Category 1</a></li>
                        <li class="dropdown-item"><a href="/products/category2">Category 2</a></li>
                        <li class="dropdown-item"><a href="/products/category3">Category 3</a></li>
                    </ul>
                </li>
                <li class="nav-item"><a href="/about" class="nav-link">About</a></li>
                <li class="nav-item"><a href="/contact" class="nav-link">Contact</a></li>
            </ul>
        </nav>
    </header>
    
    <main class="site-main">
        <aside class="sidebar">
            <div class="sidebar-widget">
                <h3 class="widget-title">Categories</h3>
                <ul class="category-list">
                    <li class="category-item"><a href="/category1">Category 1</a></li>
                    <li class="category-item"><a href="/category2">Category 2</a></li>
                    <li class="category-item"><a href="/category3">Category 3</a></li>
                </ul>
            </div>
            <div class="sidebar-widget">
                <h3 class="widget-title">Recent Posts</h3>
                <ul class="post-list">
                    <li class="post-item"><a href="/post1">Post 1</a></li>
                    <li class="post-item"><a href="/post2">Post 2</a></li>
                    <li class="post-item"><a href="/post3">Post 3</a></li>
                </ul>
            </div>
        </aside>
        
        <div class="content-area">
            <article class="post">
                <header class="post-header">
                    <h1 class="post-title">Complex Structure Test</h1>
                    <div class="post-meta">
                        <span class="post-date">January 1, 2023</span>
                        <span class="post-author">by <a href="/author">Test Author</a></span>
                    </div>
                </header>
                
                <div class="post-content">
                    <p>This is a paragraph in the main content with some <strong>bold text</strong> and <em>italic text</em>.</p>
                    
                    <blockquote class="quote">
                        <p>This is a blockquote with a nested paragraph.</p>
                        <footer class="quote-footer">- Quote Author</footer>
                    </blockquote>
                    
                    <section class="post-section">
                        <h2 class="section-title">Section Title</h2>
                        <p>Paragraph in a nested section.</p>
                        
                        <div class="image-gallery">
                            <figure class="gallery-item">
                                <img src="image1.jpg" alt="Image 1" class="gallery-image">
                                <figcaption class="image-caption">Image 1 Caption</figcaption>
                            </figure>
                            <figure class="gallery-item">
                                <img src="image2.jpg" alt="Image 2" class="gallery-image">
                                <figcaption class="image-caption">Image 2 Caption</figcaption>
                            </figure>
                        </div>
                    </section>
                    
                    <section class="post-section">
                        <h2 class="section-title">Another Section</h2>
                        <p>Another paragraph in a different section.</p>
                        
                        <div class="code-block">
                            <pre><code>
function testFunction() {
    console.log("Hello, world!");
}
                            </code></pre>
                        </div>
                    </section>
                </div>
                
                <footer class="post-footer">
                    <div class="post-tags">
                        <span class="tag-label">Tags:</span>
                        <a href="/tag1" class="tag">Tag 1</a>
                        <a href="/tag2" class="tag">Tag 2</a>
                        <a href="/tag3" class="tag">Tag 3</a>
                    </div>
                    
                    <div class="post-share">
                        <span class="share-label">Share:</span>
                        <a href="#" class="share-link">Facebook</a>
                        <a href="#" class="share-link">Twitter</a>
                        <a href="#" class="share-link">LinkedIn</a>
                    </div>
                </footer>
            </article>
            
            <section class="comments-section">
                <h2 class="comments-title">Comments</h2>
                
                <div class="comment">
                    <div class="comment-meta">
                        <span class="comment-author">Comment Author</span>
                        <span class="comment-date">January 2, 2023</span>
                    </div>
                    <div class="comment-content">
                        <p>This is a comment on the article.</p>
                    </div>
                </div>
                
                <div class="comment">
                    <div class="comment-meta">
                        <span class="comment-author">Another Commenter</span>
                        <span class="comment-date">January 3, 2023</span>
                    </div>
                    <div class="comment-content">
                        <p>This is another comment with <a href="#">a link</a>.</p>
                    </div>
                </div>
            </section>
        </div>
    </main>
    
    <footer class="site-footer">
        <div class="footer-widgets">
            <div class="footer-widget">
                <h3 class="widget-title">About Us</h3>
                <p>This is a complex HTML structure test file.</p>
            </div>
            <div class="footer-widget">
                <h3 class="widget-title">Quick Links</h3>
                <ul class="quick-links">
                    <li><a href="/page1">Page 1</a></li>
                    <li><a href="/page2">Page 2</a></li>
                    <li><a href="/page3">Page 3</a></li>
                </ul>
            </div>
            <div class="footer-widget">
                <h3 class="widget-title">Contact</h3>
                <address class="contact-info">
                    <p>123 Test Street</p>
                    <p>Test City, TC 12345</p>
                    <p>Email: <a href="mailto:test@example.com">test@example.com</a></p>
                </address>
            </div>
        </div>
        
        <div class="copyright">
            <p>&copy; 2023 Test Company. All rights reserved.</p>
        </div>
    </footer>
</body>
</html>'''

    writeFile(outputDir, "complex-structure.html", html)
  }

  /**
   * Generate HTML with international characters for testing character encoding.
   */
  void generateInternationalHtml(Path outputDir) {
    def html = '''<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>International Characters Test</title>
    <meta name="description" content="Test file with characters from different languages">
</head>
<body>
    <h1>International Characters Test</h1>
    
    <h2>Cyrillic (Russian)</h2>
    <p>Привет мир! Это тестовый текст на русском языке.</p>
    
    <h2>Chinese (Simplified)</h2>
    <p>你好世界！这是中文测试文本。</p>
    
    <h2>Japanese</h2>
    <p>こんにちは世界！これは日本語のテストテキストです。</p>
    
    <h2>Korean</h2>
    <p>안녕하세요 세계! 이것은 한국어 테스트 텍스트입니다.</p>
    
    <h2>Arabic</h2>
    <p>مرحبا بالعالم! هذا نص اختبار باللغة العربية.</p>
    
    <h2>Hebrew</h2>
    <p>שלום עולם! זהו טקסט בדיקה בעברית.</p>
    
    <h2>Greek</h2>
    <p>Γειά σου Κόσμε! Αυτό είναι ένα δοκιμαστικό κείμενο στα ελληνικά.</p>
    
    <h2>Thai</h2>
    <p>สวัสดีชาวโลก! นี่คือข้อความทดสอบภาษาไทย</p>
    
    <h2>Hindi</h2>
    <p>नमस्ते दुनिया! यह हिंदी में एक परीक्षण पाठ है।</p>
    
    <h2>Special Characters</h2>
    <p>© ® ™ • ½ ¼ ¾ € £ ¥ ← → ↑ ↓ ♠ ♣ ♥ ♦ ☺ ☻ ♫ ♪</p>
</body>
</html>'''

    writeFile(outputDir, "international.html", html)
  }

  /**
   * Generate malformed HTML for testing error handling.
   */
  void generateMalformedHtml(Path outputDir) {
    def html = '''<!DOCTYPE html>
<html>
<head>
    <title>Malformed HTML Test
    <meta charset="UTF-8">
    <meta name="description" content="Test file with malformed HTML structure">
</head>
<body>
    <h1>Malformed HTML Test</h1>
    
    <!-- Unclosed tags -->
    <div class="unclosed">
        <p>This paragraph tag is not closed
        <p>This is another paragraph
    </div>
    
    <!-- Incorrectly nested tags -->
    <span><strong>This is incorrectly nested</span></strong>
    
    <!-- Missing end tags -->
    <ul>
        <li>Item 1
        <li>Item 2
        <li>Item 3
    </ul>
    
    <!-- Missing attributes quotes -->
    <a href=https://example.com class=link>Link with missing quotes</a>
    
    <!-- Unescaped special characters -->
    <p>These characters should be escaped: < > & " '</p>
    
    <!-- Unclosed comment tag
    <p>This paragraph is inside an unclosed comment
    
    <!-- Empty attributes -->
    <input required disabled>
    
    <!-- Invalid attributes -->
    <div class="valid" invalid-attr="test">Invalid attribute</div>
    
    <!-- Duplicate IDs -->
    <div id="duplicate">First element with this ID</div>
    <div id="duplicate">Second element with this ID</div>
    
    <!-- Improperly closed void elements -->
    <img src="image.jpg"></img>
    <br></br>
    
    <!-- Mismatched end tags -->
    <div>This has a mismatched end tag</span>
    
    <!-- HTML after </html> -->
</body>
</html>

<p>This paragraph is after the HTML end tag</p>'''

    writeFile(outputDir, "malformed.html", html)
  }

  /**
   * Generate HTML with JavaScript for testing dynamic content handling.
   */
  void generateJavaScriptHtml(Path outputDir) {
    def html = '''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>JavaScript Dynamic Content Test</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            margin: 0;
            padding: 20px;
            max-width: 800px;
            margin: 0 auto;
        }
        
        .hidden {
            display: none;
        }
        
        .container {
            border: 1px solid #ccc;
            padding: 20px;
            margin: 20px 0;
        }
        
        button {
            padding: 10px 15px;
            background: #4CAF50;
            color: white;
            border: none;
            cursor: pointer;
            margin: 5px;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        
        table, th, td {
            border: 1px solid #ddd;
        }
        
        th, td {
            padding: 10px;
            text-align: left;
        }
        
        th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>
    <h1>JavaScript Dynamic Content Test</h1>
    
    <div class="container">
        <h2>Delayed Content</h2>
        <p>The content below will appear after a 2-second delay:</p>
        <div id="delayed-content" class="hidden">
            <p>This content was dynamically added after page load.</p>
            <p>Current time: <span id="current-time"></span></p>
        </div>
    </div>
    
    <div class="container">
        <h2>Interactive Elements</h2>
        <button id="toggle-button">Toggle Visibility</button>
        <button id="add-element">Add New Element</button>
        <button id="change-style">Change Style</button>
        <button id="load-data">Load Data</button>
        
        <div id="toggle-target" class="container">
            <p>This content can be toggled.</p>
        </div>
        
        <div id="dynamic-container"></div>
        
        <table id="data-table" class="hidden">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Value</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <!-- Will be populated by JavaScript -->
            </tbody>
        </table>
    </div>
    
    <div class="container">
        <h2>AJAX Content</h2>
        <button id="ajax-button">Load External Content</button>
        <div id="ajax-container"></div>
    </div>
    
    <script>
        // Delayed content
        setTimeout(() => {
            const delayedContent = document.getElementById('delayed-content');
            delayedContent.classList.remove('hidden');
            
            const timeElement = document.getElementById('current-time');
            timeElement.textContent = new Date().toLocaleTimeString();
        }, 2000);
        
        // Toggle visibility
        document.getElementById('toggle-button').addEventListener('click', function() {
            const target = document.getElementById('toggle-target');
            if (target.style.display === 'none') {
                target.style.display = 'block';
            } else {
                target.style.display = 'none';
            }
        });
        
        // Add new element
        document.getElementById('add-element').addEventListener('click', function() {
            const container = document.getElementById('dynamic-container');
            const newElement = document.createElement('div');
            newElement.className = 'container';
            newElement.innerHTML = `<p>New element added at: ${new Date().toLocaleTimeString()}</p>`;
            container.appendChild(newElement);
        });
        
        // Change style
        document.getElementById('change-style').addEventListener('click', function() {
            const containers = document.querySelectorAll('.container');
            const randomColor = '#' + Math.floor(Math.random()*16777215).toString(16);
            containers.forEach(container => {
                container.style.backgroundColor = randomColor;
                container.style.color = isLightColor(randomColor) ? '#000' : '#fff';
            });
        });
        
        // Load data
        document.getElementById('load-data').addEventListener('click', function() {
            const table = document.getElementById('data-table');
            const tbody = table.querySelector('tbody');
            
            // Clear existing rows
            tbody.innerHTML = '';
            
            // Generate sample data
            for (let i = 1; i <= 5; i++) {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${i}</td>
                    <td>Item ${i}</td>
                    <td>${Math.floor(Math.random() * 1000)}</td>
                    <td>${Math.random() > 0.5 ? 'Active' : 'Inactive'}</td>
                `;
                tbody.appendChild(row);
            }
            
            // Show the table
            table.classList.remove('hidden');
        });
        
        // AJAX button (simulated)
        document.getElementById('ajax-button').addEventListener('click', function() {
            const container = document.getElementById('ajax-container');
            container.innerHTML = '<p>Loading...</p>';
            
            // Simulate AJAX request
            setTimeout(() => {
                container.innerHTML = `
                    <h3>External Content</h3>
                    <p>This content was loaded via simulated AJAX.</p>
                    <p>Time: ${new Date().toLocaleTimeString()}</p>
                `;
            }, 1500);
        });
        
        // Helper function to determine if a color is light or dark
        function isLightColor(color) {
            const hex = color.replace('#', '');
            const r = parseInt(hex.substr(0, 2), 16);
            const g = parseInt(hex.substr(2, 2), 16);
            const b = parseInt(hex.substr(4, 2), 16);
            
            const brightness = (r * 299 + g * 587 + b * 114) / 1000;
            return brightness > 128;
        }
    </script>
</body>
</html>'''

    writeFile(outputDir, "javascript.html", html)
  }

  /**
   * Generate HTML with forms for testing form extraction.
   */
  void generateFormHtml(Path outputDir) {
    def html = '''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Form Elements Test</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            margin: 0;
            padding: 20px;
            max-width: 800px;
            margin: 0 auto;
        }
        
        .form-container {
            border: 1px solid #ccc;
            padding: 20px;
            margin: 20px 0;
        }
        
        .form-group {
            margin-bottom: 15px;
        }
        
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        
        input, select, textarea {
            width: 100%;
            padding: 8px;
            box-sizing: border-box;
        }
        
        button {
            padding: 10px 15px;
            background: #4CAF50;
            color: white;
            border: none;
            cursor: pointer;
        }
        
        .checkbox-group, .radio-group {
            margin-bottom: 15px;
        }
        
        .checkbox-group label, .radio-group label {
            font-weight: normal;
            display: inline-block;
            margin-right: 15px;
        }
        
        .checkbox-group input, .radio-group input {
            width: auto;
            margin-right: 5px;
        }
    </style>
</head>
<body>
    <h1>Form Elements Test</h1>
    
    <div class="form-container">
        <h2>Contact Form</h2>
        <form id="contact-form" method="post" action="/submit">
            <div class="form-group">
                <label for="name">Name:</label>
                <input type="text" id="name" name="name" placeholder="Enter your name" required>
            </div>
            
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" id="email" name="email" placeholder="Enter your email" required>
            </div>
            
            <div class="form-group">
                <label for="phone">Phone:</label>
                <input type="tel" id="phone" name="phone" placeholder="Enter your phone number">
            </div>
            
            <div class="form-group">
                <label for="subject">Subject:</label>
                <select id="subject" name="subject">
                    <option value="">Choose a subject</option>
                    <option value="general">General Inquiry</option>
                    <option value="support">Technical Support</option>
                    <option value="billing">Billing Question</option>
                    <option value="other">Other</option>
                </select>
            </div>
            
            <div class="form-group">
                <label for="message">Message:</label>
                <textarea id="message" name="message" rows="5" placeholder="Enter your message" required></textarea>
            </div>
            
            <div class="checkbox-group">
                <label>Interests:</label>
                <div>
                    <label>
                        <input type="checkbox" name="interests" value="product-a"> Product A
                    </label>
                    <label>
                        <input type="checkbox" name="interests" value="product-b"> Product B
                    </label>
                    <label>
                        <input type="checkbox" name="interests" value="product-c"> Product C
                    </label>
                </div>
            </div>
            
            <div class="radio-group">
                <label>How did you hear about us?</label>
                <div>
                    <label>
                        <input type="radio" name="source" value="search"> Search Engine
                    </label>
                    <label>
                        <input type="radio" name="source" value="social"> Social Media
                    </label>
                    <label>
                        <input type="radio" name="source" value="referral"> Referral
                    </label>
                    <label>
                        <input type="radio" name="source" value="other"> Other
                    </label>
                </div>
            </div>
            
            <div class="form-group">
                <label for="date">Preferred Contact Date:</label>
                <input type="date" id="date" name="date">
            </div>
            
            <div class="form-group">
                <label for="time">Preferred Contact Time:</label>
                <input type="time" id="time" name="time">
            </div>
            
            <div class="form-group">
                <label>
                    <input type="checkbox" name="terms" required> I agree to the terms and conditions
                </label>
            </div>
            
            <div class="form-group">
                <button type="submit">Submit</button>
                <button type="reset">Reset</button>
            </div>
        </form>
    </div>
    
    <div class="form-container">
        <h2>Login Form</h2>
        <form id="login-form" method="post" action="/login">
            <div class="form-group">
                <label for="username">Username:</label>
                <input type="text" id="username" name="username" placeholder="Enter your username" required>
            </div>
            
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" id="password" name="password" placeholder="Enter your password" required>
            </div>
            
            <div class="form-group">
                <label>
                    <input type="checkbox" name="remember"> Remember me
                </label>
            </div>
            
            <div class="form-group">
                <button type="submit">Login</button>
            </div>
        </form>
    </div>
    
    <div class="form-container">
        <h2>Search Form</h2>
        <form id="search-form" method="get" action="/search">
            <div class="form-group">
                <label for="search">Search:</label>
                <input type="search" id="search" name="q" placeholder="Search...">
            </div>
            
            <div class="form-group">
                <button type="submit">Search</button>
            </div>
        </form>
    </div>
    
    <div class="form-container">
        <h2>File Upload Form</h2>
        <form id="upload-form" method="post" action="/upload" enctype="multipart/form-data">
            <div class="form-group">
                <label for="file">Select File:</label>
                <input type="file" id="file" name="file">
            </div>
            
            <div class="form-group">
                <label for="multiple-files">Select Multiple Files:</label>
                <input type="file" id="multiple-files" name="files" multiple>
            </div>
            
            <div class="form-group">
                <button type="submit">Upload</button>
            </div>
        </form>
    </div>
</body>
</html>'''

    writeFile(outputDir, "forms.html", html)
  }

  /**
   * Generate HTML with tables for testing table extraction.
   */
  void generateTableHtml(Path outputDir) {
    def html = '''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Table Elements Test</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            margin: 0;
            padding: 20px;
            max-width: 1000px;
            margin: 0 auto;
        }
        
        .table-container {
            border: 1px solid #ccc;
            padding: 20px;
            margin: 20px 0;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 15px 0;
        }
        
        caption {
            font-weight: bold;
            margin-bottom: 10px;
        }
        
        table, th, td {
            border: 1px solid #ddd;
        }
        
        th, td {
            padding: 10px;
            text-align: left;
        }
        
        th {
            background-color: #f2f2f2;
        }
        
        tr:nth-child(even) {
            background-color: #f9f9f9;
        }
        
        tfoot {
            font-weight: bold;
        }
        
        .responsive-table {
            overflow-x: auto;
        }
    </style>
</head>
<body>
    <h1>Table Elements Test</h1>
    
    <div class="table-container">
        <h2>Basic Table</h2>
        <table>
            <caption>Sample Data Table</caption>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Department</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>1</td>
                    <td>John Doe</td>
                    <td>john@example.com</td>
                    <td>Marketing</td>
                </tr>
                <tr>
                    <td>2</td>
                    <td>Jane Smith</td>
                    <td>jane@example.com</td>
                    <td>Sales</td>
                </tr>
                <tr>
                    <td>3</td>
                    <td>Bob Johnson</td>
                    <td>bob@example.com</td>
                    <td>Engineering</td>
                </tr>
                <tr>
                    <td>4</td>
                    <td>Alice Brown</td>
                    <td>alice@example.com</td>
                    <td>HR</td>
                </tr>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="4">4 employees</td>
                </tr>
            </tfoot>
        </table>
    </div>
    
    <div class="table-container">
        <h2>Table with Row and Column Spans</h2>
        <table>
            <thead>
                <tr>
                    <th rowspan="2">Category</th>
                    <th colspan="3">Quarterly Sales ($)</th>
                </tr>
                <tr>
                    <th>Q1</th>
                    <th>Q2</th>
                    <th>Q3</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>Software</td>
                    <td>10,000</td>
                    <td>15,000</td>
                    <td>12,500</td>
                </tr>
                <tr>
                    <td>Hardware</td>
                    <td>8,500</td>
                    <td>9,000</td>
                    <td>11,000</td>
                </tr>
                <tr>
                    <td>Services</td>
                    <td>12,000</td>
                    <td>18,000</td>
                    <td>22,000</td>
                </tr>
            </tbody>
            <tfoot>
                <tr>
                    <td>Total</td>
                    <td>30,500</td>
                    <td>42,000</td>
                    <td>45,500</td>
                </tr>
            </tfoot>
        </table>
    </div>
    
    <div class="table-container">
        <h2>Nested Tables</h2>
        <table>
            <thead>
                <tr>
                    <th>Department</th>
                    <th>Employees</th>
                    <th>Budget</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>Marketing</td>
                    <td>
                        <table>
                            <tr>
                                <th>Name</th>
                                <th>Position</th>
                            </tr>
                            <tr>
                                <td>John Doe</td>
                                <td>Director</td>
                            </tr>
                            <tr>
                                <td>Jane Smith</td>
                                <td>Manager</td>
                            </tr>
                        </table>
                    </td>
                    <td>$150,000</td>
                </tr>
                <tr>
                    <td>Engineering</td>
                    <td>
                        <table>
                            <tr>
                                <th>Name</th>
                                <th>Position</th>
                            </tr>
                            <tr>
                                <td>Bob Johnson</td>
                                <td>CTO</td>
                            </tr>
                            <tr>
                                <td>Alice Brown</td>
                                <td>Developer</td>
                            </tr>
                            <tr>
                                <td>Tom Wilson</td>
                                <td>Developer</td>
                            </tr>
                        </table>
                    </td>
                    <td>$300,000</td>
                </tr>
            </tbody>
        </table>
    </div>
    
    <div class="table-container">
        <h2>Complex Table with Multiple Sections</h2>
        <div class="responsive-table">
            <table>
                <caption>Company Financial Report</caption>
                <thead>
                    <tr>
                        <th rowspan="2">Category</th>
                        <th colspan="4">Quarterly Data</th>
                        <th rowspan="2">Annual Total</th>
                    </tr>
                    <tr>
                        <th>Q1</th>
                        <th>Q2</th>
                        <th>Q3</th>
                        <th>Q4</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <th colspan="6">Revenue ($)</th>
                    </tr>
                    <tr>
                        <td>Product A</td>
                        <td>45,000</td>
                        <td>50,000</td>
                        <td>47,500</td>
                        <td>55,000</td>
                        <td>197,500</td>
                    </tr>
                    <tr>
                        <td>Product B</td>
                        <td>32,000</td>
                        <td>38,000</td>
                        <td>42,000</td>
                        <td>48,000</td>
                        <td>160,000</td>
                    </tr>
                    <tr>
                        <td>Services</td>
                        <td>28,000</td>
                        <td>30,000</td>
                        <td>35,000</td>
                        <td>40,000</td>
                        <td>133,000</td>
                    </tr>
                    <tr>
                        <th>Total Revenue</th>
                        <td>105,000</td>
                        <td>118,000</td>
                        <td>124,500</td>
                        <td>143,000</td>
                        <td>490,500</td>
                    </tr>
                    <tr>
                        <th colspan="6">Expenses ($)</th>
                    </tr>
                    <tr>
                        <td>Salaries</td>
                        <td>40,000</td>
                        <td>42,000</td>
                        <td>42,000</td>
                        <td>45,000</td>
                        <td>169,000</td>
                    </tr>
                    <tr>
                        <td>Marketing</td>
                        <td>15,000</td>
                        <td>18,000</td>
                        <td>20,000</td>
                        <td>25,000</td>
                        <td>78,000</td>
                    </tr>
                    <tr>
                        <td>Operations</td>
                        <td>12,000</td>
                        <td>14,000</td>
                        <td>15,500</td>
                        <td>18,000</td>
                        <td>59,500</td>
                    </tr>
                    <tr>
                        <th>Total Expenses</th>
                        <td>67,000</td>
                        <td>74,000</td>
                        <td>77,500</td>
                        <td>88,000</td>
                        <td>306,500</td>
                    </tr>
                </tbody>
                <tfoot>
                    <tr>
                        <th>Net Profit</th>
                        <td>38,000</td>
                        <td>44,000</td>
                        <td>47,000</td>
                        <td>55,000</td>
                        <td>184,000</td>
                    </tr>
                    <tr>
                        <th>Profit Margin</th>
                        <td>36.2%</td>
                        <td>37.3%</td>
                        <td>37.8%</td>
                        <td>38.5%</td>
                        <td>37.5%</td>
                    </tr>
                </tfoot>
            </table>
        </div>
    </div>
</body>
</html>'''

    writeFile(outputDir, "tables.html", html)
  }

  /**
   * Generate HTML with SEO elements for testing SEO analysis.
   */
  void generateSeoTestHtml(Path outputDir) {
    def html = '''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SEO Test Page - Comprehensive Analysis of Search Engine Optimization Elements</title>
    <meta name="description" content="This page contains various SEO elements for testing analysis tools and demonstrates best practices for search engine optimization.">
    <meta name="keywords" content="SEO, test, analysis, search engine optimization, web page analyzer">
    <meta name="author" content="Web Page Analyzer">
    <meta name="robots" content="index, follow">
    
    <!-- Open Graph / Facebook -->
    <meta property="og:type" content="website">
    <meta property="og:url" content="https://example.com/seo-test">
    <meta property="og:title" content="SEO Test Page">
    <meta property="og:description" content="Testing search engine optimization elements">
    <meta property="og:image" content="https://example.com/image.jpg">
    
    <!-- Twitter -->
    <meta property="twitter:card" content="summary_large_image">
    <meta property="twitter:url" content="https://example.com/seo-test">
    <meta property="twitter:title" content="SEO Test Page">
    <meta property="twitter:description" content="Testing search engine optimization elements">
    <meta property="twitter:image" content="https://example.com/image.jpg">
    
    <!-- Canonical URL -->
    <link rel="canonical" href="https://example.com/seo-test">
    
    <!-- Alternate languages -->
    <link rel="alternate" hreflang="es" href="https://example.com/es/seo-test">
    <link rel="alternate" hreflang="fr" href="https://example.com/fr/seo-test">
    
    <!-- Preconnect and resource hints -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preload" href="styles.css" as="style">
    <link rel="stylesheet" href="styles.css">
    
    <!-- Structured data -->
    <script type="application/ld+json">
    {
        "@context": "https://schema.org",
        "@type": "WebPage",
        "name": "SEO Test Page",
        "description": "Testing search engine optimization elements",
        "publisher": {
            "@type": "Organization",
            "name": "Web Page Analyzer",
            "logo": {
                "@type": "ImageObject",
                "url": "https://example.com/logo.png"
            }
        }
    }
    </script>
</head>
<body>
    <header>
        <nav aria-label="Main Navigation">
            <ul>
                <li><a href="/">Home</a></li>
                <li><a href="/about">About</a></li>
                <li><a href="/services">Services</a></li>
                <li><a href="/blog">Blog</a></li>
                <li><a href="/contact">Contact</a></li>
            </ul>
        </nav>
    </header>
    
    <main>
        <article>
            <header>
                <h1>Comprehensive Analysis of Search Engine Optimization Elements</h1>
                <p class="meta">Published on <time datetime="2023-07-15">July 15, 2023</time> by <a href="/author">John Smith</a></p>
            </header>
            
            <section id="introduction">
                <h2>Introduction to SEO</h2>
                <p>Search Engine Optimization (SEO) is the practice of increasing the quantity and quality of traffic to your website through organic search engine results. This test page demonstrates various SEO elements and best practices.</p>
                
                <figure>
                    <img src="seo-diagram.jpg" alt="SEO Process Diagram" width="800" height="400" loading="lazy">
                    <figcaption>Figure 1: The SEO optimization process</figcaption>
                </figure>
            </section>
            
            <section id="on-page-seo">
                <h2>On-Page SEO Elements</h2>
                <p>On-page SEO involves optimizing various parts of your website that affect your search engine rankings. Here are some key elements:</p>
                
                <h3>Content Quality</h3>
                <p>High-quality content that provides value to users is crucial for SEO. Content should be:</p>
                <ul>
                    <li>Informative and relevant to the user's search intent</li>
                    <li>Well-structured with proper headings (H1, H2, H3, etc.)</li>
                    <li>Readable and engaging</li>
                    <li>Unique and original</li>
                </ul>
                
                <h3>Keyword Optimization</h3>
                <p>Keywords should be strategically placed in:</p>
                <ol>
                    <li>Page titles</li>
                    <li>Meta descriptions</li>
                    <li>URL structures</li>
                    <li>Headings and subheadings</li>
                    <li>Opening paragraph</li>
                    <li>Throughout the content (avoid keyword stuffing)</li>
                </ol>
                
                <h3>HTML Structure</h3>
                <p>Proper HTML structure helps search engines understand your content:</p>
                <ul>
                    <li>Use semantic HTML elements (article, section, nav, etc.)</li>
                    <li>Implement proper heading hierarchy</li>
                    <li>Use descriptive alt text for images</li>
                    <li>Create a logical page structure</li>
                </ul>
            </section>
            
            <section id="technical-seo">
                <h2>Technical SEO Elements</h2>
                <p>Technical SEO focuses on improving the technical aspects of a website to increase its rankings in search engines. Key elements include:</p>
                
                <h3>Site Speed</h3>
                <p>A fast-loading website improves user experience and rankings. Techniques include:</p>
                <ul>
                    <li>Image optimization</li>
                    <li>Minification of CSS and JavaScript</li>
                    <li>Browser caching</li>
                    <li>Content Delivery Networks (CDNs)</li>
                </ul>
                
                <h3>Mobile Responsiveness</h3>
                <p>With mobile-first indexing, websites must be fully responsive and mobile-friendly.</p>
                
                <h3>Structured Data</h3>
                <p>Structured data helps search engines understand your content better and can result in rich snippets in search results.</p>
                
                <pre><code>
{
  "@context": "https://schema.org",
  "@type": "Article",
  "headline": "Comprehensive Analysis of SEO Elements",
  "author": {
    "@type": "Person",
    "name": "John Smith"
  },
  "datePublished": "2023-07-15"
}
                </code></pre>
            </section>
            
            <section id="off-page-seo">
                <h2>Off-Page SEO Factors</h2>
                <p>Off-page SEO refers to actions taken outside of your website to impact your rankings. Major factors include:</p>
                
                <h3>Backlink Profile</h3>
                <p>Quality backlinks from reputable websites signal to search engines that your content is valuable. Focus on:</p>
                <ul>
                    <li>Natural link building</li>
                    <li>Guest posting on relevant sites</li>
                    <li>Creating link-worthy content</li>
                </ul>
                
                <table>
                    <caption>Comparison of Link Types</caption>
                    <thead>
                        <tr>
                            <th>Link Type</th>
                            <th>Value</th>
                            <th>Risk</th>
                            <th>Effort Required</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Natural editorial links</td>
                            <td>Very High</td>
                            <td>None</td>
                            <td>High (creating linkable content)</td>
                        </tr>
                        <tr>
                            <td>Guest posting</td>
                            <td>Medium-High</td>
                            <td>Low</td>
                            <td>Medium</td>
                        </tr>
                        <tr>
                            <td>Directory listings</td>
                            <td>Low-Medium</td>
                            <td>Low</td>
                            <td>Low</td>
                        </tr>
                        <tr>
                            <td>Paid links</td>
                            <td>Varies</td>
                            <td>Very High</td>
                            <td>Low</td>
                        </tr>
                    </tbody>
                </table>
            </section>
            
            <section id="seo-tools">
                <h2>SEO Analysis Tools</h2>
                <p>Several tools can help analyze and improve your SEO performance:</p>
                <ul>
                    <li><a href="https://search.google.com/search-console" target="_blank" rel="noopener">Google Search Console</a> - Monitor site performance in Google search results</li>
                    <li><a href="https://www.google.com/analytics" target="_blank" rel="noopener">Google Analytics</a> - Track user behavior and traffic sources</li>
                    <li><a href="https://moz.com" target="_blank" rel="noopener">Moz</a> - Comprehensive SEO toolset</li>
                    <li><a href="https://ahrefs.com" target="_blank" rel="noopener">Ahrefs</a> - Backlink analysis and keyword research</li>
                    <li><a href="https://www.semrush.com" target="_blank" rel="noopener">SEMrush</a> - Competitor analysis and keyword positioning</li>
                </ul>
            </section>
            
            <section id="conclusion">
                <h2>Conclusion</h2>
                <p>SEO is a multifaceted discipline that requires attention to on-page, technical, and off-page factors. By implementing best practices and regularly monitoring performance, websites can improve their visibility in search engine results.</p>
                
                <p>For more information, consult the following resources:</p>
                <ul>
                    <li><a href="https://developers.google.com/search/docs" target="_blank" rel="noopener">Google Search Central Documentation</a></li>
                    <li><a href="https://moz.com/beginners-guide-to-seo" target="_blank" rel="noopener">Moz Beginner's Guide to SEO</a></li>
                </ul>
            </section>
        </article>
        
        <aside>
            <h2>Related Articles</h2>
            <ul>
                <li><a href="/article1">Understanding Meta Tags</a></li>
                <li><a href="/article2">The Importance of Mobile-First Indexing</a></li>
                <li><a href="/article3">How to Conduct Keyword Research</a></li>
                <li><a href="/article4">Link Building Strategies for 2023</a></li>
            </ul>
            
            <h2>Newsletter Signup</h2>
            <form action="/subscribe" method="post">
                <label for="email">Email:</label>
                <input type="email" id="email" name="email" required>
                <button type="submit">Subscribe</button>
            </form>
        </aside>
    </main>
    
    <footer>
        <nav aria-label="Footer Navigation">
            <ul>
                <li><a href="/privacy">Privacy Policy</a></li>
                <li><a href="/terms">Terms of Service</a></li>
                <li><a href="/sitemap">Sitemap</a></li>
            </ul>
        </nav>
        <p>© 2023 Web Page Analyzer. All rights reserved.</p>
    </footer>
</body>
</html>'''

    writeFile(outputDir, "seo-test.html", html)
  }

  /**
   * Helper method to write file content with proper encoding.
   */
  private static void writeFile(Path dir, String filename, String content) {
    Files.write(dir.resolve(filename), content.getBytes(StandardCharsets.UTF_8))
  }
}
