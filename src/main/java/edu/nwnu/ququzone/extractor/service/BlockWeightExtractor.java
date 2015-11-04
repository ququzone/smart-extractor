package edu.nwnu.ququzone.extractor.service;

import edu.nwnu.ququzone.extractor.result.Result;
import edu.nwnu.ququzone.extractor.result.SuccessResult;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * block weight extractor.
 *
 * @author Yang XuePing
 */
@Component("blockWeightExtractor")
public class BlockWeightExtractor extends AbstractExtractor {
    @Override
    protected Result parse(Document doc) {
        return new SuccessResult("hello", doc.text());
    }
}
