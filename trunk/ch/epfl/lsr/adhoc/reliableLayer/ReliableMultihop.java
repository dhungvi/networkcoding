package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.io.*;
/**
 * This class adds more reliability to franc in the case of a Unicast (1-hop or multihop).
 * <br>To work properly, it has to be placed above the Routing Layer.
 *
 *
 * To be able to implement the Layer in the Framework there must be declared some parameters in the configuration
 * file of the Framework:
 * <lu>
 * <li>resendTh : The Treshold that defines the maximum number of times a message has to be resent (if no acknoledgment
 * has arrived in meantime)
 *<li>timeout : Defines the timeout that the Thread has to wait for untill a not acknowledged message is resent. Has to be given in
 *milliseconds.</li>
 *<li>testfile : It's optional! If this String is set, there will be printed some test information to the specified file.</li>
 *</lu>
 * @ver May 2003
 * @author Leiggener Alain
 */

public class ReliableMultihop extends AsynchronousLayer{

	Parameters params;

    /** A reference to the message pool */
    private MessagePool mp=null;
    /** The Timer-Thread, that controls the timeouts. */
    private CheckMultihop control=null;
    /** TTL of the AcknowledgeMessages sent by this Layer. Declared in config file */
    private int ttl;
    /** The TimeOut, set in the config file */
    private long TIME_OUT;
    private int AckMulti_type;
    private int InternMsg_type;

    /** Here, all outgoing Unicast Messages are stored. Needed to bind the outgoing MsgId to his specific Destinations object. */
    public HashMap sentUnicast;
    /** A LinkedList to order the specific timeouts. The values are Integers (reliableSeqNbr).*/
    private LinkedList orderTimeouts;
    /** The unique NodeId of this node */
    private Long nodeID;
    /** Reliable sequence number: Changes for every message passed to a lower Layer. Together with the NodeID it identify the
    message. It's needed because the other sequence number is set by the lowest Layer. */
    private int reliableSeqNbr=0;
    /** Threshold : Max. Number of times a message is to be resent. It's set in the config file. */
    private int resendTh;

    /** If it's a testphase (test=true) , a Stream to an output file is openend. */
    private boolean test=false;
    /** Testfile to print. Set in the configfile. */
    private String testfile=null;
    /** Stream to write the testfile. */
    private FileWriter fw;

    /**
     * The only constructer of this class.
     */
    public ReliableMultihop(String name, Parameters params){
	super(name, params);
	this.params = params;
    }
    /**
     * Initialize the layer.
     */
    public void initialize (FrancRuntime runtime){
	this.mp=runtime.getMessagePool();
	this.nodeID=new Long(runtime.getNodeID());
        try{
            AckMulti_type = mp.getMessageType ("MHAck");
          }
        catch(Exception ex){
          throw new RuntimeException("Error reading the MHAck type parameter: " +ex.getMessage());
        }
        try{
            InternMsg_type = mp.getMessageType ("InternMsg");
          }
        catch(Exception ex){
          throw new RuntimeException("Error reading the InternMsg type parameter: " +ex.getMessage());
        }

       try {
          /* in Milliseconds !*/
	    TIME_OUT = params.getLong("timeout");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the timeout parameter: " + pdnee.getMessage());
	}
	try {
	    resendTh = params.getInt("resendThreshold");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the ResendThreshold parameter: " + pdnee.getMessage());
	}
        /* the ttl is set to 1. Since its reliablity at one hop thats enough to reach its direct neighbors. */
        try {
	    ttl = params.getInt("ttl");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the ttl parameter: " + pdnee.getMessage());
	}
        try {
	    testfile = params.getString("testoutputfile");
	} catch(ParamDoesNotExistException pdnee) {
	    test=false;
	}
        if(testfile!=null)
			test=true;
    }

    public void startup(){
	/* create Hashtables for sent messages with default capacity=16 and Loadfactor=0.75 */
	sentUnicast=new HashMap();
	orderTimeouts = new LinkedList();
        /* if it's a testphase */
        if(test){
          try{
            fw=new FileWriter(testfile);
            fw.write("Go\n");
            fw.write("Timeout: "+TIME_OUT+"(ms)\n");
            fw.flush();
          }
          catch(IOException ex) {
            System.out.println("Could not open TestOutputFile");
          }
        }
        /* start Thread to control to handle the Timeout */
	control=new CheckMultihop(this, sentUnicast, orderTimeouts);
	control.start(); // start the ControlThread
	start();
    }


    /**
     * Overwrite the Method of the AsynchronousLayer. The message and the necessary informations to being able to resend
     * the message are stored in the appropriate Data-structures. At last the message is send to the lower layer.
     */
    public synchronized int sendMessage(Message msg) throws SendMessageFailedException {
	/* save the message in the Msg_Table, but only if it's not an Acknowledge Message */
	if(msg.getType()!=AckMulti_type){
             long dstNode=msg.getDstNode();
             if(dstNode!=0){ // test if it's unicast
                 //  Get timeout , to add in the Destinations Object  */
                 long timeout=System.currentTimeMillis()+TIME_OUT;
                 /* Increment the internal copycounter of the message. So the message can't be reused until all copies are free  */
                 msg.createCopy();
                 /* Object to save all necessary Data to resend the message */
                 UnicastDestination dest=new UnicastDestination(timeout, msg);
                 dest.setDest(dstNode);
                    //System.out.println("Unicast Message to :" +dstNode+" RelSeqnbr: "+reliableSeqNbr);
                    //System.out.println("Node added to Destination (Unicast): "+dstNode+"\n");
                   reliableSeqNbr=((reliableSeqNbr +1 ) & 0xFFFF);
                   msg.setReliableSeqNbr(reliableSeqNbr);
                   /* The key of the Hashtable is generated */
                   Integer key=new Integer(reliableSeqNbr);
                   sentUnicast.put(key,dest);
                   orderTimeouts.addLast(key);
                   /* if it's a test. Print to the outputstream */
                   if(test){
                      if(msg.getType()==32){
                        if(msg.getDstNode()!=0){
                            try{
                              fw.write("Unicast Textmessage sent.\n");
                              fw.write("At time: "+System.currentTimeMillis()/1000+" sec.\n");
                              fw.flush();
                              fw.write("\t to"+dest.getDst()+"\n");
                              fw.flush();
                            }
                            catch(IOException ex){
                              System.out.println("Cannot write to the file");
                            }
                        }
                      }
                   }

                   /* if this message is the first and only one in the Hashtable, it has to notify the control
                      Thread. The control Thread was blocked (wait()) before. */
                   if(sentUnicast.size()>=1&&control.getAwake()==false){
                       //System.out.println("call go()");
                       control.setAwake(true);
                       control.go();
                   }
                } //end unicast
          }
	/* send the message to the next lower Layer */
	return super.sendMessage(msg);
    }

    /** Method to send a message one more time. It's called from the TimeoutControlThread.
     *
     */
    protected synchronized void reSendMessage() {
    /*It must be synchronized, since 2 calls at parallel time
     *  from the CheckAcknowledge Thread could cause to resend the same message 2 times (firstElement()). */
      try{
          Integer nextResend=(Integer)orderTimeouts.getFirst();
          UnicastDestination dest=(UnicastDestination)sentUnicast.get(nextResend);
          Message msg=dest.getMessage();
          /* If there aren't any neighbors left the message isn't resend */
          if(dest!=null){
            if(dest.size()!=0){
                System.out.println("copie resend... ");
                msg.createCopy(); // The message needs to life on after sending
                /* This message is sent the last one, so its Timeout is put on the end of the Timeoutvector */
                orderTimeouts.remove(nextResend);
                //  Get timeout , to add in the Destinations Object  */
                long timeout=System.currentTimeMillis()+TIME_OUT;
                dest.setTime(timeout);
                orderTimeouts.addLast(nextResend);
                try{
                    super.sendMessage(msg);

                    System.out.println("Resent TextMessage: "+msg.getReliableSeqNbr());
                    /* testoutputfile */
                     if(test){
                      if(msg.getType()==32){
                        try{
                          fw.write("Textmessage RESENT. RelSeqnbr:"+msg.getReliableSeqNbr()+". The "+(dest.getNbr_Resend()+1)+". time\n");
                          fw.write("At time: "+System.currentTimeMillis()/1000+" sec.\n");
                          fw.flush();
                        }
                        catch(IOException ex){
                          System.out.println("Cannot write to the file");
                        }
                      }
                   } /* end test */
                }
                catch (SendMessageFailedException ex) {
                    System.out.println(">Couldn't resend the Message: "+ex.getMessage());
                }
                dest.incrementNbr_Resend();
            }
            /* it could happen that in meantime (between the call to the isRebroadcastneeded method an the resendMethod) the remaining ack are
              arrived and there is no need to resend the message. So it's deleted here. */
            else{
                sentUnicast.remove(nextResend);
                orderTimeouts.remove(nextResend);
                mp.freeMessage(msg); /* copy field of Message is decremented-> can be used otherwise */
            }
          }
         }
         catch (NoSuchElementException ex) {}
    }

    /**
     * Overwrite the appropriate Method in the AsynchronousLayer.
     */
    public void handleMessage(Message msg){
	if(msg.getType()==AckMulti_type){
	    MHAck ack=(MHAck)msg;
            /* if the MHAck message is passed untill this layer, it must be for me. */
            synchronized (this) {
                Integer relSeqNbr=new Integer(ack.getSeqNbrToAck()); // We need an Integer Object
                UnicastDestination dest=(UnicastDestination)sentUnicast.get(relSeqNbr);
                /* delete the correspondending entry in the Destinations Object */
                if(dest!=null){
                    dest.updateDest(ack.getSrcNode());
                     if(test){
                        Message msgAck=dest.getMessage();
                        if(msgAck.getType()==32){
                          try{
                            fw.write("Multihop Unicast Message ack from: NodeID: "+ack.getSrcNode()+"\n");
                            fw.flush();
                          }
                          catch(IOException ex){
                            System.out.println("Cannot write to the file");
                          }
                        }
                      } /* end test */
                    // deleted in the Meantime.
                    //System.out.println("Message Acknowledged: "+relSeqNbr+". And Source ("+ack.getSrcNode()+") Deleted from Destinations");
                }
            }
	    /* Discard the Ack. It's getting free for new Ack arriving */
	    mp.freeMessage(msg);
	}
        else{ // it's not an acknowledge
            if(msg.getType()==InternMsg_type){
                  // It's a intern message from the ReliableBroadcast Layer to inform this Layer about a change in
                  // the RelSeqNbr of a message sent.
                  InternMsg intern=(InternMsg)msg;
                  synchronized(this){
                      Integer oldSeq=new Integer(intern.getOldRelSeqNbr());
                      UnicastDestination dst=(UnicastDestination)sentUnicast.get(oldSeq);
                      Integer newSeq=new Integer(intern.getNewRelSeqNbr());
                      //put the new entries
                      sentUnicast.put(newSeq, dst); // the seq is reset
                      orderTimeouts.addLast(newSeq);
                      //Delete the old entries
                      sentUnicast.remove(oldSeq);
                      orderTimeouts.remove(oldSeq);
                  }
                  mp.freeMessage(msg);
            }
            else{  // it's a unicast to acknowledge
                    //System.out.println("Incoming message, SourcNode: "+source)

                    /* If it was a unicast to me, an Acknowledge for the message just arrived is send */
                    if(nodeID.equals(new Long(msg.getDstNode()))){
                        try{
                            sendAck(msg.getReliableSeqNbr(), msg.getSrcNode());
                        }
                        catch (SendMessageFailedException e) {
                            System.out.println("> # Could not send UnicastAcknowledge: " + e.getMessage());
                        }
                    }
                    /* The message is always passed to the upper Layer. The control if it is arrived a second time is
                    made in the ReliableBroadcast Layer */
                    super.handleMessage(msg);
                }
          } // end no acknowledge
    }

    /**
     * Private Method that creates an acknowledgment and passes it to the lower layer to
     * be sent on the network.
     */
    private void sendAck (int seqNbr, long oldSource) throws SendMessageFailedException{
	MHAck ack=null;
	try{
	    ack=(MHAck)mp.getMessage((char)AckMulti_type);
	    ack.setSeqNbrToAck(seqNbr);
	    ack.setOldSource(oldSource);
	    ack.setDstNode(oldSource);
	    ack.setTTL(ttl);  // TTL have to be bigger here. Maybe we set it in the config file.
	}
	catch (Exception e){
	    System.out.println("> Could not create the Acknowledgemessage");
	}
	/*send the Message*/
	sendMessage((Message)ack);
    }


    /** Controls if the next Message in the queue is really needed to be resend. If not, the message is deleted from the Hashmap.
     */
    protected synchronized boolean isResentNeeded(){
      try{
        Integer nextResend=(Integer)orderTimeouts.getFirst();
        UnicastDestination dest=(UnicastDestination)sentUnicast.get(nextResend);
        if(dest!=null){
          Message msg=dest.getMessage();
          Integer relSeqNbr=new Integer(msg.getReliableSeqNbr());
          if(dest.size()!=0){
                if(dest.getNbr_Resend()<resendTh){
                        //System.out.println("Rebroadcast needed. RelSeqNbr: "+msg.getReliableSeqNbr());
                          /* only for testing purpose
                         if(test){
                          if(msg.getType()==32){
                            try{
                              fw.write("Unicast Textmessage need to be REsent by the ReliableMultihop Layer. RelSeqnbr: "+msg.getReliableSeqNbr()+"\n");
                              fw.flush();
                            }
                            catch(IOException ex){
                              System.out.println("Cannot write to the file");
                            }
                          }
                        } */
                        return true;
                }
                else{
                      System.out.println("Message has been resent a maximum times");
                      //System.out.println("del dest ...with msg relSeqnbr"+seqNbr);
                      sentUnicast.remove(relSeqNbr);
                      orderTimeouts.remove(relSeqNbr);
                     /* testoutputfile */
                     if(test){
                      if(msg.getType()==32){
                        try{
                          fw.write("No more resend (max resends done) of Textmessage with RelSeqnbr: "+msg.getReliableSeqNbr()+"\n");
                          fw.write("At time: "+System.currentTimeMillis()/1000+" sec.\n");
                          fw.flush();
                        }
                        catch(IOException ex){
                          System.out.println("Cannot write to the file");
                        }
                      }
                   } /* end test */
                   mp.freeMessage(msg);
                } // end else
          }
          else {// dest.size()==0
               /*
            if the destination node has ack' the message-> no rebroadcast is needed and the entry in the senMessage Table can be deleted.
             */
             System.out.println("del dest ...with msg relSeqnbr"+msg.getReliableSeqNbr());
              sentUnicast.remove(relSeqNbr);
              orderTimeouts.remove(relSeqNbr);
              if(test){
                      if(msg.getType()==32){
                        try{
                          fw.write("No more resend of Textmessage with RelSeqnbr: "+msg.getReliableSeqNbr()+"\n");
                          fw.write("At time: "+System.currentTimeMillis()/1000+" sec.\n");
                          fw.flush();
                        }
                        catch(IOException ex){
                          System.out.println("Cannot write to the file");
                        }
                      }
              } /* end test */
              mp.freeMessage(msg);
          } // end else dest.size()=0

          //mp.freeMessage(msg); // The copie field of the message needs to be decremented here, that the message is usable to new messages. It will be accable in the message Pool.
        }// end dest!=null
      } // end try
      catch(NoSuchElementException ex)  {}
      return false;
    }
}
