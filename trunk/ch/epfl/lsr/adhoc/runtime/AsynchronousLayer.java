package ch.epfl.lsr.adhoc.runtime;

/**
 * The base class that represents any asynchronous layer in the communication
 * layer hierarchie.
 * <p>
 * @author Urs Hunkeler
 * @version 1.0
 */
public abstract class AsynchronousLayer extends FrancThread {
    /** This boolean variable indicates if the thread is running*/
    private boolean running = true;
    /** The Buffer object used to store messages*/
    private Buffer buffer;
    /** The AsychronousLayer object representing the lower AsychronousLayer of this layer*/
    private AsynchronousLayer  lowerLayer;
    /** Layer's name read from XML config file */
    private String name;

    /**
     * Default Constructor.
     */
    public AsynchronousLayer(String name, Parameters params) {
		this.name = name;
		this.buffer = new Buffer();
		setDaemon(false);		
	}

    /**
     * This method is called to initialize the layer.
     *
     */
    public abstract void initialize(FrancRuntime runtime);

    /**
     * This method is called to start the layer.
     * <p>
     * This method will usually just start the main thread for the layer.
     */
    public abstract void startup();

    /**
     * This method is used to specify the next lower layer in the layer hierarchie.
     * <p>
     * The lower layer is used to obtain messages from and to send messages to.
     * <p>
     * @param lowerLayer The layer that is just below this layer in the layer
     *        hierarchie.
     */
    public final void setLowerLayer(AsynchronousLayer lowerLayer) {
		if(this.lowerLayer != null) {
			throw new RuntimeException("LowerLayer can only be specified once!");
		}
		this.lowerLayer = lowerLayer;
    }

    /**
     * The run method that implements the thread.
     * <p>
     * This simple thread just runs an infinite loop and waits for new messages.
     * When a new message is found, the method handleMessage() is called, which
     * will then decide, what to do with the message.
     */
    public final void run() {
		while(running) {
			try {
				Message msg = getNetworkMessage();
				if(msg != null) // if msg == null then probably the message type is unknown (see standard output for errors)
					handleMessage(msg);
			}
			catch(Exception e) {e.printStackTrace();}
		}
    }

    /**
     * The halt method to stop the thread.
     * <p>
     * This method can be used to stop this thread.
     */
    public final synchronized void halt() {
		running = false;
    }

    /**
     * This method is used to obtain the next message in the buffer.
     * <p>
     * If no message is available, this method will block.
     * Otherwise it will return the message object.
     * <p>
     * @return The next message in the buffer
     */
    public final Message getMessage() {
		Message msg = (Message)buffer.remove();
		return msg;
    }

    /**
     * When a new message arrives, this method decides what to do with it.
     * <p>
     * The default implementation is to put the message into the buffer. But
     * a subclass might want to override this default behaviour.
     * <p>
     * @param msg The message to handle
     */
    protected void handleMessage(Message msg) {
		buffer.add(msg);
    }

    /**
     * This method is used to send a Message.
     * <p>
     * The default behaviour is to forward the message to the next lower layer.
     * Obviously the lowest layer (network layer) has to override this method and
     * send the message to the network. But other subclasses might also want to
     * override this method.
     * <p>
     * @param msg The message object to be send
     * @throws IllegalArgumentException
     * @throws SendMessageFailedException
     */
    public synchronized int sendMessage(Message msg) throws SendMessageFailedException {
		return lowerLayer.sendMessage(msg);
    }

    /**
     * Returns the next message.
     * <p>
     * This method is called by the thread's run method to obtain the next message
     * to handle. This method should block if there is no message available.
     * <p>
     * The default behaviour is to return the next message from the lower layer.
     * Obviously the lowest layer (network layer) has to override this method and
     * return instead a message received on the network. But other subclasses might
     * also want to override this method.
     * <p>
     * @return The next message
     */
    protected Message getNetworkMessage() {
		return lowerLayer.getMessage();
    }

    /**
     * Indicates wether messages are waiting.
     * <p>
     * This method  will return true if at least one message is waiting,
     * and false in all other cases (no exceptions, no blocking).
     * <p>
     * @return A boolean value indicating if there are messages waiting (true)
     *         or not (false)
     */
    public final boolean messagesAvailable() {
		return !buffer.isEmpty();
    }

    public String toString() {
		return name+" ("+getClass().getName()+")";
    }
}
