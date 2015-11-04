package edu.nwnu.ququzone.extractor.service;

import edu.nwnu.ququzone.extractor.result.Result;

/**
 * extract page main content.
 *
 * @author Yang XuePing
 */
public interface Extractor {
    Result extract(String url);
}
