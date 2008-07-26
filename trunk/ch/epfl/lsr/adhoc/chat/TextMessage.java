package ch.epfl.lsr.adhoc.chat;

import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This is a simple implementation of a custom made message object.
 * <p>
 * This Message is a very simple implementation of Message, which allows to
 * set and get a textual message. It is created through the MessageFactory class
 * (also a simple test class).
 * <p>
 * @see TextMessageFactory
 * @see Message
 * @author Urs Hunkeler
 * @version 1.0
 */
public class TextMessage extends Message {
  /** Contains the actual message */
  private String text;

  /**
   * Creates a new instance of TextMessage.
   * <p>
   * The type of this message object is initialized at creation time and cannot
   * be changed later (for better use in a MessagePool).
   * <p>
   * @param type The type of service for this message
   */
  public TextMessage(char type) {
    super(type);
  }

  /**
   * Write the text (for this message) with the addString() method to the
   * buffer (for sending the message on the network).
   */
  public void prepareData() {
    addString(text);
  }

  /**
   * Read the text (for this message) from the buffer with the getString() method.
   */
  public void readData() {
    text = getString();
  }

  /**
   * Changes the textual message in this message object.
   * <p>
   * @param text The new textual message
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Returns the textual message contained whithin this message object.
   * <p>
   * @return The textual message in this message object
   */
  public String getText() {
    return text;
  }

  /**
   * Overwrites Object.toString().
   * <p>
   * This method allows this message object to be printed in a statement such
   * as System.out.println().
   * <p>
   * @return A String representation of this object (the text containted in this message)
   */
  public String toString() {
    return text;
  }
}
