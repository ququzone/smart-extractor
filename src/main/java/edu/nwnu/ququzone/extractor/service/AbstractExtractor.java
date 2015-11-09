package edu.nwnu.ququzone.extractor.service;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import edu.nwnu.ququzone.extractor.result.FailureResult;
import edu.nwnu.ququzone.extractor.result.Result;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * abstract extractor.
 *
 * @author Yang XuePing
 */
public abstract class AbstractExtractor implements Extractor {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractExtractor.class);

    protected OkHttpClient client;

    protected AbstractExtractor() {
        this.client = new OkHttpClient();
        this.client.setConnectTimeout(20, TimeUnit.SECONDS);
        this.client.setReadTimeout(20, TimeUnit.SECONDS);
    }

    @Override
    public Result extract(String url) {
        try {
            Document doc = getDocument(url);
            if (doc == null) {
                return new FailureResult(String.format("fetch %s document error.", url));
            }
            return parse(doc);
        } catch (ParseException e) {
            LOG.error(String.format("parse %s document exception.", url), e);
            return new FailureResult(e.getMessage());
        } catch (Exception e) {
            LOG.error(String.format("fetch %s document exception.", url), e);
            return new FailureResult(String.format("fetch %s document exception.", url));
        }
    }

    protected abstract Result parse(Document doc);

    protected Document getDocument(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                byte[] data = response.body().bytes();
                String encoding = detectEncoding(data);
                return Jsoup.parse(new String(data, encoding), url);
            } else {
                throw new RuntimeException(String.format("get %s document error", url));
            }
        } catch (IOException e) {
            LOG.error("fetch document error:" + url, e);
        }
        return null;
    }

    protected String detectEncoding(byte[] data) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(data, 0, data.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        if (encoding == null) {
            encoding = "UTF-8";
        }
        return encoding;
    }
}
