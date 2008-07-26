package ch.epfl.lsr.adhoc.services.neighboring;

import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This class represents a single hello message.
 * Inheriting from Message for the use with the adhoc communication stack
 * it contains a message type.
 *
 * @see Message
 *
 * @author Reto Krummenacher
 */
class HelloMsg extends Message {


    //FIELDS
    //private char type;
    
    private String sourceName;

    //CONSTRUCTORS
//    HelloMsg() {
//    }

    HelloMsg(char type) {
      super(type);
//        this.type = type;
    }


    //GETTERS / SETTERS
//    public char getType() {
//        return type;
//    }
		
	  public void setSourceName (String sourceName)
	  {
	  	this.sourceName = sourceName;
	  }	
		
	  public String getSourceName ()
	  {
	  	return sourceName;
	  }


    //METHODS

    /**
     * This methods links the values received over the communication channel
     * to the fields of the hello message calling the method.
     * Used to create a hello message at reception of data.
     */
    public void readData() {
    	sourceName = getString();
    }

    /**
     * This method adds the content of a hello message to the serialized version
     * to transmit it over the communication channel.
     */
    public void prepareData() {
    	addString(sourceName);
    }

}
