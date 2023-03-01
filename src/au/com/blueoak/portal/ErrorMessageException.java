package au.com.blueoak.portal;

public class ErrorMessageException extends RuntimeException {
	
    private static final long serialVersionUID = -3598688579566820736L;

    public ErrorMessageException() {
    
        super();
    }

    public ErrorMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ErrorMessageException(String message, Throwable cause) {
    
        super(message, cause);
    }

    public ErrorMessageException(String message) {
    
        super(message);
    }

    public ErrorMessageException(Throwable cause) {
    
        super(cause);
    }

}
