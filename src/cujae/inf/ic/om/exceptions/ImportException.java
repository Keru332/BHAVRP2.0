package cujae.inf.ic.om.exceptions;

public class ImportException extends AbstractException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}