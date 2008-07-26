package ch.epfl.lsr.adhoc.services.statistics;

import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;

/**
 * Cette classe est la classe reliée à la classe Sender qui sert à envoyer le nombre de messages
 * convenus à la cadence voulue.
 *
 *@see Sender
 */

class ThreadSender extends FrancThread
  {
   /** if this variable is true, the thread is active */
   private boolean active;
   /** parameter of the constructor so the thread can use the message pool and access to th type of message */
   private Sender clientLayer;
   
   /** the number of messages per seconds to be send (configuration file Parameter "throughput")*/
   private int frequence;
   
   /** the total number of messages to be sent (configuration file Parameter "nbreMess")*/
   private int nbreMess;
   
   /** the sleep (configuration file Parameter "sleepDepart" second) */
   int sleepDepart;
   
   /** message pool of the ender layer */
   private MessagePool mp;
   
		/** creates a new instance of ThreadEnvoi
		*
		*@param Sender The layer Sender for who this thread works
		*@param MessagePool The Message Pool of the StatisticsLayerClient
		*@param frequence The number of message per second to be sent
		*@param nbreMess The number of messages to be sent
		*@param sleepDepart The time this thread must wait before sending
		*/ 
     public ThreadSender (Sender clientLayer,MessagePool mp, int frequence, int nbreMess, int sleepDepart)
     {
      super();
      this.clientLayer = clientLayer;
      this.frequence = frequence;
      this.mp = mp;
      this.nbreMess = nbreMess;
      this.sleepDepart = sleepDepart;
     }
	 
	 /** This method is called to start the thread */	
     public void run()
     {
     	
     try{FrancThread.sleep((long)(sleepDepart*1000));}catch (Exception e){System.out.println(e.toString());}
      int cpt = 0; 
      active = true;
      ThroughputMessage msg = null;
	  long sleepTime = 0;
	  if (frequence > 0)
	  {
	  	sleepTime = 1000/(long)frequence;
	  }
	  		
      while(cpt < nbreMess && active)
      {
      	
      	 cpt ++;
			 if(sleepTime>0)
	         try{FrancThread.sleep(sleepTime);} // il attend pour envoyer à la bonne frequence
	  	 
	  	 catch (Exception e){System.out.println(e.toString());}
	     
	     try
	  	 {
		  	msg = (ThroughputMessage)mp.getMessage(clientLayer.getmessType());
		  	msg.setParameters(System.currentTimeMillis (),false, 0,0);
		  	
	  	 }
	  	 catch (Exception e){System.out.println ("> # Could not create Message: " +e.getMessage());}
	     
	     
	     try {clientLayer.sendMessage (msg);} catch (SendMessageFailedException e) {e.printStackTrace();}
	     
	     
	  }	
	  clientLayer.termineEnvoi();
  }
		
     /** this method is called to stop the thread */
     public void arreter(){active = false;}
      
      	
   }
