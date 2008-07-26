package ch.epfl.lsr.adhoc.services.neighboring;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This is the message factory for hello messages.
 *
 * @author Reto Krummenacher
 *
 * @see IMessageFactory
 * @see MessagePool
 */
public class HelloMsgFactory implements IMessageFactory {

    //CONSTUCTORS
    public HelloMsgFactory() {
    }

    /**
     * This method creates a new message object (of type = HelloMsg).
     *
     * @param type The type of the message
     * @return A message object of type HelloMsg
     */
    public Message createMessage(char type) {
        return new HelloMsg(type);
    }
}

