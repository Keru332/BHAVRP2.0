package cujae.inf.ic.om.exceptions;

public class ClusterException extends AbstractException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClusterException(String message) {
		super(message);
	}

	public ClusterException(String message, Throwable cause) {
		super(message, cause);
	}
}