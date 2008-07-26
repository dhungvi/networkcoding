package ch.epfl.lsr.adhoc.services.statistics;

import java.io.*;
import ch.epfl.lsr.adhoc.runtime.FrancThread;

/**
 * Cette classe est un thread relié à la classe Sender qui sert à enregistrer le nombre de messages recus dans un
 * fichier lorsque l'on quitte le programme
 *
 *@see Sender
 */

class ThreadFinSender extends FrancThread
  {
   /** if this variable is true, the thread is active */
   private boolean active;
   /** parameter of the constructor so the thread can get the parameters and the number of messages */
   private Sender sender;
   
   
	/** creates a new instance of ThreadFinSender
	*
	*@param Sender : the sender who send the message
	*/ 
     public ThreadFinSender (Sender sender)
     {
      super();
      this.sender = sender;
     }
	 
	 /** This method is called to start the thread */	
     public void run()
     {
	     int nbreMessagesRecus = sender.getNbreMessagesRecus();
	     int nbreMessagesEnvoyes = sender.getNbreMessagesEnvoyes ();
	     int throughput = sender.getThroughput ();
	     String nomFichier = ecrireResultat (nbreMessagesRecus, nbreMessagesEnvoyes ,throughput);
	     
	     System.out.println ();
	     System.out.println ("les resultats ont ete sauvegardes dans le fichier : "+nomFichier);
  	 }
		
     /** This method is called to write the results in a file 
     *
     *@param nbreMessagesRecus : (int) the number of messages received
     *@param nbreMessagesEnvoyes : (int) the number of messages send
     *@param throughput : (int) the throughput
     */
     private String ecrireResultat (int nbreMessagesRecus, int nbreMessagesEnvoyes , int throughput)
  	 {
  		String fichier = "resultatsExp_"+throughput+"_"+nbreMessagesEnvoyes+".csv";
  	
  		FileWriter sauvegardeFichier = null;
    	BufferedWriter filtre = null;
    	try
    	{
    		sauvegardeFichier = new FileWriter(fichier,true);
        	filtre = new BufferedWriter(sauvegardeFichier); 
			
			filtre.write ("throughput;nombre de messages envoyés;nombre de messages recus");
        	
     		filtre.newLine ();        
        	filtre.write (""+throughput);
        	filtre.write (";");
        	filtre.write (""+nbreMessagesEnvoyes); 
        	filtre.write (";");
        	filtre.write (""+nbreMessagesRecus);
     		filtre.newLine ();
    	}
       
    	catch (IOException e)
    	{
       		System.out.println ("Impossible de stocker les resultats :");
       		e.printStackTrace();
       		fichier = "";
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
       	return (fichier);
    }
      
      	
   }
