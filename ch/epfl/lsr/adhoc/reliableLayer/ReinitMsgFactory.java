package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/** Class to instantiant a Reinit (Message). It's used when there are no messages yet produced and can't be taken from the MessagePool.
So it creates an Instance of the Reinit (Message).
@ver May 2003
@author Leiggener Alain
*/
public class ReinitMsgFactory implements IMessageFactory{

    /** Public Constructor */
    public ReinitMsgFactory(){
    }

    /* Creates and returns a new Ack Message */
    public Message createMessage(char type){
	return new Reinit(type);
    }
}
