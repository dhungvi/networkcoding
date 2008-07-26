package ch.epfl.lsr.adhoc.comessages;

/**
 * This is an object that store the destination, source and
 * the message id of an message.
 * @author Alexandre Kozma
 */

public class Dsm {
    
  /**
   * The source id of the message
   */
    private long sourceId;
  /**
   * The destination id of the message
   */
    private long destId; 
  /**
   * The message id 
   */
    private int messageId;
    
  /**
   * The default contructor
   */
    public Dsm() {
    }
    
    /**
     * This method returns the source id of the message.
     * @return sourceId the source id of the message.
     */
    public long getSourceId(){
	return sourceId;
    }
    
    /**
     * This method returns the destination id of the message.
     * @return destId the destination id of the message.
     */
    public long getDestId(){
	return destId;
    }
    
    /**
     * This method returns the message id of the message.
     * @return messageId the message id of the message.
     */
    public int getMessageId(){
	return messageId;
    }
    
    /**
     * This method sets the source id of the message.
     * @param srcId the source id of the message.
     */
    public void setSourceId(long srcId){
	sourceId = srcId ;
    }
    
     /**
      * This method sets the destination id of the message.
      * @param dstId the destination id of the message.
      */
    public void setDestId(long dstId){
	destId = dstId;
    }
    
    /**
     * This method sets the message id of the message.
     * @param msgId the source id of the message.
     */
    public void setMessageId(int msgId){
	messageId = msgId;
    }
    
    /**
     * This method overrides the contains method.Check a dsm given in parameter
     * and a vector of dsm.  .
     * @param obj the dsm object to compart.
     * @return If the dsm is in the vector 
     * (mean the same destination, source, message id) it return true.
     */
    public boolean equals(Object obj){
	Dsm y = (Dsm)obj;
	if((this.sourceId == y.getSourceId())&&
	   (this.destId == y.getDestId())&&
	   (this.messageId == y.getMessageId())){
	    return true;
	}
	return false;  
    }
    
    /**
     * This method sets the parameters destination, source and message id of a dsm.
     * @param dstId the destination id of the message.
     * @param srcId the source id of the message.
     * @param msgId the message id of the message.
     */
    public void setParameters(long dstId, long srcId, int msgId){
	this.destId = dstId;
	this.sourceId = srcId;
	this.messageId = msgId;
    }
    
    /**
     * This method sets the default value for a dsm.Source Id, 
     * destination Id and message Id to zero.
     */
    public void init(){
 	sourceId = 0;
  	destId = 0;
   	messageId = 0;
    }
    
    /**
     * This method checks if a Dsm is null.
     */
    public boolean testIfNull(){
	return((this.sourceId == 0)&&
	       (this.destId == 0)&&
	       (this.messageId == 0));
    }
    
    /**
     * This method checks if the node id is the same as send in parameter.
     * Need to keep only the message for the node.
     * @param nodeId the node id which come from a Dsm historic.
     */
    public boolean filterDestId(long nodeId){
	return(destId == nodeId);
	
    }
}
