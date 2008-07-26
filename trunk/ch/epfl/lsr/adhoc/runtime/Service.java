package ch.epfl.lsr.adhoc.runtime;

/**
 * This is a minimalistic interface for services.
 * <p>
 * A service is a service which is registered with the dispatcher. When the
 * dispatcher receives a message with a type that is registered for a service,
 * it will directly pass the message to the service by invoking the services
 * deliverMessage() method.
 * <p>
 * @see Dispatcher
 * @author Javier Bonny
 * @version 1.0
 */
public interface Service {
  /**
   * The deliverMessage() method allows to deliver the given message to the service.
   * <p>
   * The method should return as fast as possible (otherwise it might block the
   * reception of other messages).
   * <p>
   * @param msg the message object to be delivered
   */
  public void deliverMessage(Message msg);

  /**
   * This method is used to initialize the service.
   * <p>
   * The service is automatically instantiaded by the CommunicationsLayer class.
   * This method allows the service to initialize itself with values from a
   * configuration object. The service also receives a reverence to the
   * instance of CommunicationsLayer (which represents the current node).
   * <p>
   * @param config The configuration object (containing all configuration information)
   * @param comLayer The CommunicationsLayer for this node
   */

  public void initialize(FrancRuntime runtime);
  /**
   * This method is used (by CommunicationsLayer) to start this service.
   * <p>
   * For instance, a service might have its own thread, which can be started here.
   * Also note that a service should only start to send message after its
   * startup() method has been invoked.
   */
  public void startup();
}
