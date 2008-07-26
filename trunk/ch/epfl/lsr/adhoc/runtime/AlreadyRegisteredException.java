package ch.epfl.lsr.adhoc.runtime;

/**
 * This exception is thrown when the user tries to register a service that is already
 * registered in the Dispatcher.
 * <p>
 * This exception extends java.lang.RuntimeException because it should not normaly occure,
 * and there is little sens that this exception is handeled.
 *
 * @see Dispatcher
 *
 * @author Javier Bonny
 * @version 1.0
 */
public class AlreadyRegisteredException extends RuntimeException {

	private char msgType;

  /**
   * Creates the exception.
   *
   * @param msg  A textual message giving some details for this occurence of the exception.
   */

	public AlreadyRegisteredException(char msgType) {
		this("A service with the same  message type \""+ 
			 ((int)msgType)+"\" has already been registered.",msgType);
	}

	public AlreadyRegisteredException(String msg, char msgType) {
		super(msg);
		this.msgType = msgType;
	}

	public char getMessageType() {
		return msgType;
	}
}
