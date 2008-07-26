package ch.epfl.lsr.adhoc.services.statistics;

import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;

/**
 * This class is a Layer to send a number of message in broadcast
 *
 *
 */

public class Sender extends AsynchronousLayer {

    //FIELDS
	
	Parameters params;

    /** The type of message to be receive */
    private char messType;
  
    /** A reference to the message pool */
    private MessagePool mp;
  
    /** The counter of messages received */
    private int compteur;  
  
    /** the total number of messages to be sent (configuration file Parameter "nbreMess")*/
    private int nbreMess;  
  
    /** the number of messages per seconds to be send (configuration file Parameter "throughput")*/
    private int throughput;
  
    /** the sleep (configuration file Parameter "sleepDepart" second) */
    int sleepDepart;
 
    /** the Thread who sends the messages */
    private ThreadSender threadEnvoi;
  
    /** the symbol which is shown after the number of messages */
    private char symbol;
  
    /** the thread which writes the results in a file when we end the application */
    private ThreadFinSender threadFin;

    //CONSTRUCTORS
    public Sender(String name, Parameters params){
	super(name, params);
	this.params = params;
    }

    //METHODS
    public void initialize(FrancRuntime runtime) {

	this.mp = runtime.getMessagePool();
	messType = mp.getMessageType ("ThroughputMessage");
	super.setDaemon (false);
	compteur = 0;
	symbol = ' ';
    
	Runtime systemRuntime = Runtime.getRuntime ();
	threadFin = new ThreadFinSender (this);
	// TODO : Ugly way of registering a FrancThread as a shutdown hook.
	systemRuntime.addShutdownHook ((Thread)(Object)threadFin);
 
    
	System.out.println();
	System.out.println ();
    
    
	//recuperation des paramètres
    
	try {
	    throughput = params.getInt("throughput");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the throughtput configuration parameter: " + pdnee.getMessage());
	}
	try {
	    nbreMess = params.getInt("nbreMess");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the number of messages configuration parameter: " + pdnee.getMessage());
	}
	try {
	    sleepDepart = params.getInt("sleepDepart");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the sleep configuration parameter: " + pdnee.getMessage());
	}
	threadEnvoi = new ThreadSender (this,mp,throughput,nbreMess,sleepDepart);
    }

    public void startup() {
	start();
	threadEnvoi.start(); //demarrage du thread qui va envoyer les messages

    }

    /**
     * Inherited comments
     */
    protected void handleMessage(Message msg) {
	long heureArrivee = System.currentTimeMillis();
	char typeMessage = msg.getType ();
	if (typeMessage == messType)
	    {
		compteur++;
		System.out.print("\rNombre de messages recus : "+compteur+symbol);
		mp.freeMessage (msg);				
	    }
	else
	    super.handleMessage (msg);   					    
    }
  
    /** This method is called to get the type of messages this class get */  
    public char getmessType (){
  	return messType;
    }
  
    /** This method is called by the Thread Sender to say that the sending is finish */
    protected void termineEnvoi (){
  	symbol = '*';
    }
  
    /** This method is called by the ThreadFinSender to get the number of messages sent */
    protected int getNbreMessagesEnvoyes (){ return (nbreMess);}
    /** This method is called by the ThreadFinSender to get the throughput */
    protected int getThroughput () { return throughput; }
    /** This method is called by the ThreadFinSender to get the number of messages received */
    protected int getNbreMessagesRecus () { return compteur;}
}

