package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WebScrapingService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT = 30000; // 30 seconds

    /**
     * Scrape vehicle listing data from a URL
     */
    public Map<String, String> scrapeVehicleListing(String url) {
        Map<String, String> scrapedData = new HashMap<>();

        try {
            Document doc = fetchDocument(url);

            // Extract data based on common HTML patterns
            scrapedData.put("make", extractMake(doc));
            scrapedData.put("model", extractModel(doc));
            scrapedData.put("year", extractYear(doc));
            scrapedData.put("mileage", extractMileage(doc));
            scrapedData.put("price", extractPrice(doc));
            scrapedData.put("description", extractDescription(doc));

            log.info("Successfully scraped listing from: " + url);

        } catch (IOException e) {
            log.error("Failed to scrape URL: " + url, e);
            scrapedData.put("error", "Failed to scrape URL: " + e.getMessage());
        }

        return scrapedData;
    }

    /**
     * Fetch HTML document from URL with error handling
     */
    private Document fetchDocument(String url) throws IOException {
        Connection connection = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .followRedirects(true);

        return connection.get();
    }

    /**
     * Extract vehicle make from common HTML selectors
     */
    private String extractMake(Document doc) {
        // Try common CSS selectors
        String[] selectors = {
                ".make", "[data-make]", ".vehicle-make", ".vehicle_make",
                "span.make", "div.make", "[id*='make']"
        };

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null && !element.text().isEmpty()) {
                return element.text().trim();
            }
        }

        // Fallback: extract from text patterns
        String text = doc.text();
        // This is a simplified approach
        return null;
    }

    /**
     * Extract vehicle model from common HTML selectors
     */
    private String extractModel(Document doc) {
        String[] selectors = {
                ".model", "[data-model]", ".vehicle-model", ".vehicle_model",
                "span.model", "div.model", "[id*='model']"
        };

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null && !element.text().isEmpty()) {
                return element.text().trim();
            }
        }

        return null;
    }

    /**
     * Extract vehicle year from common HTML selectors
     */
    private String extractYear(Document doc) {
        String[] selectors = {
                ".year", "[data-year]", ".vehicle-year", ".vehicle_year",
                "span.year", "div.year", "[id*='year']"
        };

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null && !element.text().isEmpty()) {
                String year = element.text().trim();
                // Extract 4-digit year
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{4}");
                java.util.regex.Matcher matcher = pattern.matcher(year);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }

        return null;
    }

    /**
     * Extract vehicle mileage from common HTML selectors
     */
    private String extractMileage(Document doc) {
        String[] selectors = {
                ".mileage", "[data-mileage]", ".vehicle-mileage", ".km",
                "span.mileage", "div.mileage", "[id*='mileage']"
        };

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null && !element.text().isEmpty()) {
                String mileageText = element.text().trim();
                // Extract numbers only
                return mileageText.replaceAll("[^0-9]", "");
            }
        }

        return null;
    }

    /**
     * Extract listing price from common HTML selectors
     */
    private String extractPrice(Document doc) {
        String[] selectors = {
                ".price", "[data-price]", ".vehicle-price", ".auction-price",
                "span.price", "div.price", "[id*='price']"
        };

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null && !element.text().isEmpty()) {
                return element.text().trim();
            }
        }

        return null;
    }

    /**
     * Extract vehicle description from common HTML selectors
     */
    private String extractDescription(Document doc) {
        String[] selectors = {
                ".description", "[data-description]", ".vehicle-description",
                "span.description", "div.description", ".details", "p"
        };

        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element != null && !element.text().isEmpty()) {
                return element.text().trim();
            }
        }

        return null;
    }

    /**
     * Detect auction source from URL
     */
    public String detectAuctionSource(String url) {
        if (url.contains("cars.co.za")) {
            return "Cars.co.za";
        } else if (url.contains("copart")) {
            return "Copart";
        } else if (url.contains("smd")) {
            return "SMD";
        } else if (url.contains("gumtree")) {
            return "Gumtree";
        } else if (url.contains("facebook")) {
            return "Facebook Marketplace";
        }

        return "Unknown";
    }
}
