package ch.epfl.lsr.adhoc.services.neighboring;

import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.Dispatcher;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;

/**
 * This class implements the "hello" utlities like sending and processing of
 * hello messages.
 *
 * @see HelloMsg
 *
 * @author Reto Krummenacher
 */
class Hello implements Runnable {

    //FIELDS
    /**
     * The dispatcher which processes the outbound messages
     */
    private Dispatcher disp;

    /**
     * The message pool that provides the hello message instances
     */
    private MessagePool mp;

    private char msgType;
    private int helloInterval;


    //CONSTRUCTORS
    /**
     * Constructor of a new Hello instance.
     *
     * @param disp The dispatcher responsible for in- and outbound hello
     * messages
     */
    Hello(FrancRuntime runtime,char msgType, int helloInterval) {
        this.disp = runtime.getDispatcher();
        this.mp = runtime.getMessagePool();
        this.msgType = msgType;
        this.helloInterval = helloInterval;
    }

    //METHODS
    /**
     * This method is run by the thread broadcasting periodic hello messages
     */
    public void run() {

        sendHello();
        while(true) {
            try {
                FrancThread.sleep(helloInterval*1000);
            } catch (Exception e) {
                //throw e;
            }
            sendHello();
        }
    }

    /**
     * This method is responsible for the sending of periodic messages to the
     * dispatcher.
     */
    private void sendHello() {
        HelloMsg msg = null;
        try {
          msg = (HelloMsg)mp.getMessage(msgType);
			 msg.setDstNode(Message.BROADCAST);
			 msg.setTTL(1);
          disp.sendMessage(msg);
        } catch (SendMessageFailedException sme) {
          try {
            //in case of failure resend the message
            System.out.println("--> There was an error sending the message: " + sme.getMessage());
            FrancThread.sleep(500);
            disp.sendMessage(msg);
          } catch (InterruptedException ie) {
          } catch (SendMessageFailedException sme2) {
            System.out.println("hello message cannot be sent...");
          }
        }
    }

    /**
     * This method is proceeding incoming hello messages.
     * <p>
     * This method is not used for the implementation of the NeighborService but
     * is given for the sake of completeness.
     */
    public void processHello() {
    }
    /**
     * This method permit to change the hello interval dynamically
     */
    
    public void setInterval (int helloInterval){
    	this.helloInterval = helloInterval;
    }
    
    /**
     * This method permit to get the hello interval
     */
    
    public int getInterval (){
    	return helloInterval;
    }
}
