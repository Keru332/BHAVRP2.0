package cujae.inf.ic.om.exceptions;

public class ProblemException extends AbstractException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProblemException(String message) {
		super(message);
	}

	public ProblemException(String message, Throwable cause) {
		super(message, cause);
	}
}