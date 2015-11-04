package edu.nwnu.ququzone.extractor.result;

/**
 * failure result
 *
 * @author Yang XuePing
 */
public class FailureResult extends Result {
    private final String msg;

    public FailureResult(String msg) {
        super(false);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
