package com.nnson128.relationshipservice.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/link-preview")
@CrossOrigin(origins = "*")
public class LinkPreviewController {

    @PostMapping
    public ResponseEntity<Map<String, String>> getLinkPreview(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new HashMap<>());
        }

        try {
            // Fetch the webpage with better settings
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
                .followRedirects(true)
                .get();

            Map<String, String> preview = new HashMap<>();

            // Extract title
            String title = doc.select("meta[property=og:title]").attr("content");
            if (title.isEmpty()) {
                title = doc.select("meta[name=title]").attr("content");
            }
            if (title.isEmpty()) {
                title = doc.select("title").text();
            }
            preview.put("title", title);

            // Extract description
            String description = doc.select("meta[property=og:description]").attr("content");
            if (description.isEmpty()) {
                description = doc.select("meta[name=description]").attr("content");
            }
            preview.put("description", description);

            // Extract image - try multiple sources
            String image = extractImage(doc, url);
            if (!image.isEmpty()) {
                preview.put("image", image);
            }

            return ResponseEntity.ok(preview);
        } catch (IOException e) {
            System.err.println("Error fetching link preview: " + e.getMessage());
            // If fetching fails, return empty preview
            return ResponseEntity.ok(new HashMap<>());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.ok(new HashMap<>());
        }
    }

    private String extractImage(Document doc, String baseUrlString) {
        try {
            URL baseUrl = new URL(baseUrlString);

            // Try Open Graph image first (most reliable)
            String image = doc.select("meta[property=og:image]").attr("content");
            if (!image.isEmpty()) {
                return makeAbsoluteUrl(image, baseUrl);
            }

            // Try Twitter image
            image = doc.select("meta[property=twitter:image]").attr("content");
            if (!image.isEmpty()) {
                return makeAbsoluteUrl(image, baseUrl);
            }

            // Try Twitter image:src
            image = doc.select("meta[name=twitter:image:src]").attr("content");
            if (!image.isEmpty()) {
                return makeAbsoluteUrl(image, baseUrl);
            }

            // Try Facebook image
            image = doc.select("meta[property=facebook:image]").attr("content");
            if (!image.isEmpty()) {
                return makeAbsoluteUrl(image, baseUrl);
            }

            // Try any meta image
            image = doc.select("meta[name=image]").attr("content");
            if (!image.isEmpty()) {
                return makeAbsoluteUrl(image, baseUrl);
            }

            // Try first <img> tag that's not tiny (icons are usually small)
            Elements images = doc.select("img[src]");
            for (Element img : images) {
                String src = img.attr("src");
                String alt = img.attr("alt");

                // Skip icons, logos, and favicons
                if (isNotIcon(src, alt)) {
                    String absoluteUrl = makeAbsoluteUrl(src, baseUrl);
                    if (isValidImageUrl(absoluteUrl)) {
                        return absoluteUrl;
                    }
                }
            }

            // Try picture tag
            Elements pictures = doc.select("picture source[srcset]");
            for (Element picture : pictures) {
                String srcset = picture.attr("srcset");
                if (!srcset.isEmpty()) {
                    // Extract first URL from srcset
                    String[] parts = srcset.split(",");
                    if (parts.length > 0) {
                        String firstUrl = parts[0].trim().split("\\s+")[0];
                        String absoluteUrl = makeAbsoluteUrl(firstUrl, baseUrl);
                        if (isValidImageUrl(absoluteUrl)) {
                            return absoluteUrl;
                        }
                    }
                }
            }

            return "";
        } catch (Exception e) {
            System.err.println("Error extracting image: " + e.getMessage());
            return "";
        }
    }

    private String makeAbsoluteUrl(String url, URL baseUrl) {
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return url;
            }

            if (url.startsWith("//")) {
                return baseUrl.getProtocol() + ":" + url;
            }

            if (url.startsWith("/")) {
                return baseUrl.getProtocol() + "://" + baseUrl.getHost() + url;
            }

            // Relative path
            return new URL(baseUrl, url).toString();
        } catch (Exception e) {
            return url;
        }
    }

    private boolean isNotIcon(String src, String alt) {
        String lowerSrc = src.toLowerCase();
        String lowerAlt = alt.toLowerCase();

        // Check if it looks like an icon/logo/favicon
        return !(lowerSrc.contains("icon") ||
            lowerSrc.contains("logo") ||
            lowerSrc.contains("favicon") ||
            lowerSrc.contains("avatar") ||
            lowerAlt.contains("icon") ||
            lowerAlt.contains("logo") ||
            lowerAlt.contains("favicon") ||
            lowerAlt.contains("avatar"));
    }

    private boolean isValidImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        String lower = url.toLowerCase();
        // Check for common image extensions
        return lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".png") ||
            lower.endsWith(".gif") ||
            lower.endsWith(".webp") ||
            lower.endsWith(".svg") ||
            lower.contains(".jpg?") ||
            lower.contains(".jpeg?") ||
            lower.contains(".png?");
    }
}
