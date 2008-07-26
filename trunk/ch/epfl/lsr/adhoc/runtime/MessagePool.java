package ch.epfl.lsr.adhoc.runtime;

import java.util.*;

/**
 * The message pool organizes the reuse of message objects.
 * <p>
 * The message pool will, instead of creating new object, take existing objects
 * that are no longer used and reuse them. It is very important to correctly
 * implement the freeing (method freeMessage()) of messages that are no longer
 * used. (not yet implemented)
 * <p>
 * The message pool will keep a list of message types and their associated
 * message factories. When a message arrives on the network, the message pool
 * will determine its type (first two bytes) and then create a message object
 * with the appropiate message factory.
 * <p>
 * @author Javier Bonny
 * @version 1.0
 */
public class MessagePool {
  /** The table containing the different MessageFactory types */
  private Hashtable mfTypes;
  /** The table containing the message types (char) associated with the message type names */
  private Hashtable msgTypes;
  /** The table containing the message type names associated with the message types (char) */
  private Hashtable msgTypeNames;
  /** The array containing the different freed Messages */
  private Vector[] messages;
  /** The array containing the different MessageFactory */
  private IMessageFactory[] msgFactories;
  /** The array containing the different messages names */
  private String[] msgNames;
  /** the maximum messages contained in messages */
  private int pool = 64;
  
  /**
   * Constructor.
   * <p>
   * The constructor simply initializes empty tables.
   */
    /**
     * TODO
     * Make the constructor protected but accessible from FrancRuntime
     */
  public MessagePool() {
    mfTypes = new Hashtable();
    msgTypes = new Hashtable();
    msgTypeNames = new Hashtable();
    // --> need a separate init method (for configuration)?
    messages = new Vector[pool];
    msgFactories = new IMessageFactory[pool];
    msgNames = new String[pool];
  }
  
  /**
   * Returns a reference on the unique MessagePool object.
	* @return A reference on the Message Pool.
	*/
  /*public static synchronized MessagePool getMessagePool() {
      throw new RuntimeException("Message should not be accessed this way but through Runtime instead.");
      }*/

  /**
   * This method takes a byte array and creates a message object with this
   * data.
   * <p>
   * @param data The byte array containing the message as received on the network
   *             (the information in this
   *             byte array is copied and the original byte array can be reused)
   * @param length The number of bytes that will be used for the message
   * @return A message object containing the given data
   * @see Message
   * @throws UnknownMessageTypeException
   * @throws IllegalArgumentException
   * @throws PoolLimitExceededException
   */
  public synchronized Message createMessage(byte[] data, short length) throws UnknownMessageTypeException {
    if (data == null || length < 2){
      throw new IllegalArgumentException("The byte[] is null or smaller than 2 bytes (2 bytes needed for type)");
    }
    char type = Message.getType(data);
    // Changement pour vericiation
    if (type < pool){
      if(messages[type] == null){
        throw new UnknownMessageTypeException("Unknown MessageType (no pool): " + ((int)type), type);
      } else if (!(messages[type].isEmpty())){
        Message msg = (Message)messages[type].elementAt(0);
        messages[type].removeElementAt(0);
        msg.setType(type);
        msg.setByteArray(data, length);
        msg.setMessagePool(this);
        return msg;
      } else {
        IMessageFactory msgFactory = msgFactories[type];
        if(msgFactory != null) {
          Message msg = msgFactory.createMessage(type);
          msg.setByteArray(data, length);
	      msg.setMessagePool(this);          
          return msg;
        }
        throw new UnknownMessageTypeException("Unknown MessageType (no factory): " + ((int)type), type);
      }
    } else {
      Character msgType = new Character(type);
      IMessageFactory msgFactory = (IMessageFactory)mfTypes.get(msgType);
      if(msgFactory != null) {
        Message msg = msgFactory.createMessage(type);
        msg.setMessagePool(this);        
        msg.setByteArray(data, length);
        return msg;
      }
      throw new UnknownMessageTypeException(type);
    }
  }

  /**
   * This method is called to free a message object that is no longer used.
   * <p>
   * The message object becomes available for further use.
   * <p>
   * @param msg The message object that we want to reuse
   * @throws IllegalArgumentException
   */
  public synchronized void freeMessage(Message msg){
    if (msg == null){
      throw new IllegalArgumentException("error: the msg is null");
    }

    char type = msg.getType();
    if(msg.free()) {
      if(type < pool) {			
        messages[type].addElement(msg);
      }
    }
  }

  /**
   * This method is used to add a message factory for a message type.
   * <p>
   * @param msgtype The type of the messages
   * @param mf The message factory that we want to add
   * @param typeName The name of the message type to add
   * @throws IllegalArgumentException
   */
  public synchronized void addMessageFactory(char msgType, IMessageFactory mf, String typeName) {
    if (mf == null){
      throw new IllegalArgumentException("The message factory cannot be null");
    } else if(typeName == null) {
      throw new IllegalArgumentException("There must be a valid name for the message type: " + ((int)msgType));
    }
    if(msgType < pool) {
      if(messages[msgType] != null) {
        throw new IllegalArgumentException("A message factory for this type already exist: " + ((int)msgType));
      } else {
        messages[msgType] = new Vector(); // maybe better to initialize to a default initial size
        msgFactories[msgType] = mf;
        msgNames[msgType] = typeName;
      }
    } else {
      Character type = new Character(msgType);
      if (mfTypes.containsKey(type)){
        throw new IllegalArgumentException("A message factory for this type already exist: " + ((int)msgType));
      }
      mfTypes.put(type, mf);
      msgTypes.put(typeName, type);
      msgTypeNames.put(type, typeName);
    }
  }

  /**
   * Returns the numerical message type for a named message type.
   * <p>
   * If the type cannot be found, this method throws a RuntimeException.
   * <p>
   * @param name The name of the message type
   * @return The corresponding numerical message type
   */
  public synchronized char getMessageType(String name) {
    if(name == null) {
      throw new IllegalArgumentException("name cannot be null");
    }
    for(int i = 0; i < pool; i++){
      if(name.equals(msgNames[i])) {
        return (char)i;
      }
    }
    Character c = (Character)msgTypes.get(name);
    if(c == null) throw new RuntimeException("Type '" + name + "' not found");
    return c.charValue();
  }

  /**
   * Returns the name of a numberical message type.
   * <p>
   * If the type cannot be found, this method throws a RuntimeException.
   * <p>
   * @param type The numerical message type
   * @return The corresponding name
   */
  public synchronized String getMessageType(char type) {
    if(type < pool) {
      return msgNames[type];
    } else {
      Character c = new Character(type);
      String name = (String)msgTypeNames.get(c);
      return name;
    }
  }

  /**
   * This method is used to remove a message factory.
   * <p>
   * @param type The type of the messages correponding to the message factory that will be removed.
   */
  public synchronized void removeMessageFactory(char type){
    if(type < pool) {
      messages[type] = null;
      msgFactories[type] = null;
      msgNames[type] = null;
    } else {
      Character msgType = new Character(type);
      String msgName = (String)msgTypeNames.get(msgType);
      mfTypes.remove(msgType);
      msgTypeNames.remove(msgType);
      msgTypes.remove(msgName);
    }
  }

  /**
   * This method is used to obtain a message of the given type.
   * <p>
   * @param msgtype The type of the message
   * @return a message object of the given type
   * @throws IllegalArgumentException
   */
  public synchronized Message getMessage(char msgtype) {
    if (msgtype < pool){
      if(messages[msgtype] == null) {
        //throw new UnknownMessageTypeException("Unknown MessageType: " + ((int)msgtype), msgtype);
        throw new IllegalArgumentException("There is no message factory for this type of message: " + ((int)msgtype));
      } else if (!(messages[msgtype].isEmpty()) ) {
        Message msg = (Message)messages[msgtype].elementAt(0);
        messages[msgtype].removeElementAt(0);
        msg.setType(msgtype);
        return msg;
      } else {
        Message msg = msgFactories[msgtype].createMessage(msgtype);
        return msg;
      }
    } else {
      Character type =  new Character(msgtype);
      if (mfTypes.containsKey(type)){
        IMessageFactory mf = (IMessageFactory)mfTypes.get(type);
        return mf.createMessage(msgtype);
      } else {
        throw new IllegalArgumentException("There is no message factory for this type of message: " + ((int)msgtype));
      }
    }
  }
}




