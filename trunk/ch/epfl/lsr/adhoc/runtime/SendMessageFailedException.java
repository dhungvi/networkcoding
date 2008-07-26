package ch.epfl.lsr.adhoc.runtime;

/**
 * This exception is thrown when the programmer tries to send a message, but
 * the sending fails due to a network error (for instance the underlying layer
 * throws an java.io.IOException).
 * <p>
 * This exception extends java.lang.Exception because it can occure anytime and
 * the programmer should preview an error handling mechanism if it happens
 * (the program could just retry, or show an error message to the user).
 * <p>
 * @see AsynchronousLayer
 * @author Urs Hunkeler
 * @version 1.0
 */
public class SendMessageFailedException extends FrancException {

	/**
	 * Creates the exception.
	 * <p>
	 * @param msg  A textual message giving some details for this occurence of the exception.
	 */
	public SendMessageFailedException(String msg) {
		super(msg);
	}

	public SendMessageFailedException(String message, Throwable cause) {
		super(message,cause);
	}

	public SendMessageFailedException(Throwable cause) {
		super(cause);
	}
}
