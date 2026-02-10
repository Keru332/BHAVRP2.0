package cujae.inf.ic.om.exceptions;

public class ExportException extends AbstractException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}