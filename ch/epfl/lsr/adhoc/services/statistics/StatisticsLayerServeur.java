package ch.epfl.lsr.adhoc.services.statistics;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import java.io.*;

/**
 * Cette classe est la classe serveur qui sert a calculer le througput et la latence.
 *
 *@see StatisticsLayerClient
 */
 
public class StatisticsLayerServeur extends AsynchronousLayer {

    //FIELDS
	private Parameters params;
    /** The type of message to be receive */
    private char messType;
    /** A reference to the message pool */
    private MessagePool mp;
    /** The number of message received (without ignored)*/
    private int compteur;
    /** The total number of message received (with ignored)*/
    private int compteurTot;
    /** The number of message to be ignored (configuration parameter "nbreMessIgn")*/
    private int nbreMessIgn;
    /** The hour in millisecond of the reception of the first message receive */
    private long heureDepart;
    /** the hour in millisecond of the reception of the last message receive */
    private long heureFin;
    /** the number of time the experience has been driven */
    private int compteurExp;
    /** the number of time the experience must be driven (configuration parameter "nbreExp")*/
    private int nbreExp;

    //CONSTRUCTORS
    public StatisticsLayerServeur(String name, Parameters params){
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
    
	// recueration des parametres
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
					heureDepart = System.currentTimeMillis (); // à la premiere reception d un message, on prend l'heure
				    }
	    
				if (((ThroughputMessage)msg).getStop())
				    { 	
	    		
					compteurExp++;	
					double moyenne = (double)compteur*1000.0 /((double)heureFin-(double)heureDepart);
					System.out.println ("Resultats de l'experience "+compteurExp+" sur "+nbreExp+": \n---------Nbre de messages recus"+compteur);
					System.out.println ("througput moyen = "+moyenne);
					messageReponse.setParameters(((ThroughputMessage)msg).getTime(),true,1,0);
					ecrireResultat (moyenne);
					if (compteurExp < nbreExp)
					    {
						reinitialize ();
					    }
					else 
					    {
	    				
						System.out.println ("\n\nExperience terminee");
					    }	
				    }	
				else
				    {
	    			
					compteur++;
					heureFin = System.currentTimeMillis (); // on prend l'heure d'arrivée, c'est peut-etre le dernier message
					messageReponse.setParameters(((ThroughputMessage)msg).getTime(),false,1,0);
				    }
	    	
				try {sendMessage (messageReponse);
				}
				catch (SendMessageFailedException e) {
				    System.out.println ("je n'arrive pas a envoyer "+e.getMessage());} 
	    		
			    }
			else
			    compteurTot ++;
		    }
		mp.freeMessage(msg);
	    }
	else
	    super.handleMessage (msg);
    
	// mp.freeMessage(msg);
    				
    					    
    }
  
    /** This method is called to write the results in a file
     *
     *@param througuput : the Throughput mesured
     */  
    private void ecrireResultat (double throughput)
    {
  	String fichier = "resultatsExpServer_"+"_"+nbreMessIgn+"_"+nbreExp+".csv";
  	
  	FileWriter sauvegardeFichier = null;
	BufferedWriter filtre = null;
	try
	    {
		sauvegardeFichier = new FileWriter(fichier,true);
		filtre = new BufferedWriter(sauvegardeFichier);
		if (compteurExp == 1)
		    {
			filtre.write ("nombre messages ignorés;throughput réel de réception");
			filtre.newLine();
		    } 
        
		filtre.write (""+nbreMessIgn); 
		filtre.write (";");
		filtre.write (""+throughput);
		filtre.newLine ();
	    }
       
	catch (IOException e)
	    {
		System.out.println ("Impossible de stocker les resultats :");
		e.printStackTrace();
	    }
	finally
	    {
		try
		    {
			if(filtre!=null)
			    filtre.close();
			if (sauvegardeFichier!=null)
			    sauvegardeFichier.close();
		    }
		catch(IOException e)
		    {
			System.out.println ("probleme fichier");
			e.printStackTrace();
		    }
	    }
    
	System.out.println ("Resultats sauvegardes dans le fichier : "+fichier);
    }
  
    /** this method is called to reinitialize the layer between experiments */
    private void reinitialize ()
    {
  	compteur = 0;
  	compteurTot = 0;
    }
    
}

