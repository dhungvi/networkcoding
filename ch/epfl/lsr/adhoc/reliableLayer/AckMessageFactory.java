package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.IMessageFactory;
import ch.epfl.lsr.adhoc.runtime.Message;

/* Class to instantiant a Ack (Message). It's used when there have not been any messages produced yet and can't be taken from the MessagePool.
So it creates an Instance of the Ack (Message).
@ver mars 2003
@author Leiggener Alain
*/
public class AckMessageFactory implements IMessageFactory{

    /** Public Constructor */
    public AckMessageFactory(){
    }

    /** Creates and returns a new Ack Message */
    public Message createMessage(char type){
	return new Ack(type);
    }
}
