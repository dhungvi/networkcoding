package ch.epfl.lsr.adhoc.services.statistics;

import java.io.*;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;
import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.reliableLayer.*;

/**
 *Cette classe est la classe client qui sert a calculer le througput et la latence avec une couche reliable.
 *
 *@see StatisticsLayerServeurRel
 *@see ReliableLayer
 */

public class StatisticsLayerClientRel extends AsynchronousLayer {

    //FIELDS

	Parameters params;
    /** The type of message to be receive */
    private char messType, messTypeRe;
    /** A reference to the message pool */
    private MessagePool mp;
    /** The couter of messages received */
    private int compteur;  
    /** The number of message send (configuration parameter "nbreMess")*/
    private int nbreMess;  
    /** The number of message to ignore (configuration parameter "nbreMessIgn")*/
    private int nbreMessIgn;  
    /** The RTT (calculated value) */
    private double RTTTotal;
    /** The thread who send messages */
    private ThreadEnvoiRel threadEnvoi;
    /** the number of time the has been driven */
    private int compteurExp;
    /** the number of time the experience must be driven (configuration parameter "nbreExp")*/
    private int nbreExp;
    /** the number of message per seconds must be send  (configuration parameter "throughput")*/
    private int throughput;
    /** the real number of messages per seconds sent */
    private double throughputEnvoi;
    /** the time the client must sleep before new experience (configuration parameter "sleepEntreExp" sec )*/
    private long sleepEntreExp;

    //CONSTRUCTORS
    public StatisticsLayerClientRel(String name, Parameters params){
	super(name, params);
	this.params = params;
    }

    //METHODS
    public void initialize(FrancRuntime runtime) {
      
	this.mp = runtime.getMessagePool();
	messType = mp.getMessageType ("ThroughputMessage");
	messTypeRe = mp.getMessageType ("Reinit");
	super.setDaemon (false);
	compteur = 0;
	compteurExp = 0;
	RTTTotal = 0;
    
    
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
	    nbreExp = params.getInt("nbreExp");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the the number of experiences configuration parameter: " + pdnee.getMessage());
	}
	try {
	    nbreMessIgn = params.getInt("nMessIgn");
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the number of messages to ignore in the configuration parameter: " + pdnee.getMessage());
	}
	try {
	    sleepEntreExp = params.getInt("sleep");
	    sleepEntreExp = 1000*sleepEntreExp;
	} catch(ParamDoesNotExistException pdnee) {
	    throw new RuntimeException("Error reading the sleep time in the configuration parameter: " + pdnee.getMessage());
	}
	threadEnvoi = new ThreadEnvoiRel (this,mp,throughput,nbreMess);
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
	float RTT = 0;
	if (msg.getType () == messType)
	    {
		if (((ThroughputMessage)msg).getSens() == 1)
		    {
			if (((ThroughputMessage)msg).getStop())
			    {
				double throughputMoyen = ((ThroughputMessage)msg).getThroughput();
				double RTTMoyen = RTTTotal/(double)compteur;
				compteurExp++;
				System.out.println();
				System.out.println ("Resultats de l'experience "+compteurExp+" sur "+nbreExp+"......\n-----Nbre Confirmations recues : "+(compteur));
				System.out.println ("-----RTT Moyen : "+RTTMoyen);
				System.out.println ("-----Throughput Moyen : "+throughputMoyen);
				ecrireResultat (RTTMoyen, throughputMoyen);
				if (compteurExp < nbreExp)
				    reinitialize();
				else
				    System.out.println ("\n\nExperience terminee");
			    }
			else
			    {
				long heureDepart = ((ThroughputMessage)msg).getTime();
				RTT = (heureArrivee-heureDepart);
				RTTTotal = RTTTotal + RTT; 
				compteur++;
				if (compteur == (nbreMess-nbreMessIgn))
				    {
					envoyerMessageFin ();
				    }
				/*else
				  {
				  if ((nbreMess-nbreMessIgn-compteur)<50)
				  {
				  System.out.print ("\rNombre de confirmations a recevoir encore : "+(nbreMess-nbreMessIgn-compteur));
				  }
				  }*/
			    }
		    }
		mp.freeMessage (msg);				
	    }
	else
	    super.handleMessage (msg);    					    
    }
  
  
    /** this method is called to reinitialize the layer between experiments */
    private void reinitialize ()
    {
  	System.out.println ("\n\nJe vais envoyer le message de reinitialisation....");
  	envoyerMessageReinitialisation ();
  	try{FrancThread.sleep(sleepEntreExp);}catch (Exception e){System.out.println(e.toString());}
  	System.out.println ("\n\nJe me reinitialise\n\n");
  	compteur = 0;
	RTTTotal = 0;
  	threadEnvoi = new ThreadEnvoiRel (this,mp,throughput,nbreMess);
  	threadEnvoi.start();
    }
  
  
    /** This method is called to get the type of messages this class get */  
    public char getmessType (){
  	return messType;
    }
  
    /** This method is called to write the results in a file
     *
     *@param RTTMoyen : the RTT mesured
     *@param throughputMoyen : the throughput mesured by the server
     */  
    private void ecrireResultat (double RTTMoyen, double throughputMoyen)
    {
  	String fichier = "resultatsExpRel_"+throughput+"_"+nbreMess+"_"+nbreMessIgn+"_"+nbreExp+".csv";
  	
  	FileWriter sauvegardeFichier = null;
	BufferedWriter filtre = null;
	try
	    {
		sauvegardeFichier = new FileWriter(fichier,true);
		filtre = new BufferedWriter(sauvegardeFichier); 
		if (compteurExp == 1)
		    {
			filtre.write ("nombre messages envoyés;nombre de messages ignorés;throughput Théorique;throughput réel d'envoi;;RTT;throughput reception");
			filtre.newLine();
		    }
        
		filtre.write (""+nbreMess);
		filtre.write (";");
		filtre.write (""+nbreMessIgn); 
		filtre.write (";");
		filtre.write (""+throughput);
		filtre.write (";");
		filtre.write (""+throughputEnvoi);
		filtre.write (";");
		filtre.write (";");
		filtre.write (""+RTTMoyen);
		filtre.write (";");
		filtre.write (""+throughputMoyen);
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
  
  
    /** this method is called at the end of an experiments to send a message to the server to get the throughput */
    private void envoyerMessageFin ()
    {
  	ThroughputMessage msgFin = null;
  	try
	    {
  		msgFin = (ThroughputMessage)mp.getMessage(messType);
	  	msgFin.setParameters(System.currentTimeMillis (),true,0,0);
	    }
	catch (Exception e){System.out.println ("> # Could not create Message: " +e.getMessage());}

	try {sendMessage (msgFin);} catch (SendMessageFailedException e) {e.printStackTrace();} 
    }
  
  
    /** this method is called to send the reinitialization message to the layer reliable */
    private void envoyerMessageReinitialisation ()
    {
  	Reinit msgReinit = null;
  	try
	    {
  		msgReinit = (Reinit)mp.getMessage(messTypeRe);
	    }
	catch (Exception e){System.out.println ("> # Could not create Message: " +e.getMessage());}

	try {sendMessage (msgReinit);} catch (SendMessageFailedException e) {e.printStackTrace();} 
    }
  
    /** this method is called by the ThreadSenderRel to set the throughput of sending */
    protected void setThroughputEnvoi (double throughputEnvoi)
    {
  	this.throughputEnvoi = throughputEnvoi;
    }
}

