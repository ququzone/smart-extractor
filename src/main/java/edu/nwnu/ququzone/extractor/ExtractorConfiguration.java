package edu.nwnu.ququzone.extractor;

import com.google.common.io.ByteStreams;
import edu.nwnu.ququzone.extractor.result.Result;
import edu.nwnu.ququzone.extractor.service.Extractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * extractor spring configuration.
 *
 * @author Yang XuePing
 */
@SpringBootApplication
@RestController
public class ExtractorConfiguration {
    private static String tpl;

    static {
        try {
            tpl = new String(ByteStreams.toByteArray(ExtractorConfiguration.class.getClassLoader().getResourceAsStream("index.tpl")));
        } catch (IOException e) {
        }
    }

    @Autowired
    @Qualifier("rowBlockExtractor")
    private Extractor extractor;

    public static void main(String[] args) {
        SpringApplication.run(ExtractorConfiguration.class, args);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return tpl;
    }


    @RequestMapping(value = "/extract", method = RequestMethod.GET)
    public Result extract(@RequestParam String url) {
        return extractor.extract(url);
    }
}
