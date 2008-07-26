package ch.epfl.lsr.adhoc.runtime;

/**
 * This class is the new super class of all messages handled by the MANET framwork.
 * <p>
 * This class handels a complet set of header fields. It further provides different
 * method for serialization.
 * <p>
 * To implement a message, one has only to implement the methods <b>prepareData(),</b>
 * <b>readData()</b> and <b>getID()</b>.
 * <br>
 * <b>prepareData()</b> is called just before
 * the message is sent to the network. All it has to do is put all variables
 * into the byte stream to be sent. This is done with the addXXX() methods.
 * <br>
 * <b>readData()</b> is called just after the message is received on the network.
 * All it has to do is read all variables (in the same order) from the byte stream.
 * This is done with the getXXX() methods.
 * <br>
 * <b>getID()</b> should return the message type. This method is, for instance,
 * called just before the message is sent over the network, to write the header
 * field <i>Message Type</i> to the stream.
 * <p>
 * The format of the header is as follows:<br>
 * <table border = 1>
 * <tr>
 *   <td>0 - 1</td>
 *   <td>2</td>
 *   <td>Message Type</td>
 * </tr>
 * <tr>
 *   <td>2</td>
 *   <td>1</td>
 *   <td>Version</td>
 * </tr>
 * <tr>
 *   <td>3 - 4</td>
 *   <td>2</td>
 *   <td>Header length</td>
 * </tr>
 * <tr>
 *   <td>5 - 12</td>
 *   <td>8</td>
 *   <td>Source Node ID</td>
 * </tr>
 * <tr>
 *   <td>13 - 20</td>
 *   <td>8</td>
 *   <td>Destination Node ID</td>
 * </tr>
 *	<tr>
 *   <td>21 - 28</td>
 *   <td>8</td>
 *   <td>Previous Hop ID</td>
 * </tr>
 *	<tr>
 *   <td>29 - 36</td>
 *   <td>8</td>
 *   <td>Next Hop ID</td>
 * </tr> 
 * <tr>
 *   <td>37</td>
 *   <td>1</td>
 *   <td>TTL</td>
 * </tr>
 * <tr>
 *   <td>38 - 39</td>
 *   <td>2</td>
 *   <td>Sequence Number</td>
 * </tr>
 * <tr>
 *   <td>40-41</td>
 *   <td>2</td>
 *   <td>Reliable Sequence Number</td>
 * </tr>
 * <tr>
 *   <td>42 - </td>
 *   <td>0 - 255</td>
 *   <td>Network bits</td>
 * </tr>
 * </table>
 * <p>
 * A sample implementation:
 * <table border=1><tr><td>
 * <pre>
 * <font color="blue"><b>package</b></font> ch.epfl.lsr.test;
 *
 * <font color="blue"><b>import</b></font> ch.epfl.lsr.adhoc.mhm.*;
 *
 * <font color="blue"><b>public</b></font> <font color="blue"><b>class</b></font> TextMessage extends Message {
 *   <font color="blue"><b>private</b></font> String text;
 *   <font color="blue"><b>private</b></font> <font color="blue"><b>char</b></font> type;
 *
 *   <font color="blue"><b>public</b></font> TextMessage(<font color="blue"><b>char</b></font> type) {
 *     super(type);
 *   }
 *
 *   <font color="blue"><b>public</b></font> void prepareData() {
 *     addString(text);
 *   }
 *
 *   <font color="blue"><b>public</b></font> void readData() {
 *     text = getString();
 *   }
 *
 *   <font color="blue"><b>public</b></font> void setText(String text) {
 *     this.text = text;
 *   }
 *
 *   <font color="blue"><b>public</b></font> String getText() {
 *     <font color="blue"><b>return</b></font> text;
 *   }
 * }
 * </pre>
 * </td></tr></table>
 * <p>
 * @author Urs Hunkeler
 * @version 1.0
 */
public abstract class Message {
    /**
     * The constant used to say that a string was null.
     * <p>
     * When a string is null, this value will be used for the length. Therefore the
     * maximum length of a string is (0xFFFF - 1).
     */
    private static final char NULL_STRING = (char)0xFFFF;
    /**
     * The current version of the message (if there are different nodes running
     * different versions of the manet framework implementation).
     */
    private static final byte VERSION = (byte)1;
  
    /**
     * Special destination for a broadcast message.
     * @see #setDstNode
     */
    public static final long BROADCAST = 0l;
  
    /** This is the buffer for sending or receiving the message.
     *  <p>
     *  This buffer is only set when sending or receiving the message.
     */
    public byte[] data;
    /**
     * The current index in the buffer (only for sending or receiving).
     */
    private int index;
    /**
     * The length (in bytes) of this message, when sent on the network.
     * <p>
     * This value is only valid (>= 0) when the message was received on the
     * network.
     */
    private int length = -1;

    /** The unique node id of the source node (the node originally sending this message)*/
    private long srcNode=-1;
    /** The unique node id of the destination for this message, or 0 for broadcast*/
    private long dstNode;
    /** The ID the previous hop*/
    private long previousHop;
    /** The ID the next hop*/
    private long nextHop=-1;
  
    /** The time to live (number of hops) for this message*/
    private int ttl=255;
    /**
     * The sequence number of this message.
     * <p>
     * Together with the source node id, the sequence number uniquely identifies
     * the message. Actually, the sequence number repeates itself after 0xFFFF
     * messages. But this should be large enough to be sure not to have to different
     * messages with the same sequence number at the same time.
     */
    private int sequenceNumber;
    /**
     * The network bits for virtual networking.
     * <p>
     * Each byte contains 8 bits, which represent each another virtual network.
     * The message is sent on network n if the n-th bit is set.
     * @see ch.epfl.lsr.adhoc.communicationslayer.VirtualNetworksLayer
     */
    private byte[] networks;
    /** The message type for this message */
    private char msgType;
    /** The number of active copies of this message (a message cannot be recycled until all copies are freed) */
    private int copies = 1;

    /** Together with the sourceNode it builds the unique identifier of the message.
	Needed and added for the Reliable Broadcast
	* The difference to the sequenceNumber makes the fact, that this field is already and only set by the Reliable Layer. It's needed to recognize the Acknowledges arrived from the Destinations nodes.
	*/
    private int reliableSeqNbr=-1;
    
    /** 
     * Reference to the message pool necessary to deserialize messages 
     * contained into this message.
     */
    
    private transient MessagePool messagePool;

    /**
     * Default constructor.
     */
    public Message(char type) {
	this.msgType = type;
    }

    /**
     * This method copies the serialized form of the message into the given byte
     * array and returns the length (in bytes).
     * <p>
     * Actually, this method only copies the header information and asks the
     * subclass to add the message information through the addXXX() methods
     * (this is done in the method prepareData()).
     * <p>
     * @param data The buffer into which the message is written
     * @return The number of bytes written
     * 
     */
    public final synchronized int getByteArray(byte[] data) {
	this.data = data;

	// type
	data[0] = (byte)(msgType >> 8);
	data[1] = (byte)msgType;

	// version
	data[2] = VERSION;

	// header length
	int lNetworks = (networks == null)?0:networks.length;
	int lHeader = 43; //2 + 1 + 2 + 8 + 8 + 8 + 8 + 1 + 2 + 1 +2 = type + version + size header + srcNode + dstNode + previousHop + nextHop + ttl + seq# + size networks + reliableSeq#
	lHeader += lNetworks;
	data[3] = (byte)(lHeader >> 8);
	data[4] = (byte)lHeader;

	// src node
	data[ 5] = (byte)(srcNode >> 56);
	data[ 6] = (byte)(srcNode >> 48);
	data[ 7] = (byte)(srcNode >> 40);
	data[ 8] = (byte)(srcNode >> 32);
	data[ 9] = (byte)(srcNode >> 24);
	data[10] = (byte)(srcNode >> 16);
	data[11] = (byte)(srcNode >>  8);
	data[12] = (byte) srcNode;

	// dst node
	data[13] = (byte)(dstNode >> 56);
	data[14] = (byte)(dstNode >> 48);
	data[15] = (byte)(dstNode >> 40);
	data[16] = (byte)(dstNode >> 32);
	data[17] = (byte)(dstNode >> 24);
	data[18] = (byte)(dstNode >> 16);
	data[19] = (byte)(dstNode >>  8);
	data[20] = (byte) dstNode;

	// previous hop
	data[21] = (byte)(previousHop >> 56);
	data[22] = (byte)(previousHop >> 48);
	data[23] = (byte)(previousHop >> 40);
	data[24] = (byte)(previousHop >> 32);
	data[25] = (byte)(previousHop >> 24);
	data[26] = (byte)(previousHop >> 16);
	data[27] = (byte)(previousHop >>  8);
	data[28] = (byte) previousHop;
	 
	// next hop
	data[29] = (byte)(nextHop >> 56);
	data[30] = (byte)(nextHop >> 48);
	data[31] = (byte)(nextHop >> 40);
	data[32] = (byte)(nextHop >> 32);
	data[33] = (byte)(nextHop >> 24);
	data[34] = (byte)(nextHop >> 16);
	data[35] = (byte)(nextHop >>  8);
	data[36] = (byte) nextHop;

	// ttl
	data[37] = (byte)ttl;

	//sequence number
	data[38] = (byte)(sequenceNumber >> 8);
	data[39] = (byte) sequenceNumber;

	// reliable Sequence Number
	data[40] = (byte)(reliableSeqNbr >> 8);
	data[41] = (byte)(reliableSeqNbr);

	// network bytes
	data[42] = (byte)lNetworks ;
	if(lNetworks > 0) {
	    System.arraycopy(networks, 0, data, lHeader-lNetworks, lNetworks);
	}


	// other data
	index = lHeader;
	prepareData();
	this.data = null;
	this.length = index;
	return index;
    }

    /**
     * This method initializes all variables of the message object (and its
     * subclasses) with the serialized form of the message contained in the
     * buffer.
     * <p>
     * Actually, this method only initializes the header information. The subclass
     * is asked to read its data itself with the methods getXXX() (this is done
     * in the method readData()).
     * <p>
     * @param data The buffer containing the received message
     * @param length The length of the data received
     */
    public final synchronized void setByteArray(byte[] data, int length) {
	this.data = data;
	this.length = length;

	// type
	msgType = (char)(((data[0] & 0xFF) << 8) + (data[1] & 0xFF));

	// version
	if(data[2] < VERSION)
	    throw new RuntimeException("Version of Message not supported");

	// header length
	index = (int)(((data[3] & 0xFF) << 8) + (data[4] & 0xFF));

	// srcNode
	srcNode = ((data[ 5] & 0xFFL) << 56) +
	    ((data[ 6] & 0xFFL) << 48) +
	    ((data[ 7] & 0xFFL) << 40) +
	    ((data[ 8] & 0xFFL) << 32) +
	    ((data[ 9] & 0xFFL) << 24) +
	    ((data[10] & 0xFFL) << 16) +
	    ((data[11] & 0xFFL) <<  8) +
	    (data[12] & 0xFFL);

	// dstNode
	dstNode = ((data[13] & 0xFFL) << 56) +
	    ((data[14] & 0xFFL) << 48) +
	    ((data[15] & 0xFFL) << 40) +
	    ((data[16] & 0xFFL) << 32) +
	    ((data[17] & 0xFFL) << 24) +
	    ((data[18] & 0xFFL) << 16) +
	    ((data[19] & 0xFFL) <<  8) +
	    (data[20] & 0xFFL);

	// previousHop
	previousHop = ((data[21] & 0xFFL) << 56) +
	    ((data[22] & 0xFFL) << 48) +
	    ((data[23] & 0xFFL) << 40) +
	    ((data[24] & 0xFFL) << 32) +
	    ((data[25] & 0xFFL) << 24) +
	    ((data[26] & 0xFFL) << 16) +
	    ((data[27] & 0xFFL) <<  8) +
	    (data[28] & 0xFFL);

	// nextHop
	nextHop = ((data[29] & 0xFFL) << 56) +
	    ((data[30] & 0xFFL) << 48) +
	    ((data[31] & 0xFFL) << 40) +
	    ((data[32] & 0xFFL) << 32) +
	    ((data[33] & 0xFFL) << 24) +
	    ((data[34] & 0xFFL) << 16) +
	    ((data[35] & 0xFFL) <<  8) +
	    (data[36] & 0xFFL);

	// ttl
	ttl = (data[37] & 0xFF);

	// sequence number
	sequenceNumber = ((data[38] & 0xFF) << 8) +
	    (data[39] & 0xFF);

	// reliable Sequence number
	reliableSeqNbr= ((data[40] & 0xFF) << 8) + (data[41] & 0xFF);

	// networks
	int lNetworks = (int)((data[42] & 0xFF));
	if(lNetworks > 0) {
	    if(networks == null || networks.length != lNetworks)
		networks = new byte[lNetworks];
	    System.arraycopy(data, index-lNetworks, networks, 0, lNetworks);
	} else {
	    networks = null;
	}



	readData();
	this.data = null;
    }

    /**
     * This method returns the numerical message type (used to construct the
     * header information).
     * <p>
     * @return The numerical message type for this message
     */
    public final char getType() {
	return msgType;
    }

    /**
     * This method returns the length of this message in the serialized form in
     * bytes (for instance the number of bytes that are used to send this message over the
     * network).
     * <p>
     * Actually, this information is only available for messages received on the
     * network. If you want to know how many bytes a message needs to be sent over
     * the network, send it and use the return value of the sendMessage() method in
     * Data Link Layer.
     * <p>
     * @return The length in bytes that this message occupied on the network
     */
    public synchronized int getLength(){
	if (this.length < 0)
	    throw new RuntimeException("IllegalState: The length is only available for messages received on the network");
	return this.length;
    }

    /**
     * Set the unique node id of the source node (original sender).
     * <p>
     * This is usually donne automatically in the lowest layer (network layer, for
     * instance: AsyncMulticast).
     * <p>
     * @param The unique node id of the original sender
     */
    public void setSrcNode(long nodeID) {
	this.srcNode = nodeID;
    }

    /**
     * Set the unique node id of the destination node.
     * <p>
     * This value is used by the routing layer. A value set to BROADCAST means, broadcast
     * to any receiving node.
     * <p>
     * @param The unique node id of the destination (0 means broadcast)
     */
    public void setDstNode(long nodeID) {
	this.dstNode = nodeID;
    }
  
    /**
     * Set the unique node id of the next hop.
     * <p>
     * This value is used by the routing layer. A value set to BROADCAST means, broadcast
     * to any receiving node.
     * <p>
     * @param The unique node id of the next hop (0 means broadcast)
     */
    public void setNextHop(long nodeID) {
	this.nextHop = nodeID;
    }
  
  
    /**
     * Set the unique node id of the previous hop, from which the message
     * was received directly.
     * <p>
     * This is usually donne automatically in the lowest layer (network layer, for
     * instance: AsyncMulticast).
     * <p>
     * @param The unique node id of the next hop (0 means broadcast)
     */
    public void setPreviousHop(long nodeID) {
	this.previousHop = nodeID;
    }
    
    protected void setMessagePool(MessagePool mp) {
    	messagePool = mp;
    }

    /**
     * Return the unique node id of the original sender.
     * <p>
     * @return The unique node id of the original sender
     */
    public long getSrcNode() {
	return this.srcNode;
    }

    /**
     * Return the unique node id of the destination (or BROADCAST for broadcast).
     * <p>
     * @return The unique node id of the destination (or BROADCAST for broadcast)
     */
    public synchronized long getDstNode() {
	return this.dstNode;
    }
  
  
    /**
     * Return the unique node id of the next hop (or BROADCAST for broadcast).
     * <p>
     * @return The unique node id of the next hop (or BROADCAST for broadcast)
     */
    public long getNextHop() {
	return this.nextHop;
    }
  
    /**
     * Return the unique node id of the previous hop .
     * <p>
     * @return The unique node id of the next hop
     */
    public long getPreviousHop() {
	return this.previousHop;
    }

    /**
     * Set the time to live for this packet (number of hops this packet should be
     * forwarded).
     * <p>
     * @param The new TTL
     */
    public void setTTL(int ttl) {
	if(ttl < 0 || ttl > 0xFF)
	    throw new RuntimeException("TTL must be > 0 and < 255");
	this.ttl = ttl;
    }

    /**
     * Returns the current TTL for this message.
     * <p>
     * @return The TTL
     */
    public int getTTL() {
	return this.ttl;
    }

    /**
     * Set the sequence number for this message.
     * <p>
     * Usually, the sequence number is set by the original sender in the lowest
     * layer (network layer, for instance: AsyncMulticast).
     * <p>
     * Together with the source node id, the sequence number uniquely identifies
     * the message. Actually, the sequence number repeates itself after 0xFFFF
     * messages. But this should be large enough to be sure not to have to different
     * messages with the same sequence number at the same time.
     * <p>
     * @param sequenceNumber The sequence number for this message
     */
    public void setSequenceNumber(int sequenceNumber) {
	if(sequenceNumber < 0 || sequenceNumber > 0xFFFF)
	    throw new RuntimeException("The sequence number must be > 0 and < 0xFFFF");
	this.sequenceNumber = sequenceNumber;
    }

    /**
     * Return the sequence number of this message.
     * <p>
     * @return The sequence number
     */
    public int getSequenceNumber() {
	return this.sequenceNumber;
    }

    /**
     * Set the network bits.
     * <p>
     * The network bits are used to send simulate different networks.
     * <p>
     * @see ch.epfl.lsr.adhoc.communicationslayer.VirtualNetworksLayer
     * @param networks A byte array containing the network bits (the length of this
     *                 byte array must be <= 255)
     */
    public void setNetworks(byte[] networks) {
	if (networks != null && networks.length > 255){
	    throw new RuntimeException("The number of network bytes must be <= 255 ");
	}
	// we decided to use this approach because a node might potentially
	// receive more messages than it will send. It therefore is more
	// performant if you copy the network bits for the messages sent instead
	// of creating a new byte array for every message received (you won't
	// have to create new byte arrays every time you send a message thanks to message pool)
	if(this.networks == null || this.networks.length != networks.length)
	    this.networks = new byte[networks.length];
	System.arraycopy(networks, 0, this.networks, 0, networks.length);
    }

    /**
     * Returns the network bits (indicating on which networks this message was sent).
     * <p>
     * @return A byte array containing the network bits
     */
    public byte[] getNetworks() {
	return networks;
    }

    /**
     * Indicates whether the message was sent on the given network.
     * <p>
     * @see ch.epfl.lsr.adhoc.communicationslayer.VirtualNetworksLayer
     * @param The id of the virtual network
     * @return True, if the message was sent on this network, false otherwise
     */
    public boolean isInNetwork(int id) {
	if(networks == null) return false;
	int nByte = (int)(id >> 3); // division par 8
	int bit = (int)(id & 7); // modulo 8
	if(nByte >= networks.length) return false;
	return ((networks[nByte] & (1 << bit)) != 0);
    }

    /**
     * This method is called before the data is written to the network.
     * <p>
     * In this method you should write all your data with the addXXX() methods.
     */
    public abstract void prepareData();

    /**
     * This method is called when the message object is initialized with
     * data from the network.
     * <p>
     * In this method you should read all your values with the getXXX() methods.
     */
    public abstract void readData();

    /**
     * Increments the internal count of copies of this message.
     * <p>
     * This is needed if a part of the code (such as the flooding layer) directs
     * different references of the message to different parts of the framework.
     * For instance the flooding layer may retransmit the message and send it to
     * the layer above. In that case the Data Link layer would free the message,
     * as well as the other layer that removes the message from the stack. To
     * avoid this, a message is only truely recicled when all the copies have
     * been freed.
     */
    public final void createCopy() {
	copies ++;
    }

    /**
     * Decrements the number of copies and if no copies remain, resets header information.
     * <p>
     * This method decrements the number of copies. If no copies remain, the
     * header information is reinitialized. Also the method reset() is called for
     * that the actual implementation of the message can reinitialize user data
     * as well.
     * <p>
     * @return True, if the message can be recycled, false otherwise
     */
    public final boolean free() {
	if((--copies) <= 0) {
	    // reset header fields
	    data = null;
	    index = 0;
	    length = -1;
	    srcNode = -1;
	    dstNode = -1;
	    previousHop = -1;
	    nextHop = -1;		
	    ttl = 0;
	    sequenceNumber = 0;
	    //networks = null; // networks not reset to save the time used for object creation
	    msgType = (char)0;
	    copies = 1;
	    reliableSeqNbr=-1;

	    // custom reset
	    reset();
	    return true;
	}
	return false;
    }

    /**
     * This method may be overriden by an implementation to reinitialize local variables.
     */
    public void reset() {
    }

    /**
     * Changes the type of the message.
     * <p>
     * This method should only be called by message pool.
     * This is usefull if the same message object can be used for more than one message type.
     * <p>
     * @param type The new type of the message
     */
    protected void setType(char type) {
	this.msgType = type;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param i An integer to add
     */
    protected final synchronized void addInt(int i) {
	byte[] data = this.data;
	int index = this.index;

	data[index++] = (byte)'i';
	data[index++] = (byte)(i >> 24);
	data[index++] = (byte)(i >> 16);
	data[index++] = (byte)(i >>  8);
	data[index++] = (byte)i;
	this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @return An integer
     */
    protected synchronized final int getInt() {
	byte[] data = this.data;
	int index = this.index;
	if(data[index] != 'i') {
	    throw new RuntimeException("No integer found");
	}

	index++;
	int i = ((data[index++] & 0xFF) << 24) +
            ((data[index++] & 0xFF) << 16) +
            ((data[index++] & 0xFF) <<  8) +
	    (data[index++] & 0xFF);
	this.index = index;
	return i;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param c A character to add
     */
    protected final synchronized void addChar(char c) {
	byte[] data = this.data;
	int index = this.index;

	data[index++] = (byte)'c';
	data[index++] = (byte)(c >>  8);
	data[index++] = (byte)c;
	this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @return A character
     */
    protected final synchronized char getChar() {
	byte[] data = this.data;
	int index = this.index;
	if(data[index] != 'c') {
	    throw new RuntimeException("No char found");
	}

	index++;
	char c = (char)(((data[index++] & 0xFF) <<  8) +
			(data[index++] & 0xFF));
	this.index = index;
	return c;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param b A boolean to add
     */
    protected final synchronized void addBoolean(boolean b) {
	byte[] data = this.data;
	int index = this.index;

	if(b)
	    data[index++] = (byte)'1';
	else
	    data[index++] = (byte)'0';

	this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @return A boolean
     */
    protected final synchronized  boolean getBoolean() {
	byte[] data = this.data;
	int index = this.index;
	boolean b = false;
	if(data[index] == '1') {
	    b = true;
	} else if(data[index] == '0') {
	    // b = false; // already false
	} else {
	    throw new RuntimeException("No boolean found");
	}

	index++;
	this.index = index;
	return b;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param b A byte to add
     */
    protected final synchronized void addByte(byte b) {
	byte[] data = this.data;
	int index = this.index;

	data[index++] = (byte)'b';
	data[index++] = b;
	this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @return A byte
     */
    protected final synchronized byte getByte() {
	byte[] data = this.data;
	int index = this.index;
	if(data[index] != 'b') {
	    throw new RuntimeException("No byte found");
	}

	index++;
	byte b = data[index++];
	this.index = index;
	return b;
    }
  
  
    /**
     * Adds the given Message to the buffer.
     * <p>
     * This method should be called from prepareData() to write a message to
     * the send buffer for sending the message over the network.
     * <p>	 
     * 
     * @param msg A message
     */  
    protected final synchronized  void addMessage(Message msg) {
	byte msgToBytes[] = new byte[data.length];
	int msgLength = msg.getByteArray(msgToBytes);
	data[index++] = (byte)'m';
	addInt(msgLength);
	System.arraycopy(msgToBytes,0,data,index,msgLength);
	index = index + msgLength;
    }

    /**
     * Reads a message from the receive buffer.
     * <p>
     * This method should be called from readData() to read a message from the
     * receive buffer.
     * <p> 
     * 
     * @param msg A message
     * @throws UnknownMessageTypeException Exception thrown if the internal message 
     *												  cannot be deserialized.
     */  	
    protected final synchronized  Message getMessage() throws UnknownMessageTypeException {
	if(data[index] != 'm') {
	    throw new RuntimeException("No message found : "+(char)data[index]);
	}
	index++;
	int msgLength = getInt();
	byte msgToBytes[] = new byte[data.length];
	System.arraycopy(data,index,msgToBytes,0,msgLength);
	index = index + msgLength;
	Message msg = messagePool.createMessage(msgToBytes,(short)msgLength);
	/*
	 * By convention, the encapsulated message inherits all the header of its container
	 * except the msgID.
	 */
	msg.setSrcNode(getSrcNode());
	msg.setDstNode(getDstNode());
	msg.setPreviousHop(getPreviousHop());
	msg.setNextHop(getNextHop());
	msg.setSequenceNumber(getSequenceNumber());
	msg.setReliableSeqNbr(getReliableSeqNbr());
	msg.setTTL(getTTL());
    msg.setMessagePool(messagePool);
	return msg;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param s A String to add
     */
    protected final synchronized void addString(String s) {
	byte[] data = this.data;
	int index = this.index;
	char length = 0;

	data[index++] = (byte)'S';

	if(s == null) {
	    length = NULL_STRING;
	} else {
	    length = (char)s.length();
	    if(length >= NULL_STRING) {
		throw new IllegalArgumentException("The String is too long");
	    }
	}
	data[index++] = (byte)(length >>  8);
	data[index++] = (byte)length;

	if(s != null && length > 0) {
	    byte[] bytes = s.getBytes();
	    System.arraycopy(bytes, 0, data, index, length);
	    index += length;
	}
	this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @return A String
     */
    protected final String getString() {
	byte[] data = this.data;
	int index = this.index;
	if(data[index] != 'S') {
	    throw new RuntimeException("No String found");
	}

	index++;
	String s = null;
	char length = (char)(((data[index++] & 0xFF) <<  8) +
			     (data[index++] & 0xFF));
	if(length == NULL_STRING) {
	    // s = null; // already null
	} else if(length == 0) {
	    s = "";
	} else {
	    s = new String(data, index, length);
	    index += length;
	}
	this.index = index;
	return s;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param s A byte array to add and its length
     * @author Kave Salamatian
     * @version 0.1

     */
    protected final synchronized void  addBytes(byte[] b, int length) {
    byte[] data = this.data;
    int index = this.index;
	
    data[index++] = (byte)'B';
    this.index = index;
    addInt(length);
    index=this.index;
    System.arraycopy(b,0,data,index,length);
    index = index + length;
    this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @param s A buffer to store the byte array 
     * @return the length of the byte array
     * @author Kave Salamatian
     * @version 0.1

     */
    protected final synchronized  int getBytes(byte[] buf ) {
	byte[] data = this.data;
	int index = this.index;
	if(data[index] != 'B') {
	    throw new RuntimeException("No Byte array found");
	}
	index++;
	this.index=index;
	int length= getInt();
	index=this.index;
	System.arraycopy(data, index, buf, 0, length);
	index += length;
	this.index = index;
	return length;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param l A long to add
     */
    protected final synchronized void addLong(long l) {
	byte[] data = this.data;
	int index = this.index;

	data[index++] = (byte)'l';
	data[index++] = (byte)(l >> 56);
	data[index++] = (byte)(l >> 48);
	data[index++] = (byte)(l >> 40);
	data[index++] = (byte)(l >> 32);
	data[index++] = (byte)(l >> 24);
	data[index++] = (byte)(l >> 16);
	data[index++] = (byte)(l >>  8);
	data[index++] = (byte)l;
	this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @return A long
     */
    protected final synchronized long getLong() {
	byte[] data = this.data;
	int index = this.index;
	if(data[index] != 'l') {
	    throw new RuntimeException("No long found");
	}

	index++;
	long l = ((data[index++] & 0xFFL) << 56) +
	    ((data[index++] & 0xFFL) << 48) +
	    ((data[index++] & 0xFFL) << 40) +
	    ((data[index++] & 0xFFL) << 32) +
	    ((data[index++] & 0xFFL) << 24) +
	    ((data[index++] & 0xFFL) << 16) +
	    ((data[index++] & 0xFFL) <<  8) +
	    (data[index++] & 0xFFL);
	this.index = index;
	return l;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param s A short to add
     */
    protected final synchronized void addShort(short s) {
	byte[] data = this.data;
	int index = this.index;

	data[index++] = (byte)'s';
	data[index++] = (byte)(s >>  8);
	data[index++] = (byte)s;
	this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @return A short
     */
    protected final short getShort() {
	byte[] data = this.data;
	int index = this.index;
	if(data[index] != 's') {
	    throw new RuntimeException("No short found");
	}

	index++;
	short s = (short)(((data[index++] & 0xFF) <<  8) +
			  (data[index++] & 0xFF));
	this.index = index;
	return s;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param f A float to add
     */
    protected final synchronized void addFloat(float f) {
	byte[] data = this.data;
	int index = this.index;
	int i = Float.floatToIntBits(f);

	data[index++] = (byte)'f';
	data[index++] = (byte)(i >> 24);
	data[index++] = (byte)(i >> 16);
	data[index++] = (byte)(i >>  8);
	data[index++] = (byte)i;
	this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @return A float
     */
    protected final synchronized float getFloat() {
	byte[] data = this.data;
	int index = this.index;
	if(data[index] != 'f') {
	    throw new RuntimeException("No float found");
	}

	index++;
	int i = ((data[index++] & 0xFF) << 24) +
            ((data[index++] & 0xFF) << 16) +
            ((data[index++] & 0xFF) <<  8) +
	    (data[index++] & 0xFF);
	float f = Float.intBitsToFloat(i);
	this.index = index;
	return f;
    }

    /**
     * Adds the given value to the buffer.
     * <p>
     * This method should be called from prepareData() to write a variable to
     * the send buffer for sending the message over the network.
     * <p>
     * @param d A double to add
     */
    protected final synchronized void addDouble(double d) {
	byte[] data = this.data;
	int index = this.index;
	long l = Double.doubleToLongBits(d);

	data[index++] = (byte)'d';
	data[index++] = (byte)(l >> 56);
	data[index++] = (byte)(l >> 48);
	data[index++] = (byte)(l >> 40);
	data[index++] = (byte)(l >> 32);
	data[index++] = (byte)(l >> 24);
	data[index++] = (byte)(l >> 16);
	data[index++] = (byte)(l >>  8);
	data[index++] = (byte)l;
	this.index = index;
    }

    /**
     * Returns the next value from the buffer.
     * <p>
     * This method should be called from readData() to read a variable from the
     * receive buffer.
     * <p>
     * @return A double
     */
    protected final synchronized  double getDouble() {
	byte[] data = this.data;
	int index = this.index;
	if(data[index] != 'd') {
	    throw new RuntimeException("No double found");
	}

	index++;
	long l = ((data[index++] & 0xFFL) << 56) +
	    ((data[index++] & 0xFFL) << 48) +
	    ((data[index++] & 0xFFL) << 40) +
	    ((data[index++] & 0xFFL) << 32) +
	    ((data[index++] & 0xFFL) << 24) +
	    ((data[index++] & 0xFFL) << 16) +
	    ((data[index++] & 0xFFL) <<  8) +
	    (data[index++] & 0xFFL);
	double d = Double.longBitsToDouble(l);
	this.index = index;
	return d;
    }

    /**
     * A helper method that extracts the message type from the first two (2) bytes
     * of a byte array.
     * <p>
     * This method is for instance used to determine the message type of a message
     * received on the network.
     * <p>
     * @param A byte array containing a message in serialized form
     * @return The numerical message type
     */
    public static char getType(byte[] data) {
	return (char)(((data[0] & 0xFF) <<  8) + (data[1] & 0xFF));
    }

    /**
     * This method reads the reliable broadcast sequence number.
     *
     * @return The reliable broadcast sequence number */
    public int getReliableSeqNbr(){
	return reliableSeqNbr;
    }

    /**
     * Set the reliable broadcast sequence number. Should only be called by the ReliableLayer.
     * If the system is runnig without the ReliableLayer, the reliableSeqNbr is set to -1.
     * <p>
     * @param The (int) number to set for the reliableSeyNbr in this message.
     */
    public void setReliableSeqNbr(int reliableSeqNbr) {
	if(reliableSeqNbr < 0 || reliableSeqNbr > 0xFFFF)
	    throw new RuntimeException("The sequence number must be > 0 and < 0xFFFF");
	this.reliableSeqNbr = reliableSeqNbr;
    }
  
    //FIXME
    /** used by simulator only, same as toString, but cannot be overloaded by subclasses <p>
     * <b>Warning </b> : DO NOT USE<p>
     * This method was aded for the simulator, because sublcasses overload toSrting
     * whithout printing the header.
     * This is a temporary solution, until a unified pretty printing/logging
     * solution is developped.
     * The method is used only in the constructor of 
     * ch.epfl.lsr.adhoc.simulator.events.AbstractMessageEvent
     */
    public final String headerToString(){
	return "type=\""+(int)msgType+"\" seqNum=\""+sequenceNumber+"\" from=\""+srcNode+"\" to=\""+dstNode+"\" TTL=\""+ttl+"\"";
    }
  
    public String toString() {
	return "type="+(int)msgType+", seqNum="+sequenceNumber+" from "+srcNode+" to "+dstNode+", TTL="+ttl;
    }

}
