package ch.epfl.lsr.adhoc.routing.aodv;


import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This MessageFactory creates instances of the RREQMessage class.
 * <p>
 * @author Alain Bidiville
 */
public class RREQMessageFactory implements IMessageFactory {
    
    /** Default constructor */
    public RREQMessageFactory() {
    }

    /** Creates and return a new RREQ Message 
     *@return A message object, with the given type of service (RREQMessage)  */
    public Message createMessage(char type) {
	return new RREQmsg(type);
    }
}
