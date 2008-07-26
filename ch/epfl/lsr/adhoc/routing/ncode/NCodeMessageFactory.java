package ch.epfl.lsr.adhoc.routing.ncode;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This is a simple MessageFactory for test purposes only.
 * <p>
 * This MessageFactory creates instances of the TextMessage class.
 * <p>
 * @author Urs Hunkeler
 */
public class NCodeMessageFactory implements IMessageFactory {
  /** Default constructor */
  public NCodeMessageFactory() {
  }

  /**
   * Creates a new message object (of type TextMessage) with the given type of
   * service.
   * <p>
   * @param type The type of service for the message object
   * @return A message object, with the given type of service (TextMessage)
   */
  public Message createMessage(char type) {
    return new NCodeMessage(type);
  }
}

