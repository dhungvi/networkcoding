package ch.epfl.lsr.adhoc.runtime;

/**
 * If there are to many message objects created and none is freed, at a certain
 * time the maximum number of messages allowed is reached.
 * <p>
 * This exception extends java.lang.RuntimeException because it should rarely
 * happen (if the programm is well behaved, that means, if the programm frees
 * any unused objects).
 * <p>
 * @see ch.epfl.lsr.adhoc.mhm.MessagePool
 * @author Urs Hunkeler
 * @version 1.0
 */
public class PoolLimitExceededException extends RuntimeException {

  /**
   * Creates the exception.
   * <p>
   * @param data The message object that couldn't be sent.
   * @param msg  A textual message giving some details for this occurence of the exception.
   */
  public PoolLimitExceededException(String msg) {
    super(msg);
  }
}
