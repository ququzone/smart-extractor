package edu.nwnu.ququzone.extractor.result;

/**
 * successful result.
 *
 * @author Yang XuePing
 */
public class SuccessResult extends Result {
    private final String title;
    private final String html;

    public SuccessResult(String title, String html) {
        super(true);
        this.title = title;
        this.html = html;
    }

    public String getTitle() {
        return title;
    }

    public String getHtml() {
        return html;
    }
}
