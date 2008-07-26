package ch.epfl.lsr.adhoc.runtime;

import java.util.*;

/**
 * The Buffer class stores objects until they are requested.
 * <p>
 * The method for adding messages throws an exception when the buffer is full
 * (because in the context of franc the mechanism of reception of
 * messages should not be delayed). However the method for retrieving objects
 * blocks if no objects are available (if you want to prevent this, use the
 * method isEmpty() before retrieving objects).
 * <p>
 * @author Javier Bonny
 * @version 1.0
 */
public class Buffer {
  /** The Vector object used to store messages (Array List not available in JDK 1.1.6)*/
  private Vector buffer = new Vector();
  /** indicates the maximum number of objects that the buffer can contain*/
  public static int MAX_SIZE = -1;

  /**
   * The default constructor.
   * <p>
   * The construction of the object will fail with an IllegalArgumentException
   * if the MAX_SIZE property is given but contains an invalid number
   * (will be implemented later).
   */
  public Buffer() {
// could pass maxsize as parameter
  }

  /**
   * The method to put an object into the buffer.
   * <p>
   * @param obj The object to be added to the buffer
   * @throws IllegalArgumentException
   * @throws BufferFullException
   */
  public synchronized void add(Object obj) {
    int size = buffer.size();
    if (MAX_SIZE > 0 && size >= MAX_SIZE) {
      throw new BufferFullException(obj, "Buffer is full: " + size + " >= " + MAX_SIZE);
    } else if (obj == null) {
      throw new IllegalArgumentException("error: the message is null");
    } else {
      buffer.addElement(obj);  // add an object at the end of the buffer
      notifyAll();
    }
  }

  /**
   * The method to get a waiting Object.
   * <p>
   * If the buffer is empty, this method will block.
   * @return the removed object
   */
  public synchronized Object remove() {
    while (buffer.isEmpty()){
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
    Object msg = buffer.elementAt(0);
    buffer.removeElementAt(0);
    // notifyAll();  // if we want that add() method also block
    return msg; // return the first object of the buffer
  }

  /**
   * The method to test whether messages are waiting.
   * <p>
   * This method will return true if there no message in the
   * buffer, and false in all other cases.
   * <p>
   * @return a boolean value indicating if the buffer is empty or not
   */
  public boolean isEmpty() {
    return buffer.isEmpty();
  }

  /**
   * The method bufferFull() can be used to test whether the buffer is already
   * full.
   * <p>
   * This method will return true if the limit of objects in the buffer
   * has been reached and false in all other cases (ie. at least one message
   * can be added). As soon as messages are retrieved, this method will again
   * return false.
   * <p>
   * If the MAX_SIZE for the buffer is set to <= 0, there is no limit on the
   * buffer (other than the available memory).
   * <p>
   * @return a boolean value indicating if the buffer is full or not
   */
  public boolean bufferFull() {
    return(buffer.size() >= MAX_SIZE);
  }
}

