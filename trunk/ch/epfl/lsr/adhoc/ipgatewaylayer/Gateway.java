//
//  Gateway.java
//  
//
//  Created by Dominique Tschopp on Wed Dec 17 2003.
//  
//
package ch.epfl.lsr.adhoc.ipgatewaylayer;

import java.io.*;

/**
* This class defines gateway nodes. A gateway node is identified by a quadruplet (nodeID,IPaddress,inputstream, outputstream). 
 * It is used by the Gateway Server 
 * and the Gateway layer to store gateways
 * <p>
 * @see WANServer
 * @see IPG
 * @see GServer
 * @see GatewayServer
 * @see Connect
 * @see Connection
 * @see ReadStream
 * @author Dominique Tschopp
 */

public class Gateway{
	/** the node ID*/
	private String id;
	/** the ip address*/
	private String ia;
	/** the outputstream to send a message to this node*/
	private BufferedOutputStream netout;
	/** the inputstream to receive a message from this node*/
	private BufferedInputStream netin;
	
	/**
		* Constructor.
	 * <p>
	 * @param ia The IP address for this node
	 * @param id The nodeID for this node
	 */
	public Gateway(String ia, String id){
		this.id=id;
		this.ia=ia;
		this.netout=null;
		this.netin=null;
	}
	/**
		* Constructor.
	 * <p>
	 * @param ia The IP address for this node
	 * @param id The nodeID for this node
	 * @param netout The output stream for this node
	 * @param netin The input stream for this node
	 */
	
	public Gateway(String ia, String id,BufferedOutputStream netout,BufferedInputStream netin){
		this.id=id;
		this.ia=ia;
		this.netout=netout;
		this.netin=netin;
	}
	/**
		* Set node ID.
	 * <p>
	 * @param id The nodeID for this node
	 */
	public void setId(String id){
		this.id=id;
	}
	/**
		* Get the nodeID for this node.
	 * <p>
	 */
	public String getId(){
		return this.id;
	}
	/**
		* Get the IP address for this node.
	 * <p>
	 */
	public String getIa(){
		return this.ia;
	}
	/**
		* Get the outputstream for this node.
	 * <p>
	 */
	public BufferedOutputStream getOut(){
		return this.netout;
	}
	/**
		* Get the inputstream for this node.
	 * <p>
	 */
	public BufferedInputStream getIn(){
		return this.netin;
	}
	/**
		* Set the inputstream for this node.
	 * <p>
	 * @param out The Output Stream for this node
	 */
	public void setOut(BufferedOutputStream out){
		this.netout=out;
	}
	/**
		* Set the outputstream for this node.
	 * <p>
	 * @param in The input Stream for this node
	 */
	public void setIn(BufferedInputStream in){
		this.netin=in;
	}
}