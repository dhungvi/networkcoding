package ch.epfl.lsr.adhoc.ipgatewaylayer;
//
//  WANServer.java
//  
//
//  Created by Dominique Tschopp on Thu Oct 30 2003.
//  
//

import ch.epfl.lsr.adhoc.runtime.Message;
import ch.epfl.lsr.adhoc.runtime.FrancThread;

import java.util.*;
import java.net.*;
import java.io.*;

/**
* This class defines a multithreaded server which accepts TCP connections from distant gateways. 
 * Always attached to a gateway node (IPG)
 * <p>
 *
 * @see Connect
 * @see IPG
 * @see Gateway
 * @see GServer
 * @see GatewayServer
 * @see Connection
 * @see ReadStream
 * @author Dominique Tschopp
 */


public class WANServer extends FrancThread {
	/**The socket accepting connections from other gateways*/
	private ServerSocket sock=null;
	/**The IP address for the server*/
	private InetAddress WI;
	/**The IPG this WANServer is attached to*/
	private IPG ipg;
	/**The maximum size for a message*/
	private int maxSize;
	
	
	/**
		* Constructor
	 * <p>
	 * @param ipg The IPG to which the WANServer is attached
	 * @param WI The IP address for this server
	 * @param maxSize The maximum size for a message
	 */
    
	
	public WANServer (IPG ipg, InetAddress WI,int maxSize){
		this.WI=WI;
		this.ipg=ipg;
		this.maxSize=maxSize;
	}
	/**
		* this method is invoked as a separate thread
	 */
	public void run(){
		try {
			// establish the socket
			int port=9999;
			
			sock = new ServerSocket(port,0,WI);
			/**
				* listen for new connection requests.
			 * when a request arrives, pass the socket to
			 * a separate thread and resume listening for
			 * more requests. 
			 */
			while (true) {
				// now listen for connections
				Socket client = sock.accept();	
				
				// service the connection in a separate thread
				Connection c = new Connection(client,ipg,maxSize);
				c.start();
			}
		}
		catch(java.net.BindException be){
			System.out.println("***********************************************");
			System.out.println("the ip address chosen for your gateway is already in use, please select another one");
			System.out.println("***********************************************");
			System.exit(0);
		}
		catch (IOException ioe) {
			System.err.println(ioe);
		}
		finally {
			try{
				if (sock != null)
					sock.close();
			}
			catch(IOException ce){
				System.err.println(ce);
			}
		}
	}
}
/**
* This class handles a connection picked up by a WANServer
 * <p>
 *
 * @see WANServer
 * @see IPG
 * @see Gateway
 * @see GServer
 * @see GatewayServer
 * @see Connect
 * @see ReadStream
 * @author Dominique Tschopp
 */

class Connection extends FrancThread
{
	/**the socket for the treated connection*/
	private Socket client;
	/**a message*/
	private Message msg;
	/**the IPG this connection was established to*/
	private IPG ipg;
	/**a buffer to receive messages*/
	private byte[] bufRX;
	/**the maximum size for the buffer above*/
	private int maxSize;
	
	/**
		* Constructor
	 * <p>
	 * @param c The socket for the connection
	 * @param ipg The IPG to which the WANServer sending the connection is attached to
	 * @param maxSize The maximum size for a message
	 */
    
	
	public Connection(Socket c,IPG ipg,int maxSize) {
		client = c;
		this.ipg=ipg;
		this.maxSize=maxSize;
		bufRX=new byte[maxSize];
	} 
	
	/**
		* this method is invoked as a separate thread
	 */
	public void run() {
		BufferedInputStream netin = null;
		BufferedOutputStream netout = null;
		Random r=new Random();
		InetAddress source=null;
		int p=40000+r.nextInt(9999);		
		try {
			/**
			* get the input and output streams associated with the socket.
			 */
			netin = new BufferedInputStream(client.getInputStream());
			netout = new BufferedOutputStream(client.getOutputStream());
			BufferedOutputStream toGate=null;
			
			source=client.getInetAddress();
			boolean yes;
			
			while(true){
				yes=true;
				int length=netin.read(bufRX,0,maxSize);
				Long NodeID=ipg.byteToMsg(bufRX,(short)length);
				String Node=NodeID.toString();
				
				Vector gways=ipg.getgways();
				for(int v=0;v<gways.size() &&(yes);v++){
					if(Node.compareTo(((Gateway)gways.elementAt(v)).getId())==0)
						yes=false;
				}
				if(yes){
					int numgate=ipg.addGateway(source.getHostAddress(),Node,netout,netin);
				}
			}
			
		}
		catch (Exception ioe) {
			ipg.reportFailure(source.getHostAddress());
		}
		finally {
			try {
				if (netin != null)
					netin.close();
				if (netout != null)
					netout.close();
				if (client != null)
					client.close();
			}
			catch (IOException ioee) {
				System.err.println(ioee);
			}
		}
	}
	
}
