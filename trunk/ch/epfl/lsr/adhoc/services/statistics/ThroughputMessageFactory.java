package ch.epfl.lsr.adhoc.services.statistics;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This is the factory of the message with type ThroughputMessage.
 *
 * @see ThroughputMessage
 * @see IMessageFactory
 *
 * @author Christophe Crausaz
 */
public class ThroughputMessageFactory implements IMessageFactory {

  public ThroughputMessageFactory() {
  }

  //METHODS
    /**
     * This method creates a new message object (of type = ThroughputMessage).
     *
     * @param type The type of the message
     * @return A message object of type ThroughputMessage
     */
    public Message createMessage(char type) {
        return new ThroughputMessage(type);
    }
}

