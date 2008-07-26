package ch.epfl.lsr.adhoc.reliableLayer;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.UnknownMessageTypeException;
import ch.epfl.lsr.adhoc.services.neighboring.NeighborService;
import ch.epfl.lsr.adhoc.services.neighboring.NeighborTable;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Set;
import java.io.*;

/**
 * This class adds more reliability to FRANC. It guarantees more
 * reliablility in the case of Broadcast at 1 hop and Unicast at 1 hop in the Adhoc Network.
 *<br>
 * This Layer has to be put <b>above the Communication- and VirtualNetwork layer</b>.
 *<br><br>
 * Remark!
 * <lu><li>The reliability is not 100% guaranteed.</li>
 * <li>Every message is delivered only once to the upper layer. If the message has been resent
 * and this node get the same message a second time, the message is discarded. But one sends
 * always an acknowledgment to the Source Node. </li>
 *</lu><br><br>
 * To use the layer some parameters must be declared in the configuration
 * file of the Framework:
 * <lu>
 * <li>resendTh : The Treshold that defines the maximum number of times a message has to be resent.
 * <li>maxRecMessages : To not deliver a message more than once to the upper layer a table structure was implemented
 * that stores the IDs of the messages, that have already been received in this node. This parameter defines the maximum
 * of messages that are remebered for one node. If this Threshold is choosen to small the 2. point of the defintions, given above,will
 * be no more guaranteed. However there is no control made by the program.
 * <li>maxNbRec : Defines the limit of neighbors that has actually stored the messages in the appropriate table structure. But it is handled dynamically
 * by this layer, so that no entry from a source node which is still in the neighborhood is deleted.</li>
 *<li>timeout : Defines the span of time the thread has to wait for until a not acknowledged message is resent. Has to be given in
 *milliseconds.</li>
 *<li>testfile : It's optional! If this String is set, there will be printed some test information to the specified file.</li>
 *</lu>
 *
 * @ver June 2003
 * @author Leiggener Alain
 */

public class ReliableLayer extends AsynchronousLayer{

	Parameters params;

    /** A reference to FRANc runtime*/
    FrancRuntime runtime;
    /** A reference to the message pool */
    private MessagePool mp=null;
    /** The type of the Ack Message.*/
    private int Ack_type;
    /** The type of the Reinit Message.
     */
    private int Reinit_type=-3;
    /** The type of the Intern Message. Initialy set to -2. Is read from the ReliableMultihop section in the config file. */
    private int Intern_type=-2;
    /** The type of the ack message for multihop messages. Initialy set to -1. If the ReliableMultihop is used, the type have to
     *   be set in the config File at the ReliableMultihop Section.*/
    private int AckMulti_type=-1;
    /**
     * Reference to the tread that controls the timeouts of messages sent.
     */
    private CheckAcknowledge control=null;
    /** TTL of the AcknowledgeMessages sent by this Layer. Declared in config file */
    private final int ttl=1;
    /** The TimeOut, set in the config file */
    private long TIME_OUT=0;

    /** Needed to bind the outgoing MsgId to his specific Destinations object (The object where all destinations will be stored) */
    public HashMap sentMessages;
    /** Hashtable to put the messages already received on this node. The message is identified by the reliableSeqNbr and the
     SourceNode ID. */
    public HashMap recMessages;
    /**
     *  Every Node in the neighborhood has a maximum number of messages that have stored the msgID (message IDs in the
     * recMessages. If the number of messages received from a node, and thus the number of messages in
     * the vector object in the HashMap, reaches this Maximum, set in the Config File, the oldest message ID is deleted,
     * each time a new message arrives. */
    private int maxRecMessages;
    /**
    * The number of nodes (thus the large of the HashMap) in the recMessages should not be to big.
    * If a certain max number of size is reached, the entries are controlled if they are still actual.
    * If in the meantime there have gone some neighbors, its appropriate entries are deleted.
    */
    private int maxNbRec;

    /** A Vector to order the specific timeouts. The values are Integers  (reliableSeqNbr) */
    private LinkedList orderTimeouts;

    /* Neighborservice: There the actual Neighbors are fetched.*/
    private NeighborService neighbor;

    /* The unique NodeId of this node */
    private Long nodeID;

    /** Reliable sequence number: Changes for evry message passed to a lower Layer. Together with the NodeID it identifies the
       message.
    */
    private int reliableSeqNbr=0;
    /** Threshold : Maximum number of times a message has to be resent. It is set in the config file */
    private int resendTh;

    /** If it is a testphase , a Stream to the specified output file is openend. */
    private boolean test=false;
    /** The appropriate Testfile for the outputstream */
    private String testfile=null;
    /** The fileWriter Stream. */
    private FileWriter fw;

    /**
     * The only constructer of this class.
     */
    public ReliableLayer(String name, Parameters params ){
	super(name, params);
	this.params = params;
    }
    /**
     * Initialize the layer.
     */
    public void initialize (FrancRuntime runtime){
	this.runtime = runtime;
	this.mp=runtime.getMessagePool();
	this.nodeID=new Long(runtime.getNodeID());
        Ack_type = mp.getMessageType ("Ack");

        /* The next 3 message types are not obligatory. They are only needed if the ReliableMultihop Layer is activated too. */
        try{
           Reinit_type = mp.getMessageType ("Reinit");
        }
        catch(Exception ex){}
        try{
            AckMulti_type = mp.getMessageType ("MHAck");
          }
        catch(Exception ex){}
        try{
            Intern_type = mp.getMessageType ("InternMsg");
          }
        catch(Exception ex){}


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
        try {
	    maxRecMessages = params.getInt("maxRecMessages");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the maxRecMessages parameter: " + pdnee.getMessage());
	}
        try {
	    maxNbRec = params.getInt("maxNbRec");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the maxNbRec parameter: " + pdnee.getMessage());
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
	this.neighbor=(NeighborService)runtime.getService("Neighboring");
	/* create Hashtables for sent messages with default capacity=16 and Loadfactor=0.75 */
	sentMessages=new HashMap();
	recMessages = new HashMap();
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
        /* start Thread to control, to handle the Timeout */
	control=new CheckAcknowledge(this, sentMessages, orderTimeouts);
	control.start(); // start the ControlThread
	start();
    }

    /** Method that reinitialize the Hashtables and the ControlThread used by this Layer.
     */
    private synchronized void reinitialize(){
      control.setAwake(true);
      control.terminate();
      try{
        sleep(10000);
      }
      catch(InterruptedException ex) {
        System.out.println("Error while sleeping..."+ex.getMessage());
      }
      sentMessages=null;
      recMessages=null;
      orderTimeouts=null;
      control=null;
      sentMessages=new HashMap();
      recMessages=new HashMap();
      orderTimeouts=new LinkedList();
      control=new CheckAcknowledge(this, sentMessages, orderTimeouts);
      control.start();
    }

    /**
     * Overwrite the Method of the AsynchronousLayer. There the message is send to the lower layer and the Timer is started.
     */
    public synchronized int sendMessage(Message msg) throws SendMessageFailedException {
	/* save the message in the Msg_Table, but only if it's not an Acknowledge Message */
        int msgType=msg.getType();
	if((msgType!=Ack_type)&&(msgType!=AckMulti_type)){
          /* if the source field is already set, it's impossible to get an ack. That's the case if the rooting algorithm
             decides to send a received message to its neighbors. In this case the source is the same as before and not this node */
            if(msg.getSrcNode()== 0){
               /* We want to set the ReliableseqNbr of the message. If the Field has already been set by the upper Layer (ReliableMultihop)
                    this field is no more touched by this Layer. */
              reliableSeqNbr=((reliableSeqNbr +1 ) & 0xFFFF);
              int oldRelSeqNbr=msg.getReliableSeqNbr();
              msg.setReliableSeqNbr(reliableSeqNbr);
              if(oldRelSeqNbr!=-1){ // it was set by the multihopReliable Layer
                  System.out.println("Unicast Message with RelSeqnbr: "+reliableSeqNbr);
                  sendInternMessage(oldRelSeqNbr, reliableSeqNbr);
               }
               /* find all Neighbors */
               NeighborTable neighbors=neighbor.getTable();  // returns a (Vector)NeighborTable object
               long [] nbs=neighbors.getNeighborsIds();
               long dstNode=msg.getDstNode();
               boolean correct=true; // true: neighbor exists, false: error, neighbor doesn't exist
               if(dstNode!=0){ // test only if it's unicast
                  // test if it's really in his neighborhood
                  correct=false;
                  for(int i=0; i<nbs.length; i++){
                    if(nbs[i]==dstNode)
                      correct=true;
                  }
               }
               if(correct){
                   //  Get timeout , to add in the Destinations Object  */
                   long timeout=System.currentTimeMillis()+TIME_OUT;
                   /* Increment the internal copycounter of the message. So the message can't be reused until all copies are free  */
                   msg.createCopy();
                   /* Object to save all necessary Data to resend the message */
                   Destinations dest=new Destinations(timeout, msg);
                /* if it's a broadcast ... */
                   if(dstNode==0){
                      for(int i=0; i<nbs.length; i++){
                        dest.addDest(nbs[i]);
                       }
                    }
                    else{ // it's unicast
                      dest.addDest(dstNode);
                    } // end unicast only
                     /* The key of the Hashtable is generated */
                     Integer key=new Integer(reliableSeqNbr);
                     sentMessages.put(key,dest);
                     orderTimeouts.addLast(key);
                     /* if it's a test. Print to the outputstream */
                     if(test){
                        if(msg.getType()==32){
                          try{
                            fw.write("Textmessage sent. RelSeqnbr:"+reliableSeqNbr+"\n");
                            fw.write("At time: "+System.currentTimeMillis()/1000+" sec.\n");
                            fw.write("Actual Neighbors: \n");
                            if(dest.size()==0)
                              fw.write("No Neighbors known at this time\n");
                            fw.flush();
                            for(int i=0; i<dest.size(); i++){
                              fw.write("\t"+dest.get(i)+"\n");
                              fw.flush();
                            }
                          }
                          catch(IOException ex){
                            System.out.println("Cannot write to the file");
                          }
                        }
                     }
                     /* if this message is the first and only one in the Hashtable, it has to notify the control
                        Thread. The control Thread was blocked (wait()) before. */
                     if(sentMessages.size()>=1&&control.getAwake()==false){
                         control.setAwake(true);
                         control.go();
                     }
                  } //end correct (has put all data to the appropriate datastructures)
            }
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
          Destinations dest=(Destinations)sentMessages.get(nextResend);
          Message msg=dest.getMessage();
          /* If there aren't any neighbors left the message isn't resend */
          if(dest!=null){
            if(dest.size()!=0){
                msg.createCopy(); // The message needs to life on after sending
                /* This message is sent the last one, so its Timeout is put on the end of the Timeoutvector */
                orderTimeouts.remove(nextResend);
                //  Get timeout , to add in the Destinations Object  */
                long timeout=System.currentTimeMillis()+TIME_OUT;
                dest.setTime(timeout);
                orderTimeouts.addLast(nextResend);
                try{
                    super.sendMessage(msg);
                     /*if(msg.getType()==32)
                        System.out.println("Resent TextMessage: "+msg.getReliableSeqNbr());*/
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
                sentMessages.remove(nextResend);
                orderTimeouts.remove(nextResend);
                 if(test){
                    if(msg.getType()==32){
                      try{
                        fw.write("One Neighbor deleted from my actual Table: NodeID: "+nextResend+"\n");
                        fw.flush();
                      }
                      catch(IOException ex){
                        System.out.println("Cannot write to the file");
                      }
                    }
                  } /* end test */
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
        int msgType=msg.getType();
	if(msgType==Ack_type){ /* the Ack is of type 77 (definied in the config-file). So we have to check if it's an Acknowledge for this node */
	    Ack ack=(Ack)msg;
	    //System.out.println("Ack from "+ack.getSrcNode()+" arrived");
	    /* To identify the message we need the old SourceNode ID and the Sequencenumber to be the same */
	    if(nodeID.equals(new Long(ack.getOldSource()))){
                synchronized (this) {
                    Integer relSeqNbr=new Integer(ack.getSeqNbrToAck()); // We need an Integer Object
                    Destinations dest=(Destinations)sentMessages.get(relSeqNbr);
                    /* delete the correspondending entry in the Destinations Object */
                    if(dest!=null){
                        dest.updateDest(ack.getSrcNode());
                         if(test){
                            Message msgAck=dest.getMessage();
                            if(msgAck.getType()==32){
                              try{
                                fw.write("One Neighbor deleted from my actual Table: NodeID: "+ack.getSrcNode()+"\n");
                                fw.flush();
                              }
                              catch(IOException ex){
                                System.out.println("Cannot write to the file");
                              }
                            }
                            msgAck=null;
                          } /* end test */
                        // deleted in the Meantime.
                        //System.out.println("Message Acknowledged: "+relSeqNbr+". And Source ("+ack.getSrcNode()+") Deleted from Destinations");
                    }
                }
	    }
	    /* Discard the Ack. It's getting free for new Ack arriving */
	    mp.freeMessage(msg);  // !!! Problem -> all ack are delete here. No Flowing over all the network possible !!!!!!
	}
        else{
            if(msgType==Reinit_type){
                // If (while testing) the Layer should be reset
                  reinitialize();
                   /* An Acknowledge for the message just arrived is send */
                  int seqNbr=msg.getReliableSeqNbr();
                  long source=msg.getSrcNode();
                  try{
                      sendAck(seqNbr, source);
                  }
                  catch (SendMessageFailedException e) {
                      System.out.println("> # Could not send Acknowledge: " + e.getMessage());
                  }
                  mp.freeMessage(msg);
             }
             else{
                if(msgType==AckMulti_type){
                        super.handleMessage(msg);
                }
                else{  // it's neither an acknowledge nor an Reinitilisation message
                        int seqNbr=msg.getReliableSeqNbr();
                        Integer seqNbrObj=new Integer(seqNbr);
                        long source=msg.getSrcNode();
                        Long sourceObj=new Long(source);
                        boolean freeMessage=false;
                        synchronized(recMessages){
                              if(!alreadyReceived(seqNbrObj, sourceObj)){
                                  /*if(msg.getType()==32)
                                      System.out.println("TextMessage ("+seqNbr+") from " +source+"at "+nodeID+" delivered");*/
                                  super.handleMessage(msg);
                                  /* The message arrives the first time. So it's passed to the upper Layer. */
                                  LinkedList seqList;
                                  if((seqList=(LinkedList)recMessages.get(sourceObj))!=null){
                                    /* If the maximum Size of the vector for this source is reached,
                                       the oldest message is deleted from the Vector before going to
                                       store the new messages Sequence Number.
                                     */
                                      if(seqList.size()>=maxRecMessages)
                                        seqList.remove(0);
                                      seqList.addLast(seqNbrObj);
                                  }
                                  else{
                                      // If there are more nodes in our neighborhood that wanted (given by a parameter from the configFile)
                                      // we control if there are some old entries from neighbors that have gone in meantime.
                                      if(recMessages.size()>=maxNbRec){
                                          NeighborTable neighbors=neighbor.getTable();  // returns a (Vector)NeighborTable object
                                          long [] nbs=neighbors.getNeighborsIds(); // the actual neighbor Ids are returned (stable)
                                          ArrayList neighborid=new ArrayList();
                                          for(int i=0; i<nbs.length; i++){
                                              neighborid.add(new Long(nbs[i]));
                                          }
                                          Set e=recMessages.keySet(); // returns the Source Nodes stored in the hashtable
                                          Iterator i=e.iterator();
                                          Long nextEl;
                                          while(i.hasNext()){
                                             nextEl=(Long)i.next();
                                             // if the NodeID is no more in my neighborhood, its entry in the recMessage Hashtable is
                                             // deleted and so also its appropriate messagesid's.
                                             if(!neighborid.contains(nextEl))
                                                i.remove(); // Remove the actual object from the iteration (-> is also removed from the Hashmap)
                                          }
                                      }
                                      seqList=new LinkedList();
                                      seqList.addLast(seqNbrObj);
                                      recMessages.put(sourceObj, seqList);
                                      }
                              }
                              else{
                                  freeMessage=true;
                                  //System.out.println("Message from node: "+source+" (relseq:"+seqNbr+") already received and continued");
                              }
                        } // end synchronized
                        /* An Acknowledge for the message just arrived is send */
                        try{
                            if(!sourceObj.equals(nodeID))
                              sendAck(seqNbr, source);
                            else{ // It's a message from myself. So I can ack that I've received the message without sending an ack Message
                              synchronized(this){
                                Destinations dest=(Destinations)sentMessages.get(seqNbrObj);
                                if(dest!=null){
                                  dest.updateDest(source); // Delete this node from the Table of awaited acknowledgments.
                                }
                              }
                            }
                        }
                        catch (SendMessageFailedException e) {
                            System.out.println("> # Could not send Acknowledge: " + e.getMessage());
                        }
                        if(freeMessage)
                            mp.freeMessage(msg); // Message has already been delivered to the upper layer
                    } // end of sending ack and processing message
              }
          }
    }

    /**
     * Private Method that creates an acknowledgment message and passes it to the lower layer to
     * be sent on the network.
     */
    private void sendAck (int seqNbr, long oldSource) throws SendMessageFailedException{
	Ack ack=null;
	try{
	    ack=(Ack)mp.getMessage((char)Ack_type);
	    ack.setSeqNbrToAck(seqNbr);
	    ack.setOldSource(oldSource);
	    ack.setDstNode(oldSource);
	    ack.setTTL(ttl);
	}
	catch (Exception e){
	    System.out.println("> Could not create the Acknowledgemessage");
	}
	/*send the Message*/
	sendMessage((Message)ack);
    }

    /* Checks each element of the recMessages HashMap for matches with the message identifiers. If a match is found true is returned */
    private boolean alreadyReceived(Integer seqNbr, Long src){
	LinkedList seq;
	if((seq = (LinkedList)recMessages.get(src))!=null){
	      if(seq.contains(seqNbr))
	         return true;
	}
	return false;
    }


  /** Compares the actual neighbors, searched on the neighboring service, to the
   *  neighbors that haven't yet acknowledged the next message to resend. If some neighbors have gone in the meantime, they
   * are deleted from the list of Neighbors.
   */
    protected synchronized void updateNeighbors(){
      try{
            Integer nextResend=(Integer)orderTimeouts.getFirst();
            Destinations dest=(Destinations)sentMessages.get(nextResend);
            if(dest!=null){
                  /* Update the Neighbor Table */
                  NeighborTable neighbors=neighbor.getTable();  // returns a (Vector)NeighborTable object
                  long [] nbs=neighbors.getNeighborsIds(); // the actual neighbor Ids are returned (stable)
                  ArrayList neighborid=new ArrayList();
                  for(int i=0; i<nbs.length; i++){
                      neighborid.add(new Long(nbs[i]));
                  }
                  Long nextEl;
                  Iterator e=dest.iterator();
                  while(e.hasNext()){
                      nextEl=(Long)e.next();
                      if(!neighborid.contains(nextEl)){
                          /* delete neighbor, it has gone in Meantime..... */
                          if(test){
                            Message msg=dest.getMessage();
                            if(msg.getType()==32){
                              try{
                                fw.write("One Neighbor deleted from my actual Table: NodeID: "+nextEl+"\n");
                                fw.flush();
                              }
                              catch(IOException ex){
                                System.out.println("Cannot write to the file");
                              }
                            }
                          } /* end test */
                          e.remove(); // delete/remove from the actual position of the iteration. -> is removed from the ArrayList
                      }
                  }	/* finished updating the Neighbor Table */
                  /* The Message has been acknowledged by all existing nodes */
                  if(dest.size()!=0){ /* it exists one ore more neighbors that didn't acknowledge */
                    /* testoutputfile */
                     if(test){
                      Message msg=dest.getMessage();
                      if(msg.getType()==32){
                        try{
                          fw.write("One ore more Neighbors didn't send an acknowledge. RelSeqnbr:"+msg.getReliableSeqNbr()+"\n");
                          fw.flush();
                        }
                        catch(IOException ex){
                          System.out.println("Cannot write to the file");
                        }
                        msg=null;
                      }
                    } /* end test */
                 }
              }
           /* if there were deleted all entries while waiting on the timeout there will be thrown a Exception.
             It doesn't matter. All Messages have been acknowledged and needn't to be resent.
          */
          }
          catch (NoSuchElementException ex) {}
    }

    /** Controls if the next Message in the queue is really needed to be resent. If not, it is deleted from the Hashmap.
     */
    protected synchronized boolean isReBroadcastNeeded(){
      try{
        Integer nextResend=(Integer)orderTimeouts.getFirst();
        Destinations dest=(Destinations)sentMessages.get(nextResend);
        if(dest!=null){
          Message msg=dest.getMessage();
          Integer relSeqNbr=new Integer(msg.getReliableSeqNbr());
          if(dest.size()!=0){
            if(dest.getNbr_Resend()<resendTh){
                      /* testoutputfile */
                     if(test){
                      if(msg.getType()==32){
                        try{
                          fw.write("Textmessage need to be REsent. RelSeqnbr: "+msg.getReliableSeqNbr()+"\n");
                          fw.flush();
                        }
                        catch(IOException ex){
                          System.out.println("Cannot write to the file");
                        }
                      }
                    } /* end test */
                     return true;
            }
            else{
                  sentMessages.remove(relSeqNbr);
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
                  mp.freeMessage(msg); // The copie field of the message need to be decremented here, that the message is usable to new messages. It will be accable in the message Pool
                  msg=null;
               } /* end test */
            }
          }
          else {// dest.size()==0
               /*
            if all nodes have ack the message-> no rebroadcast is needed and the entry in the senMessage Table can be deleted.
             */
              sentMessages.remove(relSeqNbr);
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
              mp.freeMessage(msg); // The copie field of the message needs to be decremented here, that the message is usable to new messages. It will be accable in the message Pool.
          } // end else dest.size()=0
        }// end dest!=null
      } // end try
      catch(NoSuchElementException ex)  {}
      return false;
    }

    /** Sends an intern message to the ReliableMultihop Layer to inform it about the new RelSeqNbr.
     *
     */
    private void sendInternMessage(int old, int neu){
        InternMsg intern=null;
	try{
	    intern=(InternMsg)mp.getMessage((char)Intern_type);
	    intern.setOldRelSeqNbr(old);
	    intern.setNewRelSeqNbr(neu);
            intern.setTTL(0);
            intern.setDstNode(nodeID.longValue());
            reliableSeqNbr=((reliableSeqNbr +1 ) & 0xFFFF);
            intern.setSequenceNumber(this.reliableSeqNbr);
            super.handleMessage((Message)intern);
	}
	catch (Exception e){
	    System.out.println("> Could not create the Acknowledgemessage");
	}
    }
}
