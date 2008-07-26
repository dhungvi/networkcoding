package ch.epfl.lsr.adhoc.services.statistics;

import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;

/**
 *Cette classe est la classe serveur qui sert a calculer le througput et la latence avec une couche reliable.
 *
 *@see StatisticsLayerclientRel
 *@see ReliableLayer
 */
 
public class StatisticsLayerServeurRel extends AsynchronousLayer {

    //FIELDS
	Parameters params;
    /** The type of message to be receive */
    private char messType;
    /** A reference to the message pool */
    private MessagePool mp;
    /** The number of message received (without ignored)*/
    private int compteur;
    /** The total number of message received (with ignored)*/
    private int compteurTot;
    /** The total number of message to be received (configurtaion parameter "nbreMess")*/
    private int nbreMess;  
    /** The number of message to ignore (configuration parameter "nbreMessIgn")*/
    private int nbreMessIgn;
    /** The hour in millisecond of the reception of the first message receive */
    private long heureDepart;
    /** the hour in millisecond of the reception of the last message receive */
    private long heureFin;
    /** the number of time the experience must be driven */
    private int compteurExp;
    /** the number of time the experience must be driven (configuration parameter "nbreExp")*/
    private int nbreExp;
    /** the time the server must sleep before new experience (configuration parameter "sleepEntreExp" sec )*/
    private long sleepEntreExp;
  
  
    //CONSTRUCTORS
    public StatisticsLayerServeurRel(String name, Parameters params){
	super(name, params);
	this.params = params;
    }

    //METHODS
    public void initialize(FrancRuntime runtime) {
	this.mp = runtime.getMessagePool();
	messType = mp.getMessageType ("ThroughputMessage");
	compteur = 0;
	compteurTot = 0;
	compteurExp = 0;
    
	super.setDaemon (false);
    
	// recuperation des parametres
	try {
	    nbreMessIgn = params.getInt("nMessIgn");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the number of messages to ignore in the configuration parameter: " + pdnee.getMessage());
	}
	try {
	    nbreExp = params.getInt("nbreExp");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the number of experiences in the configuration parameter: " + pdnee.getMessage());
	}
	try {
	    sleepEntreExp = params.getInt("sleep");
	    sleepEntreExp = 1000*sleepEntreExp;
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the sleep time in the configuration parameter: " + pdnee.getMessage());
	}
    }

    public void startup() {
	start();
    }

    /**
     * Inherited comments
     */
    protected void handleMessage(Message msg) {
  	ThroughputMessage messageReponse = null;
  	
  	//on essaye d'obtenir un nouveau message qui servira a repondre
  	try {messageReponse = (ThroughputMessage)mp.getMessage(messType);}
	catch (Exception e){System.out.println ("> # Could not create Message: " +e.getMessage());}
    
	if (msg.getType () == messType) // on controle que c'est le bon type de message
	    {
		if (((ThroughputMessage)msg).getSens() == 0) // On controle qu'il est bien destiné au serveur
		    {
			if (compteurTot >= nbreMessIgn)
			    {
	    		
				if (compteur == 0) 
				    {
					heureDepart = System.currentTimeMillis (); /** à la premiere reception d un message, on prend l'heure */
				    }
	    
				if (((ThroughputMessage)msg).getStop())
				    { 	
	    		
					compteurExp++;	
					double moyenne = (double)compteur*1000.0 /((double)heureFin-(double)heureDepart);
					System.out.println ("Resultats de l'experience "+compteurExp+" sur "+nbreExp+": \n---------Nbre de messages recus"+compteur);
					System.out.println ("througput moyen = "+moyenne);
					messageReponse.setParameters(((ThroughputMessage)msg).getTime(),true,1,moyenne);
	    			
					try {sendMessage (messageReponse);}
					catch (SendMessageFailedException e) {System.out.println ("je n'arrive pas a envoyer "+e.getMessage());} 
	    			
					if (compteurExp < nbreExp)
					    reinitialize ();
					else
					    System.out.println ("\n\nExperience terminee");
				    }	
				else
				    {
	    			
					compteur++;
					heureFin = System.currentTimeMillis (); /* on prend l'heure d'arrivée, c'est peut-etre le dernier message */
					messageReponse.setParameters(((ThroughputMessage)msg).getTime(),false,1,0);
					try {sendMessage (messageReponse);}
					catch (SendMessageFailedException e) {System.out.println ("je n'arrive pas a envoyer "+e.getMessage());} 
				    }
	    		
			    }
			else
			    compteurTot ++;
		    }
		
		mp.freeMessage(msg);
    	
	    }
	else
	    super.handleMessage (msg);
    }
  
    /** this method is called to reinitialize the layer between experiments */
    private void reinitialize ()
    {
  	try{FrancThread.sleep(sleepEntreExp);}catch (Exception e){System.out.println(e.toString());}
  	compteur = 0;
  	compteurTot = 0;
    }
  
  
    
}

