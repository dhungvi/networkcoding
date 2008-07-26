package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.Message;

/** Message that is needed to inform the ReliableLayer that it should be reinitialized. That reininitialization
 *  has been used by the tests with the Statisticlayer (RTT and Throughputmeasurements). So it's guaranteed that if we
 *  repeat the tests several times the beginning state is always the same.
 *
 * How is it used?
 * A Application who wants to reset the ReliableLayer only has to send this message and if the ReliableLayer gets the
 * message it's automatically reset.
 *
 * @author Leiggener Alain
 */
public class Reinit extends Message {
    /** Construct the Reinit (Reinitialization) Message. Set the Sequence Number */
    public Reinit(char type) {
	super(type);
    }
    /** Serialize the Data in the Reinit Message */
    public void prepareData(){
    }
    /** Deserialize de Data from the Reinit Message */
    public void readData(){
    }
}
