package cujae.inf.ic.om.exceptions;

public class FactoryCreationException extends AbstractException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FactoryCreationException(String message) {
		super(message);
	}

	public FactoryCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}