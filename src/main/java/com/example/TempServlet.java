package com.example;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/convert")
public class TempServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(true);
        PrintWriter out = response.getWriter();
        
        try {
            String celsiusStr = request.getParameter("celsius");
            double celsius = Double.parseDouble(celsiusStr);
            
            // Perform conversions
            double fahrenheit = (celsius * 9/5) + 32;
            double kelvin = celsius + 273.15;
            double rankine = kelvin * 9/5;
            
            // Store in session
            List<Conversion> history = getHistory(session);
            history.add(new Conversion(celsius, fahrenheit, LocalDateTime.now()));
            if (history.size() > 5) history.remove(0);
            session.setAttribute("tempHistory", history);
            
            // Generate response PROGRAMMATICALLY
            generateStyledResponse(out, celsius, fahrenheit, kelvin, rankine, history);
            
        } catch (NumberFormatException e) {
            generateErrorResponse(out, "Invalid temperature value!");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        if ("clear".equals(request.getParameter("action"))) {
            request.getSession().removeAttribute("tempHistory");
            out.println("<script>window.location.href='index.html';</script>");
            return;
        }
        
        HttpSession session = request.getSession(false);
        List<Conversion> history = session != null ? 
            (List<Conversion>) session.getAttribute("tempHistory") : new ArrayList<>();
        
        generateDashboard(out, history);
    }
    
    // ========== STYLED RESPONSE GENERATORS ==========
    
    private void generateStyledResponse(PrintWriter out, double celsius, double fahrenheit, 
                                       double kelvin, double rankine, List<Conversion> history) {
        
        // Start HTML
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        generateHead(out, "🌡️ Conversion Result");
        
        out.println("<body>");
        
        // Container
        out.println("<div class='container'>");
        
        // Header
        out.println("<div class='header'>");
        out.println("<h1><i class='fas fa-check-circle success'></i> Conversion Successful!</h1>");
        out.println("<p class='subtitle'>Celsius to Fahrenheit Converter</p>");
        out.println("</div>");
        
        // Main Card
        out.println("<div class='main-card'>");
        
        // Temperature Display
        out.println("<div class='temp-display'>");
        out.println("<div class='temp-box celsius-box'>");
        out.println("<div class='temp-label'>INPUT</div>");
        out.println("<div class='temp-value'>" + String.format("%.2f", celsius) + "°C</div>");
        out.println("<div class='temp-status'>" + getTempStatus(celsius) + "</div>");
        out.println("</div>");
        
        out.println("<div class='arrow'>→</div>");
        
        out.println("<div class='temp-box fahrenheit-box'>");
        out.println("<div class='temp-label'>RESULT</div>");
        out.println("<div class='temp-value'>" + String.format("%.2f", fahrenheit) + "°F</div>");
        out.println("<div class='temp-status'>" + getFahrenheitStatus(fahrenheit) + "</div>");
        out.println("</div>");
        out.println("</div>");
        
        // Conversion Formula
        out.println("<div class='formula-box'>");
        out.println("<h3><i class='fas fa-calculator'></i> Conversion Formula</h3>");
        out.println("<div class='formula'>");
        out.println("℉ = (℃ × 9/5) + 32<br>");
        out.println("= (" + celsius + " × 1.8) + 32<br>");
        out.println("= <strong>" + String.format("%.2f", fahrenheit) + "°F</strong>");
        out.println("</div>");
        out.println("</div>");
        
        // Other Units
        out.println("<div class='units-grid'>");
        out.println("<div class='unit-card'>");
        out.println("<div class='unit-label'>Kelvin</div>");
        out.println("<div class='unit-value'>" + String.format("%.2f", kelvin) + " K</div>");
        out.println("</div>");
        
        out.println("<div class='unit-card'>");
        out.println("<div class='unit-label'>Rankine</div>");
        out.println("<div class='unit-value'>" + String.format("%.2f", rankine) + " °R</div>");
        out.println("</div>");
        
        out.println("<div class='unit-card'>");
        out.println("<div class='unit-label'>Difference</div>");
        out.println("<div class='unit-value'>" + String.format("%.2f", (fahrenheit - 32)) + " F°</div>");
        out.println("</div>");
        
        out.println("<div class='unit-card'>");
        out.println("<div class='unit-label'>Ratio</div>");
        out.println("<div class='unit-value'>1.8 (9/5)</div>");
        out.println("</div>");
        out.println("</div>");
        
        // History
        if (!history.isEmpty()) {
            out.println("<div class='history-section'>");
            out.println("<h3><i class='fas fa-history'></i> Recent Conversions</h3>");
            out.println("<div class='history-list'>");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            Collections.reverse(history);
            
            for (Conversion conv : history) {
                out.println("<div class='history-item'>");
                out.println("<div class='history-temp'>");
                out.println("<span class='hist-celsius'>" + String.format("%.1f", conv.celsius) + "°C</span>");
                out.println("<span class='hist-arrow'>→</span>");
                out.println("<span class='hist-fahrenheit'>" + String.format("%.1f", conv.fahrenheit) + "°F</span>");
                out.println("</div>");
                out.println("<div class='history-time'>" + conv.timestamp.format(formatter) + "</div>");
                out.println("</div>");
            }
            
            out.println("</div>");
            out.println("<a href='convert?action=clear' class='clear-btn'>Clear History</a>");
            out.println("</div>");
        }
        
        // Action Buttons
        out.println("<div class='action-buttons'>");
        out.println("<a href='index.html' class='btn primary-btn'><i class='fas fa-redo'></i> Convert Another</a>");
        out.println("<a href='index.html' class='btn secondary-btn'><i class='fas fa-home'></i> Back to Home</a>");
        out.println("</div>");
        
        out.println("</div>"); // Close main-card
        
        // Footer
        out.println("<div class='footer'>");
        out.println("<p>🌡️ Temperature Converter | Java Servlet | Real-time Conversion</p>");
        out.println("</div>");
        
        out.println("</div>"); // Close container
        out.println("</body>");
        out.println("</html>");
    }
    
    private void generateDashboard(PrintWriter out, List<Conversion> history) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        generateHead(out, "📊 Temperature Dashboard");
        
        out.println("<body>");
        out.println("<div class='container'>");
        
        out.println("<div class='header'>");
        out.println("<h1><i class='fas fa-chart-line'></i> Conversion Dashboard</h1>");
        out.println("<p>View your temperature conversion history</p>");
        out.println("</div>");
        
        if (history.isEmpty()) {
            out.println("<div class='empty-state'>");
            out.println("<i class='fas fa-thermometer-empty'></i>");
            out.println("<h3>No conversions yet</h3>");
            out.println("<p>Convert some temperatures to see them here!</p>");
            out.println("<a href='index.html' class='btn primary-btn'>Start Converting</a>");
            out.println("</div>");
        } else {
            out.println("<div class='stats-grid'>");
            
            // Statistics
            double avgCelsius = history.stream().mapToDouble(c -> c.celsius).average().orElse(0);
            double avgFahrenheit = history.stream().mapToDouble(c -> c.fahrenheit).average().orElse(0);
            double maxCelsius = history.stream().mapToDouble(c -> c.celsius).max().orElse(0);
            double minCelsius = history.stream().mapToDouble(c -> c.celsius).min().orElse(0);
            
            out.println("<div class='stat-card'>");
            out.println("<div class='stat-label'>Total Conversions</div>");
            out.println("<div class='stat-value'>" + history.size() + "</div>");
            out.println("</div>");
            
            out.println("<div class='stat-card'>");
            out.println("<div class='stat-label'>Average Celsius</div>");
            out.println("<div class='stat-value'>" + String.format("%.1f", avgCelsius) + "°C</div>");
            out.println("</div>");
            
            out.println("<div class='stat-card'>");
            out.println("<div class='stat-label'>Average Fahrenheit</div>");
            out.println("<div class='stat-value'>" + String.format("%.1f", avgFahrenheit) + "°F</div>");
            out.println("</div>");
            
            out.println("<div class='stat-card'>");
            out.println("<div class='stat-label'>Range</div>");
            out.println("<div class='stat-value'>" + String.format("%.1f", minCelsius) + "°C to " + 
                       String.format("%.1f", maxCelsius) + "°C</div>");
            out.println("</div>");
            out.println("</div>");
            
            // History Table
            out.println("<div class='history-table'>");
            out.println("<h3><i class='fas fa-list'></i> Conversion History</h3>");
            out.println("<table>");
            out.println("<thead><tr><th>Time</th><th>Celsius</th><th>Fahrenheit</th><th>Status</th></tr></thead>");
            out.println("<tbody>");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            Collections.reverse(history);
            
            for (Conversion conv : history) {
                out.println("<tr>");
                out.println("<td>" + conv.timestamp.format(formatter) + "</td>");
                out.println("<td>" + String.format("%.2f", conv.celsius) + "°C</td>");
                out.println("<td>" + String.format("%.2f", conv.fahrenheit) + "°F</td>");
                out.println("<td><span class='status-badge " + getStatusClass(conv.celsius) + "'>" + 
                           getTempStatus(conv.celsius) + "</span></td>");
                out.println("</tr>");
            }
            
            out.println("</tbody>");
            out.println("</table>");
            out.println("<a href='convert?action=clear' class='clear-btn'><i class='fas fa-trash'></i> Clear All History</a>");
            out.println("</div>");
        }
        
        out.println("<div class='footer'>");
        out.println("<a href='index.html' class='back-link'><i class='fas fa-arrow-left'></i> Back to Converter</a>");
        out.println("</div>");
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    private void generateErrorResponse(PrintWriter out, String message) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        generateHead(out, "❌ Conversion Error");
        
        out.println("<body>");
        out.println("<div class='container'>");
        
        out.println("<div class='error-card'>");
        out.println("<div class='error-icon'><i class='fas fa-exclamation-triangle'></i></div>");
        out.println("<h2>Conversion Failed</h2>");
        out.println("<p class='error-message'>" + message + "</p>");
        out.println("<p>Please enter a valid number (e.g., 25, -10, 37.5)</p>");
        out.println("<a href='index.html' class='btn primary-btn'><i class='fas fa-arrow-left'></i> Try Again</a>");
        out.println("</div>");
        
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
    // ========== HELPER METHODS ==========
    
    private void generateHead(PrintWriter out, String title) {
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + title + "</title>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css'>");
        out.println("<style>");
        generateCSS(out);
        out.println("</style>");
        out.println("</head>");
    }
    
    private void generateCSS(PrintWriter out) {
        out.println("""
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { 
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                padding: 20px;
                color: #333;
            }
            .container {
                max-width: 1200px;
                margin: 0 auto;
            }
            .header {
                text-align: center;
                margin-bottom: 30px;
                padding: 30px;
                background: rgba(255, 255, 255, 0.9);
                border-radius: 20px;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
            }
            .header h1 {
                font-size: 2.5rem;
                margin-bottom: 10px;
                color: #2d3748;
            }
            .header .subtitle {
                color: #718096;
                font-size: 1.1rem;
            }
            .success {
                color: #48bb78;
            }
            .main-card {
                background: white;
                border-radius: 20px;
                padding: 40px;
                box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
                margin-bottom: 30px;
            }
            .temp-display {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 40px;
                margin: 30px 0;
                flex-wrap: wrap;
            }
            .temp-box {
                padding: 30px;
                border-radius: 15px;
                text-align: center;
                min-width: 250px;
                flex: 1;
            }
            .celsius-box {
                background: linear-gradient(135deg, #4299e1, #63b3ed);
                color: white;
            }
            .fahrenheit-box {
                background: linear-gradient(135deg, #ed8936, #f6ad55);
                color: white;
            }
            .temp-label {
                font-size: 1.2rem;
                margin-bottom: 10px;
                opacity: 0.9;
            }
            .temp-value {
                font-size: 3.5rem;
                font-weight: bold;
                margin: 15px 0;
                font-family: 'Courier New', monospace;
            }
            .temp-status {
                font-size: 1rem;
                opacity: 0.8;
            }
            .arrow {
                font-size: 3rem;
                color: white;
                font-weight: bold;
            }
            .formula-box {
                background: #f7fafc;
                border-radius: 15px;
                padding: 25px;
                margin: 30px 0;
                border-left: 4px solid #4299e1;
            }
            .formula-box h3 {
                color: #2d3748;
                margin-bottom: 15px;
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .formula {
                background: white;
                padding: 20px;
                border-radius: 10px;
                font-family: 'Courier New', monospace;
                font-size: 1.2rem;
                color: #2d3748;
                line-height: 1.6;
            }
            .units-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                gap: 20px;
                margin: 30px 0;
            }
            .unit-card {
                background: white;
                border: 2px solid #e2e8f0;
                border-radius: 10px;
                padding: 20px;
                text-align: center;
                transition: all 0.3s;
            }
            .unit-card:hover {
                transform: translateY(-5px);
                border-color: #4299e1;
                box-shadow: 0 10px 20px rgba(66, 153, 225, 0.1);
            }
            .unit-label {
                color: #718096;
                font-size: 0.9rem;
                text-transform: uppercase;
                letter-spacing: 1px;
                margin-bottom: 10px;
            }
            .unit-value {
                font-size: 1.8rem;
                font-weight: bold;
                color: #2d3748;
                font-family: 'Courier New', monospace;
            }
            .history-section {
                margin: 40px 0;
                padding: 25px;
                background: #f7fafc;
                border-radius: 15px;
            }
            .history-section h3 {
                color: #2d3748;
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .history-list {
                max-height: 300px;
                overflow-y: auto;
            }
            .history-item {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 15px;
                background: white;
                border-radius: 10px;
                margin-bottom: 10px;
                border-left: 4px solid #48bb78;
            }
            .history-temp {
                display: flex;
                align-items: center;
                gap: 15px;
            }
            .hist-celsius {
                color: #4299e1;
                font-weight: bold;
                font-size: 1.2rem;
            }
            .hist-fahrenheit {
                color: #ed8936;
                font-weight: bold;
                font-size: 1.2rem;
            }
            .hist-arrow {
                color: #a0aec0;
            }
            .history-time {
                color: #718096;
                font-size: 0.9rem;
            }
            .action-buttons {
                display: flex;
                gap: 20px;
                justify-content: center;
                margin-top: 30px;
            }
            .btn {
                padding: 15px 40px;
                border-radius: 50px;
                text-decoration: none;
                font-weight: 600;
                font-size: 1.1rem;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                gap: 10px;
                transition: all 0.3s;
                cursor: pointer;
                border: none;
            }
            .primary-btn {
                background: linear-gradient(135deg, #667eea, #764ba2);
                color: white;
            }
            .secondary-btn {
                background: #e2e8f0;
                color: #2d3748;
            }
            .btn:hover {
                transform: translateY(-3px);
                box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
            }
            .clear-btn {
                display: inline-block;
                margin-top: 15px;
                padding: 10px 25px;
                background: #fed7d7;
                color: #c53030;
                border-radius: 50px;
                text-decoration: none;
                font-weight: 600;
                transition: all 0.3s;
            }
            .clear-btn:hover {
                background: #feb2b2;
            }
            .footer {
                text-align: center;
                padding: 20px;
                color: white;
                font-size: 0.9rem;
            }
            /* Dashboard Styles */
            .empty-state {
                text-align: center;
                padding: 60px;
                background: rgba(255, 255, 255, 0.9);
                border-radius: 20px;
                margin: 30px 0;
            }
            .empty-state i {
                font-size: 4rem;
                color: #a0aec0;
                margin-bottom: 20px;
            }
            .stats-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                gap: 20px;
                margin: 30px 0;
            }
            .stat-card {
                background: white;
                padding: 30px;
                border-radius: 15px;
                text-align: center;
                box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
            }
            .stat-label {
                color: #718096;
                font-size: 0.9rem;
                text-transform: uppercase;
                letter-spacing: 1px;
                margin-bottom: 10px;
            }
            .stat-value {
                font-size: 2.5rem;
                font-weight: bold;
                color: #2d3748;
            }
            .history-table {
                background: white;
                border-radius: 15px;
                padding: 30px;
                margin: 30px 0;
                box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 20px;
            }
            th {
                background: #f7fafc;
                padding: 15px;
                text-align: left;
                color: #4a5568;
                font-weight: 600;
                border-bottom: 2px solid #e2e8f0;
            }
            td {
                padding: 15px;
                border-bottom: 1px solid #e2e8f0;
            }
            tr:hover {
                background: #f7fafc;
            }
            .status-badge {
                padding: 5px 15px;
                border-radius: 20px;
                font-size: 0.8rem;
                font-weight: 600;
            }
            .status-freezing {
                background: #bee3f8;
                color: #2b6cb0;
            }
            .status-cold {
                background: #c6f6d5;
                color: #276749;
            }
            .status-warm {
                background: #fed7d7;
                color: #c53030;
            }
            .status-hot {
                background: #fed7d7;
                color: #c53030;
            }
            .error-card {
                background: white;
                border-radius: 20px;
                padding: 50px;
                text-align: center;
                max-width: 500px;
                margin: 100px auto;
                box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
            }
            .error-icon {
                font-size: 4rem;
                color: #fc8181;
                margin-bottom: 20px;
            }
            .error-message {
                color: #c53030;
                font-size: 1.2rem;
                margin: 20px 0;
                padding: 15px;
                background: #fed7d7;
                border-radius: 10px;
            }
            .back-link {
                color: white;
                text-decoration: none;
                display: inline-flex;
                align-items: center;
                gap: 10px;
                margin-top: 20px;
            }
            @media (max-width: 768px) {
                .temp-display {
                    flex-direction: column;
                }
                .arrow {
                    transform: rotate(90deg);
                    margin: 20px 0;
                }
                .action-buttons {
                    flex-direction: column;
                }
                .btn {
                    width: 100%;
                }
            }
            """);
    }
    
    // ========== UTILITY METHODS ==========
    
    private List<Conversion> getHistory(HttpSession session) {
        List<Conversion> history = (List<Conversion>) session.getAttribute("tempHistory");
        return history != null ? history : new ArrayList<>();
    }
    
    private String getTempStatus(double celsius) {
        if (celsius <= 0) return "❄️ Freezing";
        if (celsius <= 10) return "🥶 Cold";
        if (celsius <= 20) return "😊 Cool";
        if (celsius <= 30) return "😌 Warm";
        if (celsius <= 40) return "🥵 Hot";
        return "🔥 Very Hot";
    }
    
    private String getFahrenheitStatus(double fahrenheit) {
        if (fahrenheit <= 32) return "Freezing Point";
        if (fahrenheit <= 50) return "Cold";
        if (fahrenheit <= 68) return "Cool";
        if (fahrenheit <= 86) return "Warm";
        if (fahrenheit <= 104) return "Hot";
        return "Very Hot";
    }
    
    private String getStatusClass(double celsius) {
        if (celsius <= 0) return "status-freezing";
        if (celsius <= 15) return "status-cold";
        if (celsius <= 30) return "status-warm";
        return "status-hot";
    }
    
    // Inner class for conversion history
    class Conversion {
        double celsius;
        double fahrenheit;
        LocalDateTime timestamp;
        
        Conversion(double celsius, double fahrenheit, LocalDateTime timestamp) {
            this.celsius = celsius;
            this.fahrenheit = fahrenheit;
            this.timestamp = timestamp;
        }
    }
}