package ch.epfl.lsr.adhoc.tools;

import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.UnknownMessageTypeException;

public class DebugMessage extends Message {

    private Message internalMessage;

    public DebugMessage(char type) {
	super(type);
	internalMessage = null;
    }
    
    public void prepareData() {
	addMessage(internalMessage);
    }

    public void readData() {
	try {internalMessage = getMessage();}
	catch(UnknownMessageTypeException umte) {umte.printStackTrace();}		
    }

    public void setMsg(Message msg) {
	internalMessage = msg;
    }

    public Message getMsg(MessagePool mp) {
	return internalMessage;
    }
}
