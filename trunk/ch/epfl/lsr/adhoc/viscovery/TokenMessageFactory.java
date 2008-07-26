package ch.epfl.lsr.adhoc.viscovery;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This is the MessageFactory for TokenMessages.
 * <p>
 * This MessageFactory creates instances of the TokenMessage class.
 * <p>
 * @author Stefan Thurnherr
 * @version 1.0
 */
public class TokenMessageFactory implements IMessageFactory {
  /** Default constructor */
  public TokenMessageFactory() {
  }

  /**
   * Creates a new message object (of type TokenMessage) with the given type of
   * service.
   * <p>
   * @param type The type of service for the message object
   * @return A message object, with the given type of service (TokenMessage)
   */
  public Message createMessage(char type) {
    return new TokenMessage(type);
  }
}
