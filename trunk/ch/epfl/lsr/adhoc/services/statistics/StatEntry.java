package ch.epfl.lsr.adhoc.services.statistics;

/**
 * This class is used to store statistical information about one message type.
 * This information contains size of messages, number of messages etc.
 *
 * @author Reto Krummenacher
 */
public class StatEntry {

    //FIELDS
    /**
     * The type of message represented by this entry
     */
    private char type;
    /**
     * The number of bytes entering the stack for this message
     */
    private int inSize;
    /**
     * The number of bytes leaving the stack for this message
     */
    private int outSize;
    /**
     * The number of messages of this type entering the stack
     */
    private int in;
    /**
     * The number of message of this type leaving the stack
     */
    private int out;

    //CONSTRUCTOR
    StatEntry() {
    }

    StatEntry(char type) {
      this.type = type;
    }

    //GETTER / SETTER
    public char getType() {
      return type;
    }

    public int getInSize() {
      return inSize;
    }

    public int getOutSize() {
      return outSize;
    }

    public int getInMessages() {
      return in;
    }

    public int getOutMessages() {
      return out;
    }

    //METHODS
    /**
     * This method updates this entry with the given values
     *
     * @param size The size of the message to add in byte
     * @param inbound The direction of the message [false := (0)ut; true := (1)n
     */
    public void updateEntry(int size, boolean inbound) {
      if(!inbound) {
        out++;
        outSize = outSize + size;
      } else {
        in++;
        inSize = inSize + size;
      }
    }

    /**
     * This method transforms the values stored for this entry to a string. Only
     * used for testing.
     */
    public String toString() {
      return ("Messagetype[" + ((int)type) + "] -> In: " + in + " : " + inSize +
              "bytes - Out: " + out + " : " + outSize + "bytes");
    }
}
