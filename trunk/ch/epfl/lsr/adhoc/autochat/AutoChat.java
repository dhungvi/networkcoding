package ch.epfl.lsr.adhoc.autochat;

import ch.epfl.lsr.adhoc.autochat.DelayedTransmission;
import ch.epfl.lsr.adhoc.chat.TextMessage;
import ch.epfl.lsr.adhoc.runtime.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.Dispatcher;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.ParamDoesNotExistException;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
 * This is a sample application that demonstrates the use of franc.
 * <p>
 * The AutoChat application is implemented as an AsynchronousLayer. It will send
 * automatically simple Text messages to the network and also receive them. Every message
 * received is analyzed, and if the sender address of the message is not already
 * in the list of known nodes, it is added. Upon sending a message, the user
 * can choose a destination from the list of known hosts. There is always at
 * least one entry in this list, namely broadcast, which will send the message
 * to all nodes within range (i.e. reachable with ttl hops).
 * <p>
 * The format of the text that is sent is as follows:<br>
 * &lt;ID&gt;[&lt;counter&gt;]:&lt;message&gt;<br>
 * <br>
 * <ul>
 * <li>ID is an arbitrary text String, that defaults to 'TEST'. If a command-line
 *     argument is given, the first argument is taken as new ID</li>
 * <li>counter is a number, starting at 0 and being incremented with every message
 *     sent</li>
 * <li>message is the message that is written in the text field</li>
 * </ul>
 * <p>
 * In the configuration file, there is one additional parameter for the chat layer,
 * <i>ttl</i>, which permits to set the ttl of messages sent by the chat layer.
 * <p>
 * To quit the application, close the window.
 *
 * @see TextMessage
 * @see CommunicationsLayer
 * @author Urs Hunkeler
 * @version 1.0
 */
public class AutoChat extends AsynchronousLayer implements ActionListener, WindowListener {
  
	private Parameters params;
    /** A reference to FRANC runtime */
    FrancRuntime runtime;
    /** The variable representing the main chat window */
    private Frame frame;
    /** Indicates whether the application runs on a small device (for instance, iPaq) */
    protected static boolean small = false;
    /** The TextArea containing all received messages. */
    private TextArea taChat ;
    /** The TextField where the user can enter a test message. */
    private TextField tfMsg ;
    /** The button used to send the message. Alternatively the user can hit the enter key. */
    private Button btnSend;
    /** The button used to clear the message window. */
    private Button btnClear;
    /** A reference to the asyncronous interface used for the communications. */
    private Dispatcher disp = null;
    /** A reference to the message pool (for creating new messages) */
    private MessagePool mp = null;
    /** Type of service to be used for messages */
    private char msgType;
    /** The time to live for messages sent by chat (parameter in config file) */
    private int ttl;
    /** Vector containing all addresses of known nodes (as java.lang.Long) */
    private Vector addresses = new Vector();
    /** Choice element, which displays the name of all known nodes */
    private Choice adrChoice ;
    /** Boolean telling if the user interface is on */
    private boolean userInt=false;
    /** Logfile definition */
    private PrintWriter logfile;
    /** Used to transmit the next message */
    private DelayedTransmission dt;
    /**
     * ID that is sent with every message.
     * The node name is used to replace the default value.
     */
    private String id;
    /** Counter that counts the messages sent. This value is sent with every message. */
    private int counter = 0;

    /**
     * The constructor creates a new Frame for the Chat application.
     * <p>
     * A new Frame is created, set to an appropriate size and centered on the window.
     * <p>
     * All the network stuff is done in either in the method initialize() or in the
     * method startup().
     */
    public AutoChat(String name, Parameters params) {
		super(name, params);

		this.params = params;
		
    }

    /**
     * Initializes the chat layer.
     * <p>
     * This layer initializes network resources, such as the message pool and the
     * dispatcher. It further obtains the type of messages used by this layer
     * (configuration parameter 'msgType') and the ttl (configuration parameter 'ttl').
     *
     */
    public void initialize(FrancRuntime runtime) {

		this.runtime = runtime;

		// obtain reference to dispatcher
//		try {
			disp = runtime.getDispatcher();
//		} catch(Exception ex) {
//			System.out.println("> # Could not obtain Dispatcher: " + ex.getMessage());
//			throw new RuntimeException(ex.getMessage());
//		}
//		if(disp == null) {
//			throw new RuntimeException("Dispatcher is null");
//		}

		// Obtain reference to MessagePool
		try {
			mp = runtime.getMessagePool();
		} catch(Exception ex) {
			System.out.println("> # Could not obtain MessagePool: " + ex.getMessage());
			throw new RuntimeException(ex.getMessage());
		}
		if(mp == null) {
			throw new RuntimeException("MessagePool is null");
		}

		String msgName = null;
		try {
			msgName = params.getString("msgType");
		} catch(ParamDoesNotExistException pdnee) {
			pdnee.printStackTrace();
			throw new RuntimeException("Could not read configuration parameter 'msgType' " +
									   "for autochat layer: " + pdnee.getMessage());
		}
		if(msgName == null) {
			throw new RuntimeException("Could not read configuration parameter 'msgType' " +
									   "for autochat layer");
		}
		try {
			msgType = mp.getMessageType(msgName);
		} catch(Exception ex) {
			throw new RuntimeException("Message type '" + msgName + "' not found");
		}

		try {
			ttl = params.getInt("ttl");
		} catch(ParamDoesNotExistException pdnee) {
			throw new RuntimeException("Could not read configuration parameter 'ttl' " +
									   "for autochat layer: " + pdnee.getMessage());
		}

		id = runtime.getNodeName();		
		//Open logfile 

		try {
			  File f = new File("log"+id);
			  logfile = new PrintWriter(new FileWriter(f));
			}
			catch (IOException e) { /* Handle exceptions */ }

		int delayMax = 0;
		try {
		   delayMax = params.getInt("delayMax");
		} catch(ParamDoesNotExistException pdnee) {
			throw new RuntimeException("Error reading configuration value delayMax: " + pdnee.getMessage());
		}
		if(delayMax < 0)
		   throw new RuntimeException("Configuration variable delayMax cannot be negative");

		dt = new DelayedTransmission(this, delayMax);


		try {
			String str = params.getString("UserInterface");
			if (str.equals("on")) {
				userInt=true;
			}
		} catch(ParamDoesNotExistException pdnee) {
			throw new RuntimeException("Could not read configuration parameter 'UserInterface' " +
									   "for autochat layer: " + pdnee.getMessage());
		}
		if (userInt) {
		    taChat = new TextArea(20, 20);
		    tfMsg = new TextField(20);
		    btnSend = new Button("send");
		    btnClear = new Button("clear");
		    adrChoice = new Choice();

			// Initialize Frame
			frame = new Frame();
			frame.setLayout(new BorderLayout());
			frame.add(taChat, BorderLayout.CENTER);

			// initialize adrChoice with default (broadcast)
			adrChoice.add("<Broadcast>");
			addresses.addElement(new Long(Message.BROADCAST));
			Panel p = new Panel();
			p.add(adrChoice);
			p.add(tfMsg);
			p.add(btnSend);
			//p.add(btnClear);
			if(small)
				frame.add(p, BorderLayout.NORTH);
			else
				frame.add(p, BorderLayout.SOUTH);

			tfMsg.addActionListener(this);
			btnSend.addActionListener(this);
			btnClear.addActionListener(this);
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			if(small)
				frame.setSize(200, 200);
			else
				frame.setSize(600, 200);
			Dimension df = frame.getSize();
			frame.setLocation(((int)(d.width / 2)) - ((int)(df.width / 2)), ((int)(d.height / 2)) - ((int)(df.height / 2)));
			frame.addWindowListener(this);
			taChat.setEditable(false);
			frame.setTitle("MANET Chat (v2.1): " + id);
		}

    }

    /**
     * Start the functionality of the chat layer.
     * <p>
     * This method shows the chat window and starts the thread.
     * If the application is not running on a small device, then the windows for
     * the neighboring service and the statistics service are also shown.
     */
    public void startup() {
    	if (userInt) {
    		frame.setVisible(true);
     	}
		start();
	    dt.start();
//        while(true){
//        	
//        }
    }

    
    public void autosend() {
    	
        if (counter < 10) {
	    String txt=id + "[" + (counter++) + "]: " + "Hello";
	    send(0, txt);
	    logfile.println("At: "+id+" SEND: "+txt+" at time "+System.currentTimeMillis());
	    System.out.println("At time:"+System.currentTimeMillis()+": NODE ID"+id+" SEND: "+txt );
	}
	    logfile.flush();
    }
    
    /**
     * This method creates a new message and sends it to the lower layer.
     * <p>
     * @param dst  The destination node id (i.e. the node id of the node that should
     *             receive this message)
     * @param text The message text that should be sent
     */
    private void send(long dst, String text) {
		// obtain a message object
		Message msg = null;
		try {
			TextMessage tm = (TextMessage)mp.getMessage(msgType);
			tm.setText(text);
			tm.setDstNode(dst);
			tm.setTTL(ttl);
			msg = tm;
		} catch(Exception ex) {
			System.out.println("> # Could not create Message: " + ex.getMessage());
		}

		// send message through the AsynchronousLayer's default method
		try {
			sendMessage(msg);
		} catch(Exception ex) {
			System.out.println("> # Could not send message: " + ex.getMessage());
		}
    }

    /**
     * This method is called from the super class (AsynchronousLayer) when a new
     * new message was received.
     */
    public void handleMessage(Message msg) {
		if(msg == null) {
			System.out.println("> # message is null");
		} else if(msg.getType() == msgType) {
			String text = ((TextMessage)msg).getText();
			logfile.println("At time:"+System.currentTimeMillis()+": NODE ID"+id+" RECEIVED: "+text);
			System.out.println("At: "+id+" RECEIVED: "+text+" at time "+System.currentTimeMillis());
			if (userInt) {
				if(small)
					taChat.insert(text + "\n", 0);
				else
					taChat.append(text + "\n");
			}
			Long adr = new Long(msg.getSrcNode());
			if(!addresses.contains(adr)) {
				int i = text.indexOf("[");
				if(i > 0) {
					addresses.addElement(adr);
					if (userInt) {
						adrChoice.add(text.substring(0, i));
						adrChoice.validate();
					}
				}
			}
		} else {
			System.out.println("> Unknown message type: " + ((int)msg.getType()));
		}
		mp.freeMessage(msg);
    }

    /**
     * ActionListener that is used to send messages.
     * <p>
     * This method is called when the user clicks the send button or hits the enter
     * key.
     */
    public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();
		if(src == btnSend || src == tfMsg) {
			String msg = tfMsg.getText();
			long dst = ((Long)addresses.elementAt(adrChoice.getSelectedIndex())).longValue();
			send(dst, id + "[" + (counter++) + "]: " + msg);
			tfMsg.setText("");
		} else if(src == btnClear) {
			taChat.setText("");
		}
    }

    /**
     * This method terminates the application.
     * <p>
     * To add a dialog for exiting the application, or to save options upon exit,
     * one could change the implementation of this method.
     */
    private void exit() {
		logfile.close();
    	System.exit(0);
    }

    /**
     * This method is called when the application window receives the focus. It
     * puts the focus on the text field (so that a user can directly start to
     * enter text).
     */
    public void windowActivated(WindowEvent we) {
		/*tfMsg.requestFocus();
		  if(!small) {
		  // this code is incompatible with jdk1.1.6, but should not be called on an iPaq
		  Font font = taChat.getFont();
		  font = font.deriveFont((float)24);
		  taChat.setFont(font);
		  }*/
    }

    /** We need to implement this method for the WindowListener interface, but it's not used */
    public void windowClosed(WindowEvent we) {}
    /**
     * This method is called when the user tries to close the window (via
     * system menu). It just terminates the application.
     */
    public void windowClosing(WindowEvent we) {
		exit();
    }

    /** We need to implement this method for the WindowListener interface, but it's not used */
    public void windowDeactivated(WindowEvent we) {}
    /** We need to implement this method for the WindowListener interface, but it's not used */
    public void windowDeiconified(WindowEvent we) {}
    /** We need to implement this method for the WindowListener interface, but it's not used */
    public void windowIconified(WindowEvent we) {}
    /** We need to implement this method for the WindowListener interface, but it's not used */
    public void windowOpened(WindowEvent we) {}
}

