package ch.epfl.lsr.adhoc.comessages;


import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This CoMessageFactory creates instances of the CoMessages class.
 * @author Alexandre Kozma
 */


public class CoMessagesFactory implements IMessageFactory {
    /** Default constructor */
    public CoMessagesFactory() {
    }
    
    /**
     * Creates a new message object (of type CoMessages) with the given type of
     * service.
     * <p>
     * @param type The type of service for the message object
     * @return A message object, with the given type of service (CoMessages)
     */
    public Message createMessage(char type) {
	return new CoMessages(type);
    }
}
