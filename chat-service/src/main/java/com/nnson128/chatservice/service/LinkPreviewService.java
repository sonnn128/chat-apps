package com.nnson128.chatservice.service;

import com.nnson128.chatservice.dto.res.LinkPreviewDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
@Slf4j
public class LinkPreviewService {

    public LinkPreviewDto getLinkPreview(String url) {
        try {
            // Validate and normalize URL
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }

            // Extract domain
            String domain = getDomainName(url);

            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(5000)
                .get();

            String title = getMetaTagContent(doc, "og:title");
            if (title == null || title.isEmpty()) {
                title = doc.title();
            }

            String description = getMetaTagContent(doc, "og:description");
            if (description == null || description.isEmpty()) {
                description = getMetaTagContent(doc, "description");
            }

            String image = getMetaTagContent(doc, "og:image");

            String siteName = getMetaTagContent(doc, "og:site_name");

            return LinkPreviewDto.builder()
                .url(url)
                .title(title)
                .description(description)
                .image(image)
                .domain(domain)
                .siteName(siteName)
                .build();

        } catch (IOException e) {
            log.error("Error fetching link preview for url: {}", url, e);
            return LinkPreviewDto.builder().url(url).build();
        }
    }

    private String getMetaTagContent(Document doc, String property) {
        Element element = doc.selectFirst("meta[property=" + property + "]");
        if (element == null) {
            element = doc.selectFirst("meta[name=" + property + "]");
        }
        return element != null ? element.attr("content") : null;
    }

    private String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain != null ? domain.startsWith("www.") ? domain.substring(4) : domain : url;
        } catch (URISyntaxException e) {
            return url;
        }
    }
}
