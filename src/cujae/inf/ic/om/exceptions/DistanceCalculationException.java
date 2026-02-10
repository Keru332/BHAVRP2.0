package cujae.inf.ic.om.exceptions;


public class DistanceCalculationException extends AbstractException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DistanceCalculationException(String message) {
        super(message);
    }

    public DistanceCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}