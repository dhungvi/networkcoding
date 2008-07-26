package ch.epfl.lsr.adhoc.routing.aodv;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This MessageFactory creates instances of the RREPMessage class.
 * <p>
 * @author Alain Bidiville
 */
public class RREPMessageFactory implements IMessageFactory {
    
    /** Default constructor */
    public RREPMessageFactory() {
    }
    
    /** Creates and return a new RREP Message 
     *@return A message object, with the given type of service (RREPMessage)  */
    public Message createMessage(char type) {
	return new RREPmsg(type);
    }
}
