package edu.nwnu.ququzone.extractor.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.Test;

import java.net.URL;

/**
 * test readability extractor.
 *
 * @author Yang XuePing
 */
public class ReadabilityExtractorTest {
    @Test
    public void testCrawl() throws Exception {
        Document doc = Jsoup.parse(ReadabilityExtractorTest.class.getClassLoader().getResourceAsStream("test.html"), "UTF-8", "/");
        // Document doc = Jsoup.parse(new URL("http://192.168.1.102:3000"), 2000);

        ReadabilityExtractor extractor = new ReadabilityExtractor();
        extractor.parse(doc);
    }
}
