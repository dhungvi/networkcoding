package ch.epfl.lsr.adhoc.runtime;

/**
 * This exception is thrown if the program tries to add a new object to the
 * buffer and the buffer is already full.
 * <p>
 * An alternative way to implement this is to use a blocking function. So when
 * the program tries to add an element to an already full buffer, it just has
 * to wait until the buffer has room again (that means, somebody took an element
 * out). The current implementation of Buffer throws an exception because the
 * mechanism for receiving messages should not be delayed.
 * <p>
 * This exception extends java.lang.RuntimeException because there exist
 * mechanismes to prevent this exception from occuring (the method bufferFull()).
 * <p>
 * @see Buffer
 * @author Urs Hunkeler
 * @version 1.0
 */
public class BufferFullException extends RuntimeException {
  /** The object that, by being added to the buffer, caused the exception. */
  private Object obj;

  /**
   * Creates the exception.
   *
   * @param obj  The object that could not be added to the buffer.
   * @param msg  A textual message giving some details for this occurence of the exception.
   */
  public BufferFullException(Object obj, String msg) {
    super(msg);
    this.obj = obj;
  }

  /**
   * Returns the object that, by being added to the buffer, caused the exception.
   * <p>
   * @return The object that could not be added to the buffer.
   */
  public Object getObject() {
    return obj;
  }
}
