package ch.epfl.lsr.adhoc.comessages;

import ch.epfl.lsr.adhoc.runtime.Message;
import java.util.Vector;

/**
 * This is a simple implementation of a custom made causal order message object.
 * <p>
 * This CoMessages is a implementation of Message, which allows to
 * set and get the Dsm historics of a message. 
 * It is created through the MessageFactory class
 * <p>
 * @see CoMessageFactory
 * @see Message
 * @author Alexandre Kozma
 */

public class CoMessages extends Message {
    
    /** Vector containing all historic  */
    private Vector DsmHist = new Vector();
    /**
     * A reference to the upperLayer Message 
     */
    private Message msg;
    /**
     * A reference to the sequence number of the message 
     */
    private int seqnb;
    
    
    /**
     * Creates a new instance of CoMessages.
     * <p>
     * The type of this message object is initialized at creation time and cannot
     * be changed later (for better use in a MessagePool).
     * <p>
     * @param type The type of service for this message
     */
    public CoMessages(char type) {
	super(type);
    }
    
    
    /**
     * Call the method vectorToLong() serialize the buffer
     * (for sending the message on the network).
     */
    public void prepareData() {
  	vectorToLong();
  	addMessage(msg);
    }
    
    /**
     * Call the methode longToVector() to deserialize.
     */
    public void readData() {
	longToVector();
	try{
	    msg = getMessage();
	}catch(Exception ex) {
	    throw new RuntimeException("Message type '" + msg + "' not found");
	}
    }  
    
    /**
     * This method set the Dsm historics of the message.
     * @param out the Dsm historics of the message.
     */
    public void setDsmHist(Vector out) {	
	DsmHist = out;
    }
    
    /**
     * This method set the sequence number of the message.
     * @param seqnb the Dsm historics of the message.
     */
    public void setSeqnb(int seqnb) {	
	this.seqnb = seqnb;
    }
    
    /**
     * Return the sequence number of the message. 
     * @return seqnb the sequence number of the messager.
     */
    public int getSeqnb() {	
	return seqnb;
    }
    
    /**
     * Return the message for the upper layer. 
     * @return msg the message for the upper layer.
     */
    public Message getMsgToUpperLayer(){
    	return msg;
    }
    
    /**
     * This method set the message for the upper layer.
     * @param msg the message for the upper layer.
     */
    public void setMsgToUpperLayer(Message msg){
	this.msg =msg;
    }  
    
    /**
     * This method returns the Dsm historics of the message.
     * @return DsmHist the Dsm Hist of the message.
     */
    public Vector getDsmHist() {
	return DsmHist;
    }
    
     /**
      * This method takes the vetor of the Dsm historics and get in each object in 
      * this vector the destination, the source and the message id, to be 
      * serialized. Called by prepareData().
      */
    public void vectorToLong(){
    	addInt(seqnb);
	Dsm hist =null;
  	int dim = DsmHist.size();
  	addInt(dim);
  	for(int i = 0; i < dim; i++){
	    hist = (Dsm)DsmHist.elementAt(i);
	    long dstId = hist.getDestId();
	    addLong(dstId);
	    long srcId = hist.getSourceId();;
	    addLong(srcId);
	    int msgId = hist.getMessageId();
	    addInt(msgId);
	}	
    }
    
    /**
     * This method takes the destination, source and message id, to create 
     * a vector of Dsm historic.This is the deserialization call by readData() 
     */
    public void longToVector(){
    	seqnb = getInt();
  	DsmHist.removeAllElements();
	
  	int dim = getInt();
  	for(int i = 0; i < dim; i++){
	    Dsm hist = new Dsm();
	    long dstId = getLong();
	    hist.setDestId(dstId);
	    long srcId = getLong();
	    hist.setSourceId(srcId);
	    int msgId = getInt();
	    hist.setMessageId(msgId);
	    DsmHist.insertElementAt(hist, i);
  	}	
    }
}
