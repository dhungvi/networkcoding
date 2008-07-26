package ch.epfl.lsr.adhoc.comessages;

import ch.epfl.lsr.adhoc.runtime.Message;
import java.util.*;

/** This object contains all the Dsm,DsmWait historics and 
 *  the data of the message waiting to be deliver in order 
 *  for the upper layer. 
 *  @author Alexandre Kozma
 */
public class WaitBuffer {
    /** This object contains the destination, source and message sequence */
    private Dsm dsmWait;
    /** This vector contains all the DsmWait historics  */
    private Vector dsmWaitHist; 
    /** This is the message to forward to the upper layer.  */
    private Message msgWait;
    /** Default constructor */
    public WaitBuffer() {
  	
    }
    
    /** This method returns the Dsm of the message 
     * @return A Dsm object, with the destination, source and message id
     */
    public Dsm getDsm(){
	return dsmWait;
    }
    
    /** This method returns the Dsm historics of the message.
     *  @return A Dsm historic vector.
     */
    public Vector getDsmWaitHist(){
	return dsmWaitHist;
    }
    
    /** This method returns the message for the upper layer.
     *  @return The message for the upper layer. 
     */
    public Message getMsgWait(){
	return msgWait;
    }
    
    /** This method sets the Dsm of the message 
     *  @param dsmWait  A reference on the Dsm of the message
     * */
    public void setDsm(Dsm dsmWait){
	this.dsmWait = dsmWait ;
    }
    
    /** This method sets the Dsm historics of the message.
     *  @param dsmWaitHist  A reference on the Dsm of the message
     *  */
    public void setDsmWaitHist(Vector dsmWaitHist){
	this.dsmWaitHist = dsmWaitHist;
    }
    
    /** This method sets the message for the upper layer.
     *  @param msgWait  A reference on the message
     * */
    public void setMsgWait(Message msgWait){
	this.msgWait = msgWait;
    }
}

