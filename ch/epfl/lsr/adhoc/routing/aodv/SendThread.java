package ch.epfl.lsr.adhoc.routing.aodv;

import java.util.*;
import ch.epfl.lsr.adhoc.runtime.FrancThread;

/**
 * This class is used to send Route Request Messages
 * <p>
 * The thread sleeps an each time recalculated period of time
 * When it awakes, it sends the Route Request Messages which have to be sent at this moment from the buffer of Route Request Messages which are being sent
 * <p>
 * @author Alain Bidiville
 */
public class SendThread extends FrancThread {
    private boolean condition;
    private AODV aodv;
    /** This value indicates the next period of time for which the thread has to sleep */
    private long timeToSleep;
    
    public SendThread(AODV aodv){
	this.aodv=aodv;
    }
    
    /** The run method to activate the thread
     *This thread checks the buffer of the Route Request which are being sent for those which have to be sent immediately and then sends them 
     *For each Message which will be sent, the following actions are performed :
     *<p>
     * <li> - The node's own sequence number is incremented by one </li>
     * <li> - The node's own ID for Route Request Messages is incremented by one </li>
     * <li> - The ID of the Route Request Message and the ID of the node are added to the list of Route Request Messages which were sent or forwarded by this node  </li>
     * <p>
     *It then updates the entry of these ones in the buffer :
     * <p>
     * <li><b> TimeToWait </b> - The TimeToWait is multiplied by two </li>
     * <li><b> currentRREQtries </b> - The currentRREQtries is incremented by one </li>
     * <li><b> mustBeSentAtOnce </b> - The mustBeSentAtOnce boolean value is set to false </li>
     * <li><b> nextTimeToBeSent </b> - The nextTimeToBeSent value is revaluated </li>
     * <p>
     **/
    public void run() {
	condition=true;
	try {
	    
	    while(getCondition()){
	    	synchronized (aodv) {
		    aodv.updateRREQsSentBuffer();
		    aodv.updateRREQsentIDsPairs();
		    //aodv.printStateOfRREQsSentBuffer();
		    Vector RREQsSentBuffer = aodv.getRREQsSentBuffer();
		    for (int i=0;i<RREQsSentBuffer.size();i++){
	    		RREQsentBufferEntry rste = (RREQsentBufferEntry)RREQsSentBuffer.get(i);
	    		if (rste.getmustBeSentAtOnce()){
			    SeqNumberAndRREQIDsController SeqNumberAndRREQIDsControllerObjet = aodv.getSeqNumberAndRREQIDsControllerObjet();
			    SeqNumberAndRREQIDsControllerObjet.incrementRREQ_ID();
			    aodv.setSeqNumberAndRREQIDsControllerObjet(SeqNumberAndRREQIDsControllerObjet);
			    (rste.getRREQ()).setRREQ_ID((aodv.getSeqNumberAndRREQIDsControllerObjet()).getRREQ_ID());
			    rste.setTimeToWait(rste.getTimeToWait()*2);
			    rste.setnextTimeToBeSent(rste.getTimeToWait()+System.currentTimeMillis());
			    rste.setcurrentRREQtries(rste.getcurrentRREQtries()+1);
			    rste.setmustBeSentAtOnce(false);
			    Vector rsidp = aodv.getRREQsentIDsPairs();
			    rsidp.add(new RREQsentIDsPair((rste.getRREQ()).getSrcNode(), (rste.getRREQ()).getRREQ_ID(), System.currentTimeMillis() + aodv.getPATH_DISCOVERY_TIME()));
			    aodv.setRREQsentIDsPairs(rsidp);
			    /*System.out.println("SendThread RREQsentIDspair added. SrcNode : "+(rste.getRREQ()).getSrcNode()+" and RREQ ID : "+(rste.getRREQ()).getRREQ_ID());
			    for(int j=0;j<((rste.getRREQ()).getIDandSEQNBpairs()).size();j++){
				System.out.println("pair no : "+j+": nodeID : "+((IDandSEQNBpair)((rste.getRREQ()).getIDandSEQNBpairs()).get(j)).getNODE_ID()+", seqnb : "+((IDandSEQNBpair)((rste.getRREQ()).getIDandSEQNBpairs()).get(j)).getNODE_SEQNB());
			    }*/
			    (rste.getRREQ()).createCopy();
			    aodv.superSendMessage(rste.getRREQ());
			    
			    
			}	
		    }
		    
	    	}
		
	    	timeToSleep=aodv.calculateTimeToSleep();
	    	if(timeToSleep==-1){
		    setCondition(false);
		}
		else{
		    FrancThread.sleep(timeToSleep);
		}
	    }
	    
	} catch(Exception e) {
	    System.err.println("Exception: " + e);
	    e.printStackTrace();
	}
	
	
    }//run
    
    public synchronized void setCondition(boolean condition){
	this.condition=condition;
    }
    
    public synchronized boolean getCondition(){
	return condition;
    }
    
}


