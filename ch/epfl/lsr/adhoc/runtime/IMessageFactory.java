package ch.epfl.lsr.adhoc.runtime;

/**
 * This interface represents a MessageFactory for creating message objects in
 * the communications layer.
 * <p>
 * @author Javier Bonny
 * @version 1.0
 */
public interface IMessageFactory {

  /**
   * This method allows to create a new message object with the given type of
   * service.
   * <p>
   * @param type The type of service for the message object
   * @return A message object, with the given type of service
   */
  public Message createMessage(char type);
}

