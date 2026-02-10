package cujae.inf.ic.om.exceptions;


public class MetricException extends AbstractException {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MetricException(String message) {
        super(message);
    }

    public MetricException(String message, Throwable cause) {
        super(message, cause);
    }
}