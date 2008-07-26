package ch.epfl.lsr.adhoc.services.statistics;

import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import java.io.*;

/**
 *Cette classe est la classe client qui sert a calculer le througput et la latence.
 *
 *@see StatisticsLayerServeur
 */

public class StatisticsLayerClient extends AsynchronousLayer {

    //FIELDS

	Parameters params;
    /** The type of message to be receive */
    private char messType;
    /** A reference to the message pool */
    private MessagePool mp;
    /** The couter of messages received */
    private int compteur;  
    /** The number of message to be send (configuration parameter "nbreMess") */
    private int nbreMess;  
    /** The RTT (calculated value) */
    private double RTTTotal;
    /** The thread who send messages */
    private ThreadEnvoi threadEnvoi;
    /** the number of time the experience has been driven */
    private int compteurExp;
    /** the number of time the experience must be driven (configuration parameter "nbreExp")*/
    private int nbreExp;
    /** the number of messages per second must be send (configuration parameter "throughput")*/
    private int throughput;
    /** The number of message to be ignored (configuration parameter "nbreMessIgn")*/
    private int nbreMessIgn;
    /** the real number of messages per seconds sent */
    private double throughputEnvoi;
  

    //CONSTRUCTORS
    public StatisticsLayerClient(String name, Parameters params){
	super(name, params);
	this.params = params;
    }

    //METHODS
    public void initialize(FrancRuntime runtime) {

	this.mp = runtime.getMessagePool();
	messType = mp.getMessageType ("ThroughputMessage");
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
	    throw new RuntimeException("Error reading the the number of messages to ignore configuration parameter: " + pdnee.getMessage());
	}
	threadEnvoi = new ThreadEnvoi (this,mp,throughput,nbreMess);
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
	float RTT = 0;
	if (typeMessage == messType)
	    {
		if (((ThroughputMessage)msg).getSens() == 1)
		    {
			long heureDepart = ((ThroughputMessage)msg).getTime();
			RTT = (heureArrivee-heureDepart);
			RTTTotal = RTTTotal + RTT; 
			compteur++;
			if (((ThroughputMessage)msg).getStop())
			    {
				double RTTMoyen = RTTTotal/(double)compteur;
				compteurExp++;
				System.out.println ("Resultats de l'experience "+compteurExp+" sur "+nbreExp+"......\n-----Nbre Confirmations recues : "+(compteur-1));
				System.out.println ("-----RTT Moyen : "+RTTMoyen);
				ecrireResultat (RTTMoyen);
				if (compteurExp < nbreExp)
				    {
					try{FrancThread.sleep(5000);}catch (Exception e){System.out.println(e.toString());}
					reinitialize();
				    }
				else
				    {	
    				
					System.out.println ("\n\nExperience terminee");
				    }
				
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
  	System.out.println ("\n\nJe me reinitialise\n\n");
  	compteur = 0;
	RTTTotal = 0;
  	threadEnvoi = new ThreadEnvoi (this,mp,throughput,nbreMess);
  	threadEnvoi.start();
    }
  
  
    /** This method is called to get the type of messages this class get */  
    public char getmessType (){
  	return messType;
    }
  
    /** This method is called to write the results in a file
     *
     *@param RTTMoyen : the RTT mesured
     */  
    private void ecrireResultat (double RTTMoyen)
    {
  	String fichier = "resultatsExpClient_"+throughput+"_"+nbreMess+"_"+nbreMessIgn+"_"+nbreExp+".csv";
  	
  	FileWriter sauvegardeFichier = null;
	BufferedWriter filtre = null;
	try
	    {
		sauvegardeFichier = new FileWriter(fichier,true);
		filtre = new BufferedWriter(sauvegardeFichier);
		if (compteurExp == 1)
		    {
			filtre.write ("nombre messages envoyés;nombre de messages ignorés;throughput Théorique;throughput réel d'envoi;;RTT");
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
  
    /** this method is called by the ThreadSenderRel to set the throughput of sending */
    protected void setThroughputEnvoi (double throughputEnvoi)
    {
  	this.throughputEnvoi = throughputEnvoi;
    }
}

