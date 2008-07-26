package ch.epfl.lsr.adhoc.routing.aodv;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This MessageFactory creates instances of the RERRMessage class.
 * <p>
 * @author Alain Bidiville
 */
public class RERRMessageFactory implements IMessageFactory {
    
    /** Default constructor */
    public RERRMessageFactory() {
    }
    
    /** Creates and return a new RERR Message 
     *@return A message object, with the given type of service (RERRMessage)  */
    public Message createMessage(char type) {
	return new RERRmsg(type);
    }
}
