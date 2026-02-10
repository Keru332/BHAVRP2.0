package cujae.inf.ic.om.exceptions;

public abstract class AbstractException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractException(String message) {
		super(message);
	}
	
	public AbstractException(String message, Throwable cause) {
        super(message, cause);
    }
}