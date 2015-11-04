package edu.nwnu.ququzone.extractor.result;

/**
 * successful result.
 *
 * @author Yang XuePing
 */
public class SuccessResult extends Result {
    private final String html;
    private final String text;

    public SuccessResult(String html, String text) {
        super(true);
        this.html = html;
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public String getText() {
        return text;
    }
}
