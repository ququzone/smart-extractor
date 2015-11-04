package edu.nwnu.ququzone.extractor.result;

/**
 * extractor result.
 *
 * @author Yang XuePing
 */
public class Result {
    private final boolean success;

    public Result(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
