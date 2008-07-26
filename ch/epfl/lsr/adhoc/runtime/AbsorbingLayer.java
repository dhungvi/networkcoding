package ch.epfl.lsr.adhoc.runtime;

/**
 * This layer discards any messages arriving.
 * <p>
 * This layer should be the very last layer in any layer hierarchie. It discards
 * any arriving messages (and shows an error message). This can be useful for
 * routing layers (which cannot handle messages). In any case it prevents
 * buffer overflow (because the buffer is continously emptied).
 * <p>
 * @author Urs Hunkeler
 * @version 1.0
 */
 
 
 
public class AbsorbingLayer extends AsynchronousLayer {
    /** A reference to the message pool. */
    private MessagePool mp;
    /** this variable decide if yes or no output in the terminal must be produce*/
    private boolean output;

    public AbsorbingLayer(String name, Parameters params) {
		super(name, params);
		//recuperation du paramètre
		try {
			output = params.getBoolean("output");
		}
		catch(ParamDoesNotExistException pdnee) {
			pdnee.printStackTrace();
		}
    }

    /** There is nothing to initialize. */
    public void initialize(FrancRuntime runtime) {
		this.mp = runtime.getMessagePool();
    }

    /** Starts this layer's thread. */
    public void startup() {
		start();
    }

    /** Discards all messages. */
    public void handleMessage(Message msg) {
		if (output)
			System.out.println(">> msg discarded: msgType: " + ((int)msg.getType()) +
							   ", srcNode: " + msg.getSrcNode() + ", dstNode: " + msg.getDstNode()+", TTL = "+msg.getTTL());
		mp.freeMessage(msg);
    }
}
