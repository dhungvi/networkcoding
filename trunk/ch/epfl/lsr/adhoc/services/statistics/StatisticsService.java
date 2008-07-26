package ch.epfl.lsr.adhoc.services.statistics;

import java.util.*;
import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.MessagePool;
import ch.epfl.lsr.adhoc.runtime.Service;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.runtime.FrancThread;
import ch.epfl.lsr.adhoc.runtime.Parameters;
import ch.epfl.lsr.adhoc.runtime.Buffer;

/**
 * This service does all the processing and storing of statistical information
 * retrieved from the StatisticsLayer.
 *
 * @see StatisticsLayer
 * @see StatMessage
 * @see StatEntry
 *
 * @author Reto Krummenacher
 */
public class StatisticsService extends FrancThread implements Service {

  //FIELDS
  private Buffer buf;
  private Hashtable stats;
  private long startOfService;
  private MessagePool mp;

  //CONSTRUCTORS
  public StatisticsService(Parameters params) {
  }

  //GETTER / SETTER
  /**
   * This getter returns the startup time of this statistic service.
   * This time can be used to evaluate load or messages per time unit.
   *
   * @return The time of startup of this service
   */
  public long getStartOfService() {
    return startOfService;
  }

  //METHODS
  public void initialize(FrancRuntime runtime) {
    mp = runtime.getMessagePool();
    buf = new Buffer();
    stats = new Hashtable();

    setDaemon(true);
  }

  public void startup() {
    Date date = new Date();
    startOfService = date.getTime();
    start();
  }

  public synchronized void deliverMessage(Message msg) {
    buf.add(msg);
    notify();
  }

  public synchronized void run() {
    StatEntry stEntry = null;
    char key = '\0';
    Character htableKey = null;
    while(true) {
      try {
        wait(Long.MAX_VALUE);
        while(!buf.isEmpty()) {
          StatMessage newMsg = (StatMessage)buf.remove();
          key = newMsg.getMessageType();
          htableKey = new Character(key);
          if (stats.containsKey(htableKey)) {
            stEntry = (StatEntry)stats.get(htableKey);
          } else {
            stEntry = new StatEntry(key);
            stats.put(htableKey, stEntry);
          }
          stEntry.updateEntry(newMsg.getSize(),newMsg.getIO());
          mp.freeMessage(newMsg);
        }
      } catch (InterruptedException ie) {
      }
    }
  }

  //STATISTICS RETURN METHODS
  /**
   * This method returns a vector containing the names of all message types
   * seen by the service. Thus for all names returned some statistics are
   * available.
   *
   * @return A vector containing strings representing the names of the message
   * types available.
   */
  public Vector getMessageTypes() {
    StatEntry entry = null;
    String typeName = "";
    Vector vect = new Vector();
    if (stats != null) {
      Enumeration enumeration = stats.elements();
      while(enumeration.hasMoreElements()) {
        entry = (StatEntry)enumeration.nextElement();
        typeName = mp.getMessageType(entry.getType());
        vect.addElement(typeName);
      }
    }
    return vect;
  }

  /**
   * This method returns the trafic load for a given message type name.
   * The result is stored in a vector of three elements. At the first position
   * the number of bytes leaving the stack (outbound) are found. The second
   * value contains the bytes arriving from outside (inbound) and the third
   * integer gives the total number of bytes having passed through the
   * statistics layer.
   *
   * @param messageTypeName The name of the message type (e.g. RReq)
   *
   * @return A vector containing integers representing the trafic load for
   * the given message type.
   * [1st element := Outbound; 2nd := Inbound; 3rd := Total]
   */
  public Vector getTraficLoad(String messageTypeName) {
    Vector vect = new Vector();
    Character htableKey = new Character(mp.getMessageType(messageTypeName));
    StatEntry entry = (StatEntry)stats.get(htableKey);
    int in = entry.getInSize();
    int out = entry.getOutSize();
    vect.addElement(new Integer(out));
    vect.addElement(new Integer(in));
    vect.addElement(new Integer(in + out));
    return vect;
  }

  /**
   * This method returns the number of messages for a given message type name.
   * The result is stored in a vector of three elements. At the first position
   * the number of messages leaving the stack (outbound) are found. The second
   * value contains the messages arriving from outside (inbound) and the third
   * integer gives the total number of messages having passed through the
   * statistics layer.
   *
   * @param messageTypeName The name of the message type (e.g. RReq)
   * @return A vector containg integers representing the number of messages for
   * the given message type.
   * [1st element := Outbound; 2nd := Inbound; 3rd := Total]
   */
  public Vector getNumberMessages(String messageTypeName) {
    Vector vect = new Vector();
    Character htableKey = new Character(mp.getMessageType(messageTypeName));
    StatEntry entry = (StatEntry)stats.get(htableKey);
    int in = entry.getInMessages();
    int out = entry.getOutMessages();
    vect.addElement(new Integer(out));
    vect.addElement(new Integer(in));
    vect.addElement(new Integer(in + out));
    return vect;
  }
  /**
   * This method is used for test or for short overviews. It transforms the
   * content of the statistics database in a string representation.
   *
   * @return A string representation of the database content.
   */
  public String statsToString() {
    StatEntry entry = null;
    Enumeration enumeration = stats.elements();
    String ret = "Statistics:\n---------------------------\n";
    while(enumeration.hasMoreElements()) {
      entry = (StatEntry)enumeration.nextElement();
      ret += entry.toString() + "\n";
    }
    ret += "-----------------------\n";
    return ret;
  }
}

