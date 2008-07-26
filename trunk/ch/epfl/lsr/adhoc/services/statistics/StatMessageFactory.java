package ch.epfl.lsr.adhoc.services.statistics;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This is the factory of the message with type StatMessage.
 *
 * @see StatMessage
 * @see IMessageFactory
 *
 * @author Reto Krummenacher
 */
public class StatMessageFactory implements IMessageFactory {

  public StatMessageFactory() {
  }

  //METHODS
    /**
     * This method creates a new message object (of type = StatMessage).
     *
     * @param type The type of the message
     * @return A message object of type StatMessage
     */
    public Message createMessage(char type) {
        return new StatMessage(type);
    }
}

