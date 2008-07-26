package ch.epfl.lsr.adhoc.runtime;

import java.util.*;
import org.w3c.dom.*;

/**   
 * TODO : Comments
 * author: David Cavin
 * @version 1.0
 */


public final class FrancRuntime implements RuntimeInterface {
    
    private Config config;
    private AsynchronousLayer layers[];
    private Service services[];
    // Ajout : Kave Salamatian, Needed to know if we are in the simulation mode or not !
    private boolean simulMode=false; 
    
    protected FrancRuntime(Config config,
						   AsynchronousLayer layers[],
						   Service services[]) {
		System.out.println("\nFRANC runtime is starting up");
		this.config = config;
		this.layers = layers;
		this.services = services;
		createStack();
    }

    public void initialize() {

		// Layers intialization
		System.out.println("\nProtocol stack ("+layers.length+" layers)");
		for(int i = 0; i < layers.length; i++) {
			System.out.println("-> Initializing "+layers[i]);
			layers[i].initialize(this);
		}

		// Services intialization
		System.out.println("\nServices ("+services.length+" service"+(services.length>1?"s":"")+")");
		for(int i = 0; i < services.length; i++) {
			System.out.println("-> Initializing service "+services[i].getClass().getName());
			services[i].initialize(this);
		}
	
		// test for daemon threads (used if there is no application, such as a router)
		/*try {
		  String daemon="false";
		  try {
		  daemon = getLayerParameter("AsyncMulticast","daemon");}
		  //catch(ParamDoesNotExistException e) {e.printStackTrace();}
		  if(daemon != null) {
		  if(daemon.equalsIgnoreCase("true")) {
		  layers[layers.length - 1].setDaemon(true);
		  } else if(daemon.equalsIgnoreCase("false")) {
		  layers[layers.length - 1].setDaemon(false);
		  } else {
		  throw new RuntimeException("Expected 'true' or 'false'");
		  }
	}
} catch(Exception ex) {
	ex.printStackTrace();
	throw new RuntimeException("Error reading config parameter 'daemon': " + ex.getMessage());
}*/

		System.out.println("\n--> My unique node ID is: " + getNodeID());
		System.out.println("--> My node name is: " + getNodeName());	
    }

    public void startup() {
		System.out.println("\nFRANC is started\n");
		for(int i = 0; i < layers.length; i++) {
			layers[i].startup();
		} 
	
		for(int i = 0; i < services.length; i++) {
			services[i].startup();
		}
    }
	

	void createStack() {
		for(int i = 1; i < layers.length; i++) {
			layers[i].setLowerLayer(layers[i - 1]);
		}
	}

    public Dispatcher getDispatcher() {
		return config.getDispatcher();
    }

    public MessagePool getMessagePool() {
		return config.getMessagePool();
    }
    
    public long getNodeID() {
		return config.getNodeID();
    }

    public String getNodeName() {
		return config.getNodeName();
    }
    
    public Service getService(String name) {
		return config.getDispatcher().getService(name);
	}
    public void setSimulMode() {
		simulMode=true;
	}
    public boolean isSimulMode() {
		return simulMode;
	}   
}
