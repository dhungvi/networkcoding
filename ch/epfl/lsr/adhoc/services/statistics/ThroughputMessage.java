package ch.epfl.lsr.adhoc.services.statistics;

import ch.epfl.lsr.adhoc.runtime.Message;

/**
 * This class is representing the message ThroughputMessage. These messages
 * are very simple ones. They are only used to calculate the throughput
 * and the latency.
 *
 * @see Message
 * @see ThroughputMessageFactory
 *
 * 
 */
public class ThroughputMessage extends Message {

  //FIELDS
  /**
   
   * The type of the message entcountered, thus a field used for statistics
   */
  //private char messageType;
  /**
   * Time of sending the message
   */
  private long time;
  /**
   * if stop is true, the client knows that is the last message for this calculation, he
   * must return his results with this fields also equals to true.
   */
  private boolean stop;
  
  /**
  * 1 if the message returns to the client 0 otherwise
  */
  private int sens;
  

  private double throughput;
  //CONSTRUCTOR

  public ThroughputMessage(char type) {
    super(type);
//    this.type = type;
  }

  //GETTER  / SETTER

  /** 
  * This method is called to get the time of departure of the message 
  */
  public long getTime() {
    return time;
  }
  
  /** 
  * This method is called to know if this message is the last one (true) or not (false)
  */
  public boolean getStop(){
  	return stop;
  }
  
  /** ¨
  * This method is called to get the sens of this message 1 if the message returns to the client 0 otherwise
  */
  public int getSens(){
  	return sens;
  }
  
  public double getThroughput (){
  	return throughput;
  }
  

  //METHODS
  /**
   * This method sets the values of the message
   *
   *@param time The hour of departure
   *@param stop True if it is the last message
   *@param sens 1 if the message returns to the client 0 otherwise
   */
  public void setParameters(long time, boolean stop, int sens, double throughput) {
    this.time = time;
    this.stop = stop;
    this.sens = sens;
    this.throughput = throughput;
  }

  public void prepareData() {
    addLong(time);
    addBoolean (stop);
    addInt (sens);
    addDouble (throughput);
    for (int i = 0;i<119;i++)	
    	addDouble(0.0);
  }

  public void readData() {
    time = getLong();
    stop = getBoolean ();
    sens = getInt ();
    throughput = getDouble ();
  }
}
