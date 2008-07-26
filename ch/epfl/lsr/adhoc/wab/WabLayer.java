package ch.epfl.lsr.adhoc.wab;

import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Dispatcher;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.SendMessageFailedException;

import java.io.*;

public class WabLayer extends AsynchronousLayer {
  
	private Parameters params;
    private Dispatcher disp = null;
    private MessagePool mp = null;
    private char msgType;
    private String id;  
    private FrancThread sender;
    private int nb;
    private int nbMessages;
    private boolean master = false;

    public WabLayer(String name, Parameters params) {
		super(name,params);
		this.params = params;
		nb=0;
		sender = new SenderThread();
    }

    public void initialize(FrancRuntime runtime) {
		disp = runtime.getDispatcher();
		mp = runtime.getMessagePool();
		String msgName = null;
		try {
			msgName = params.getString("msgType");
			nbMessages = params.getInt("nb_messages");	
		}
		catch(ParamDoesNotExistException pdnee) {pdnee.printStackTrace();}
		msgType = mp.getMessageType(msgName);
		id = runtime.getNodeName();
		master = askIfMaster();
		setDaemon(false);
    }

    public void startup() {
		start();
		if(master) sender.start();
    }
	
    boolean askIfMaster() {
		try {
			System.out.print("Will this host be the WAB master (y/n) ? ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String answer = reader.readLine();
			return (answer.compareToIgnoreCase("y")==0);
		}
		catch(Exception e) {e.printStackTrace();}
		return false;
    }
	
    public void operate() {
		if(master) {
			try {FrancThread.sleep((long)5000);}
			catch(InterruptedException ie) {}
		}
		System.out.println("Sending ...");
		for(int i=0;i<nbMessages;i++) {
			WabMessage msg = (WabMessage)mp.getMessage(msgType);
			msg.setDstNode(0);		
			try {sendMessage(msg);}
			catch(SendMessageFailedException smfe) {smfe.printStackTrace();}
			/*try {FrancThread.sleep(Math.round(Math.random()*50.0));}
			  catch(InterruptedException ie) {}*/
		}
		System.out.println("Finished");
		try {FrancThread.sleep((long)15000);}	
		catch(InterruptedException ie) {}	
		System.out.println(nb+" messages received");
    }

    public void handleMessage(Message msg) {
		nb++;
		if(nb==1 && !master) sender.start();
		mp.freeMessage(msg);
    }
  
    class SenderThread extends FrancThread {
		public SenderThread() {}
		
		public void run() {
			operate();
		}
    }
}
