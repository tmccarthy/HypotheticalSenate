package au.id.tmm.hypotheticalsenate.view.controller;

/**
 * @author timothy
 */
public class MissingParameterException extends Exception {

    public MissingParameterException() {
    }

    public MissingParameterException(String message) {
        super(message);
    }

    public MissingParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
