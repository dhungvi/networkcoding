package ch.epfl.lsr.adhoc.runtime;

public class FrancException extends Exception {
	
	public FrancException(String message) {
		super(message);
	}

	public FrancException(String message, Throwable cause) {
		super(message,cause);
	}

	public FrancException(Throwable cause) {
		super(cause);
	}
}
