package ch.epfl.lsr.adhoc.runtime;

/**
 * This exception is thrown a message is received, but its type is not known.
 * <p>
 * This exception extends java.lang.Exception because it can happen any time that
 * there is a node on the local network that uses an application specific message
 * which is unknown to this implementation.
 * <p>
 * @see MessagePool
 * @author Urs Hunkeler
 * @version 1.0
 */
public class UnknownMessageTypeException extends FrancException {
	/** The unknown message type */
	private char type;


	public UnknownMessageTypeException(char type) {
		this("Unknown MessageType : " + ((int)type),type);
	}

	/**
	 * Creates the exception.
	 * <p>
	 * @param msg  A textual message giving some details for this occurence of the exception.
	 * @param type The unknown type that caused the exception
	 */
	public UnknownMessageTypeException(String msg, char type) {
		super(msg);
		this.type = type;
	}

	/**
	 * Returns the type of the message that is unknown.
	 * <p>
	 * @param type The unknown type that caused the exception
	 * @return the unknown type
	 */
	public char getUnknownType() {
		return type;
	}
}
