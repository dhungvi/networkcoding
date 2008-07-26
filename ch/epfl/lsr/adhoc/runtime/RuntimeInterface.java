package ch.epfl.lsr.adhoc.runtime;

/** TODO
 * Change all the references to FrancRuntime by RuntimeInterface 
 * throughout the code.
 */

public interface RuntimeInterface {

    public Service getService(String name);
    public void initialize();
    public void startup();
	public long getNodeID();
	public String getNodeName();
	public Dispatcher getDispatcher();
	public MessagePool getMessagePool();
}
