package cujae.inf.ic.om.exceptions;

public class AssignmentException extends AbstractException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AssignmentException(String message) {
        super(message);
    }

    public AssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}