package ch.epfl.lsr.adhoc.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.InetAddress;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ch.epfl.lsr.adhoc.tools.ParserXml;

public class Config {

    private long nodeID=-1;
    private String nodeName="uninitialized";
    private MessagePool messagePool;
    private Dispatcher dispatcher;

    private ArrayList layers;
    private ArrayList services;

	private File configFile;
	private boolean configLoaded;

    //TODO : remove franc global variable 
  protected  Element franc;

	protected FrancRuntime createRuntime() {
		if(!configLoaded)
			throw new RuntimeException("Configuration not loaded from \""+configFile.getName()+"\". "+
									   "Call \"loadConfig()\" first.");
		return new FrancRuntime(this,
								(AsynchronousLayer[])layers.toArray(new AsynchronousLayer[0]),
								(Service[])services.toArray(new Service[0]));
    }
	
    public Config(String configFileName) {
		configLoaded = false;
		configFile = new File(configFileName);
		if(!configFile.exists() ||
		   !configFile.isFile() ||
		   !configFile.canRead()) {
			throw new RuntimeException("Config file \""+
									   configFileName+
									   "\" either does not exist or is not a regular file or is not readable");
		}
    }

    protected void loadConfig() {
		System.out.println("\nParsing "+configFile.getName());
		Document document = parseXMLFile(configFile);
		readParameters(document);
		configLoaded = true;
    }

	
    // XML parsing
	
    private Document parseXMLFile(File configFile) {
		ParserXml parser = new ParserXml();
		Document document = parser.parse(configFile);
		return document;
    }

    // Extraction of the paremeters from XML config file

    private void readParameters(Document document) {
		
		franc = document.getDocumentElement();
		nodeID = computeNodeID();
		nodeName = franc.getAttributeNode("name").getNodeValue();
		if(nodeName.compareTo("net")==0)
			nodeName = getIPHostName();
		if(nodeName==null || nodeName.compareTo("auto")==0)
			nodeName = "Node_"+nodeID;
		initializeMessagePool();
		layers = new ArrayList();
		layers.add(createDataLinkLayer());
		AsynchronousLayer virtualNetworksLayer = createVirtualNetworksLayer();
		if(virtualNetworksLayer != null) layers.add(virtualNetworksLayer);
		AsynchronousLayer ipGatewayLayer  = createIPGatewayLayer();
		if(ipGatewayLayer != null) layers.add(ipGatewayLayer);
		layers.add(createRoutingLayer());
		AsynchronousLayer[] regularLayers1 = 
			createLayers(franc.getElementsByTagName(ParserXml.ROUTING).item(0)); 
		layers.addAll(Arrays.asList(regularLayers1));
		
		dispatcher = createDispatcher();
		services = new ArrayList();
		if(dispatcher != null) {
			layers.add(dispatcher);
			AsynchronousLayer[] regularLayers2 = 
				createLayers(franc.getElementsByTagName(ParserXml.DISPATCHER).item(0));
			layers.addAll(Arrays.asList(regularLayers2));
			createServices();
		}
	}

	protected AsynchronousLayer createDataLinkLayer() {
		Node node = franc.getElementsByTagName(ParserXml.DATA_LINK_LAYER).item(0);
		Parameters params = getParameters(node);
		String layerClass = nodeToElement(node).getElementsByTagName(ParserXml.CLASS).item(0).getFirstChild().getNodeValue();
		AsynchronousLayer layer = getAsynchronousLayerForName(layerClass,ParserXml.DATA_LINK_LAYER,params);
		return layer;
	}

	protected AsynchronousLayer createVirtualNetworksLayer() {
		Node node = franc.getElementsByTagName(ParserXml.VIRTUAL_NETWORKS).item(0);
		if(node == null) return null;
		Parameters params = getParameters(node);
		String layerClass = nodeToElement(node).getElementsByTagName(ParserXml.CLASS).item(0).getFirstChild().getNodeValue();
		AsynchronousLayer layer = getAsynchronousLayerForName(layerClass,ParserXml.VIRTUAL_NETWORKS,params);
		return layer;
	}

	protected AsynchronousLayer createIPGatewayLayer() {
		Node node = franc.getElementsByTagName(ParserXml.IPGATEWAY).item(0);
		if(node == null) return null;
		Parameters params = getParameters(node);
		String layerClass = nodeToElement(node).getElementsByTagName(ParserXml.CLASS).item(0).getFirstChild().getNodeValue();
		AsynchronousLayer layer = getAsynchronousLayerForName(layerClass,ParserXml.IPGATEWAY,params);
		return layer;
	}

	protected AsynchronousLayer createRoutingLayer() {
		Node node = franc.getElementsByTagName(ParserXml.ROUTING).item(0);
		Parameters params = getParameters(node);
		String layerClass = nodeToElement(node).getElementsByTagName(ParserXml.CLASS).item(0).getFirstChild().getNodeValue();
		AsynchronousLayer layer = getAsynchronousLayerForName(layerClass,ParserXml.ROUTING,params);
		return layer;
	}

	protected AsynchronousLayer[] createLayers(Node from) {
		Node current = from;
		ArrayList list = new ArrayList();
		while(true) {
			current = current.getNextSibling();
			if(current == null) break;
			else if(current.getNodeType() == Node.COMMENT_NODE)
				continue;
			else if (current.getNodeType() == Node.ELEMENT_NODE &&
					 current.getNodeName().compareTo(ParserXml.LAYER) == 0) {	  
				String layerClass = nodeToElement(current).getElementsByTagName(ParserXml.CLASS).item(0).getFirstChild().getNodeValue();
				Parameters params = getParameters(current);
				list.add(getAsynchronousLayerForName(layerClass,ParserXml.LAYER,params));
			} else
				break;		   
		}
		return (AsynchronousLayer[])list.toArray(new AsynchronousLayer[0]);
	}

	private Dispatcher createDispatcher() {
		Node node = franc.getElementsByTagName(ParserXml.DISPATCHER).item(0);
		if(node == null) return null;
		Parameters params = getParameters(node);
		String layerClass = nodeToElement(node).getElementsByTagName(ParserXml.CLASS).item(0).getFirstChild().getNodeValue();
		AsynchronousLayer layer = getAsynchronousLayerForName(layerClass,ParserXml.DISPATCHER,params);
		return (Dispatcher)layer;
	}

	protected Parameters getParameters(Node node) {
		Parameters parameters = new Parameters();
		NodeList params  = nodeToElement(node).getElementsByTagName(ParserXml.PARAM);
		for(int i = 0; i < params.getLength(); i++) {
			Node param = params.item(i);
			String name = nodeToElement(param).getAttributeNode(ParserXml.NAME).getNodeValue();
			String value = param.getFirstChild().getNodeValue();
			parameters.addParameter(name,value);
		}
		return parameters;
	}
	
	//Messages
	private void initializeMessagePool() {

		NodeList messages = franc.getElementsByTagName(ParserXml.MESSAGE);
		int numMessages = messages.getLength();
		messagePool = new MessagePool();
	
		for (int i=0; i < numMessages; i++) {
			Node message = messages.item(i); 
			String messageType = nodeToElement(message).getElementsByTagName(ParserXml.TYPE).item(0).getFirstChild().getNodeValue();
			int msgType = Integer.parseInt(messageType);
			String messageName = nodeToElement(message).getElementsByTagName(ParserXml.NAME).item(0).getFirstChild().getNodeValue();
			String messageClass = nodeToElement(message).getElementsByTagName(ParserXml.CLASS).item(0).getFirstChild().getNodeValue();
			Node messageFactory  = nodeToElement(message).getElementsByTagName(ParserXml.MESSAGE_FACTORY).item(0);
			String factoryName = nodeToElement(messageFactory).getElementsByTagName(ParserXml.NAME).item(0).getFirstChild().getNodeValue();
			String factoryClass = nodeToElement(messageFactory).getElementsByTagName(ParserXml.CLASS).item(0).getFirstChild().getNodeValue();
			System.out.println("-> Adding new message type: " + msgType + " - " + messageName + " ("+factoryName+")");
			messagePool.addMessageFactory((char)msgType,getMessageFactoryForName(factoryClass),messageName);
		}
	}


	// Services
	private void createServices() {
		NodeList listMod = franc.getElementsByTagName(ParserXml.SERVICE);
		int numServices = listMod.getLength();
		String modMsgType = null;
		for(int i = 0; i < numServices; i++) {
			Node serviceNode = listMod.item(i);
			NodeList serviceChild = serviceNode.getChildNodes();
			String serviceName = serviceChild.item(0).getFirstChild().getNodeValue();	   
			String serviceClass = serviceChild.item(1).getFirstChild().getNodeValue();		 
			Parameters params = getParameters(serviceNode);
			Service service = getServiceForName(serviceClass,params);
			try {modMsgType = params.getString(ParserXml.MSG_TYPE);}
			catch(ParamDoesNotExistException pdnee) {
				throw new RuntimeException("Unable to create service \""+serviceName+"\". Required parameter \""+ParserXml.MSG_TYPE+"\" is wrong.");
			}
			char msgType = messagePool.getMessageType(modMsgType);							
			dispatcher.register(msgType, service,serviceName);									
			services.add(service);
		}

    }

    protected String getNodeName() {
		return nodeName;
    }

    protected Dispatcher getDispatcher() {
		return dispatcher;
    }

    protected MessagePool getMessagePool() {
		return messagePool;
    }

    /**
     * return an element node if the Node node is an element
     */
    private Element nodeToElement(Node node){
	
		if (node.getNodeType() == Node.ELEMENT_NODE) return (Element)node;
	
		return null;
    }

    IMessageFactory getMessageFactoryForName(String className) {
		try {
			return (IMessageFactory)Class.forName(className).newInstance();
		}
		catch(ClassNotFoundException cnfe) {
			throw new RuntimeException("Class not found : "+cnfe.getMessage());
		}
		catch(InstantiationException ie) {
			throw new RuntimeException("Instantiation exception : "+ie.getMessage());
		}
		catch(IllegalAccessException iae) {
			throw new RuntimeException("Illegal access : "+iae.getMessage());		
		}
    }
    
    AsynchronousLayer getAsynchronousLayerForName(String className, String name) {
		try {
			return (AsynchronousLayer)Class.forName(className).getConstructor(new Class[]{name.getClass()}).newInstance(new Object[]{name});
		}
		catch(ClassNotFoundException cnfe) {
			throw new RuntimeException("Class not found : "+cnfe.getMessage());
		}
		catch(InstantiationException ie) {
			throw new RuntimeException("Instantiation exception : "+ie.getMessage());
		}
		catch(IllegalAccessException iae) {
			throw new RuntimeException("Illegal access : "+iae.getMessage());		
		}
		catch(NoSuchMethodException nsme) {
			throw new RuntimeException("No valid constuctor found for class "+className,nsme);
		}
		catch(java.lang.reflect.InvocationTargetException ite) {
			throw new RuntimeException("Unable to create "+className+" layer",ite);
		}
    }

    AsynchronousLayer getAsynchronousLayerForName(String className, String name, Parameters params) {
		try {
			return (AsynchronousLayer)Class.forName(className).getConstructor(new Class[]{name.getClass(),params.getClass()}).newInstance(new Object[]{name,params});
		}
		catch(ClassNotFoundException cnfe) {
			throw new RuntimeException("Class not found : "+cnfe.getMessage());
		}
		catch(InstantiationException ie) {
			throw new RuntimeException("Instantiation exception : "+ie.getMessage());
		}
		catch(IllegalAccessException iae) {
			throw new RuntimeException("Illegal access : "+iae.getMessage());		
		}
		catch(NoSuchMethodException nsme) {
			throw new RuntimeException("No valid constuctor found for class "+className,nsme);
		}
		catch(java.lang.reflect.InvocationTargetException ite) {
			throw new RuntimeException("Unable to create "+className+" layer",ite);
		}
    }
    

    Service getServiceForName(String className, Parameters params) {
		try {
			return (Service)Class.forName(className).getConstructor(new Class[]{params.getClass()}).newInstance(new Object[]{params});
		}
		catch(ClassNotFoundException cnfe) {
			throw new RuntimeException("Class not found : "+cnfe.getMessage());
		}
		catch(InstantiationException ie) {
			throw new RuntimeException("Instantiation exception : "+ie.getMessage());
		}
		catch(IllegalAccessException iae) {
			throw new RuntimeException("Illegal access : "+iae.getMessage());		
		}
		catch(NoSuchMethodException nsme) {
			throw new RuntimeException("No valid constuctor found for class "+className,nsme);
		}
		catch(java.lang.reflect.InvocationTargetException ite) {
			throw new RuntimeException("Unable to create "+className+" layer",ite);
		}
    }

    protected long getNodeID() {
		return nodeID;
    }

	String getIPHostName() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch(IOException ioe) {return null;}
	}
    
    long computeNodeID() {
		byte[] adr = null;
		try {
			adr = InetAddress.getLocalHost().getAddress();
		} catch(Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
		return    (System.currentTimeMillis() & 0xFFFFFFFF) +
			((adr[0] & 0xFF) << 56) +
			((adr[1] & 0xFF) << 48) +
			((adr[2] & 0xFF) << 40) +
			((adr[3] & 0xFF) << 32);
    }
}
