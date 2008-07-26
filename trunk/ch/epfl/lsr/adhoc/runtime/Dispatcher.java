package ch.epfl.lsr.adhoc.runtime;

import java.util.*;
 
/**
 * This class dipatches the messages acoording to their types.
 * <p>
 * The Dispatcher lays between the communications layer and the routing layer.
 * It is the access point for the different services.
 * A service can register his type in the dispatcher, so every message of this type
 * taken from the communication layer will be delivered to this service instead of
 * being put in the dispatcher buffer.
 * <p>
 * @author Javier Bonny
 * @version 1.0
 */
public class Dispatcher extends AsynchronousLayer {
  /** The table containing the different services*/
  private Hashtable types;
  /** The table containing the different services and their names*/
  private Hashtable services;
  /** The table containing the different names for a service type*/
  private Hashtable serviceNames;
  /** The message pool*/
  private MessagePool messagePool;

  /**
   * The constructor.
   * <p>
   * The class has a protected constructor. There is exactly one dispatcher per
   * node. A dispatcher should not be created manually, this is usually done by
   * the Main class, which initializes the whole stack according to the
   * configuration file.
   */
  public Dispatcher(String name, Parameters params) {
    super(name,params);
    types = new Hashtable();
    services = new Hashtable();
    serviceNames = new Hashtable();
  }

  /**
   * This method initializes the message pool variable.
   * <p>
   * @param config The object containing the configuration information
   * @param comLayer A reference to the instance of CommunicationsLayer
   *                 for which this instance is the dispatcher
   */
  public void initialize(FrancRuntime runtime) {
    this.messagePool = runtime.getMessagePool();
  }

  /**
   * This method starts the thread.
   */
  public void startup() {
    start();
  }

  /**
   * The method that handles the messages.
   * <p>
   * This method is called from the layers thread as soon as there is a new
   * message available.
   * <p>
   * If the message is for one of the registered services, it is delivered to this service.
   * Otherwise the message is put into the dispatcher buffer, where an upper layer can take it out.
   * <p>
   * @param msg The message newly received
   */
  protected void handleMessage(Message msg) {
    Character type = new Character(msg.getType());
    Service service = (Service)types.get(type);
    if (service != null) {
      service.deliverMessage(msg); //deliver to corresponding service
    } else {
      super.handleMessage(msg); //put in Buffer
    }
  }

  /**
   * The method used to register a service.
   * <p>
   * It registers the given type with the given service. From now on when a message
   * of the given type is received, it is automatically sent to the service.
   * <p>
   * @param msgtype The type of the message
   * @param service The service who wants to be registered
   * @param name The name of the service
   * @throws AlreadyRegisteredException
   * @throws IllegalArgumentException
   */
  public void register(char msgtype, Service service, String name) {
    Character type =  new Character(msgtype);
    if (service == null){
      throw new IllegalArgumentException("The service cannot be null");
    } else if (types.containsKey(type)){
      throw new AlreadyRegisteredException(msgtype);
    } else if(name == null) {
      throw new IllegalArgumentException("The name cannot be null");
    }
    types.put(type, service);
    services.put(name, service);
    serviceNames.put(type, name);
  }

  /**
   * The method used to unregister a service.
   * <p>
   * It removes the service corresponding to the given type. Messages that arrive
   * now and were earlier assigned to this service, they will now be forwarded to
   * the next layer (just like other messages).
   * <p>
   * @param type The type of the message
   */
  public void unregister(char type) {
    Character mType = new Character(type);
    String mName = (String)serviceNames.get(mType);
    types.remove(mType);
    serviceNames.remove(mType);
    services.remove(mName);
  }

  /**
   * Returns the named service.
   * <p>
   * @return A reference to the named service.
   */
  public Service getService(String name) {
   Service mod = (Service)services.get(name);
    if(mod == null) {
      throw new RuntimeException("Service '" + name + "' not found");
    }
    return mod;
  }
}
