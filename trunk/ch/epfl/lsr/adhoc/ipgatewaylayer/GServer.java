//
//  GServer.java
//  
//
//  Created by Dominique Tschopp on Wed Dec 17 2003.
//  
//
package ch.epfl.lsr.adhoc.ipgatewaylayer;

import ch.epfl.lsr.adhoc.runtime.FrancThread;
import java.util.*;
import java.net.*;
import java.io.*;

/**
* A GServer is an application responsible for creating GatewayServer objects
 *
 * <p>
 * @see WANServer
 * @see IPG
 * @see Gateway
 * @see GatewayServer
 * @see Connect
 * @see Connection
 * @see ReadStream
 * @author Dominique Tschopp
 */

public class GServer{
	public static void main(String args[]){
		GatewayServer gs=new GatewayServer();
		gs.start();
	}
}
/**
* A gateways Server is multithreaded and orchestrates communications between gateway nodes. It is contacted by a gateway node
 * at startup and when there is a crash. It maintains a list of gateway nodes.
 * <p>
 *
 * @see WANServer
 * @see IPG
 * @see Gateway
 * @see GServer
 * @see Connect
 * @see Connection
 * @see ReadStream
 * @author Dominique Tschopp
 */

class GatewayServer extends FrancThread{
	/**vector containing the gateways*/
	private Vector gateways=new Vector();
	/**socket for connections from gateways*/
	private ServerSocket sock=null;
	/**IP address of the server*/
	private InetAddress WI;
	/**
		* Constructor.
	 * <p>
	 */
	public GatewayServer(){
		try{
			WI=InetAddress.getLocalHost();
		}
		catch(UnknownHostException exc)
    {
			System.err.println(exc);
    }
	}
	/**
		* Add a new gateway to the list of gateways.
	 * <p>
	 * @param gw The IP address for the new node
	 * @param id The nodeID for the new node
	 */
	public int addGateway(String gw, String id){
		boolean nothere=true;
		//check whether the new gateway is already there, if yes update id, else add it to list.
		for(int counter=0;counter<gateways.size();counter++){
			Gateway n=(Gateway)gateways.elementAt(counter);
			String IP=n.getIa();
			int present=IP.compareTo(gw);
			if(present==0){
				if(n.getId()!=id)
					n.setId(id);
				nothere=false;
				counter=gateways.size()+1;
			}
		}
		if(nothere){
			Gateway nadd=new Gateway(gw,id);
			gateways.add(nadd);
		}
		return gateways.size();
	}
	/**
		* Remove a gateway from the list of gateways.
	 * <p>
	 * @param gw The IP address for the node to be removed
	 */
	public void removeGateway(String gw){
		//check if the gateway is present, if yes remove it
		for(int counter=0;counter<gateways.size();counter++){
			Gateway n=(Gateway)gateways.elementAt(counter);
			String IP=n.getIa();
			int present=IP.compareTo(gw);
			if(present==0){
				gateways.removeElement(n);
				counter=gateways.size()+1;
			}
		}
	}
	/**
		* Get the vector containing the list of gateways.
	 * <p>
	 */
	public Vector getGateways(){
		return gateways;
	}
	/**
		*Thread waiting for new connections 
	 */
	public void run(){
		try {
			// establish the socket
			int port=10000;
			sock = new ServerSocket(port,0,WI);
			
			System.out.println("Gateway Server listenning at port: "+port+" IP: "+WI.getHostAddress());
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
				Connect c = new Connect(client,this);
				c.start();
			}
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
* This class handles a connection picked up by a gateway server
 * 
 * <p>
 * @see WANServer
 * @see IPG
 * @see Gateway
 * @see GServer
 * @see GatewayServer
 * @see Connection
 * @see ReadStream
 * @author Dominique Tschopp
 */


class Connect extends FrancThread
{
	/**the socket for the treated connection*/
	private Socket client;
	/**the gateway server which picked up the connection*/
	private GatewayServer gs;
	/**
		* Constructor
	 * <p>
	 * @param c The socket for the connection
	 * @param gs The gateway server which picked up the connection
	 */
    
	public Connect(Socket c, GatewayServer gs) {
		this.client = c;
		this.gs=gs;
	} 
	
	/**
		* this method is invoked as a separate thread
	 */
	public void run() {
		BufferedInputStream netin = null;
		BufferedOutputStream netout = null;
		String srx,stx,sng,srdy;
		int iRX,iReady,j,k;
		try {
			/**
			* get the input and output streams associated with the socket.
			 */
			netin = new BufferedInputStream(client.getInputStream());
			netout = new BufferedOutputStream(client.getOutputStream());
			
			//connection from a gateway server 0:add, 1:remove  
			byte[] bufRX=new byte[32];                                       
			iRX=netin.read(bufRX);
			srx=new String(bufRX);
			String[] ipn=(srx.trim()).split(":");
			
			switch(Integer.parseInt(ipn[0])){
				case 0:
					System.out.println("adding a Gateway");
					j=gs.addGateway(ipn[1],ipn[2]);
					Vector V=gs.getGateways();
					
					Integer ng=new Integer(j);
					sng=ng.toString();
					netout.write(sng.getBytes(),0,sng.getBytes().length);
					netout.flush();
					
					byte[] ready=new byte[5];                                       
					iReady=netin.read(ready);
					srdy=new String(ready);
					
					for(int counter=0;counter<j;counter++){
                        Gateway n=(Gateway)V.elementAt(counter);
                        stx=n.getIa()+":"+n.getId()+":";
						int length=stx.length();
						String nada="xxxxxxxxxxxxxxxxxxxxxxxxxxxxx/";
						stx=stx.concat(nada.substring(length,30));
						
                        netout.write(stx.getBytes(),0,30);
					}
                        break;
					
				case 1:
					System.out.println("removing a Gateway"+ipn[1]);
					gs.removeGateway(ipn[1]);
					break;
			}
			//print gateways
			Vector Vec=gs.getGateways();
			for(int counter=0;counter<Vec.size();counter++){
				Gateway n=(Gateway)Vec.elementAt(counter);
				String s="address "+n.getIa()+"  -  ID "+n.getId()+"  "+"\n";
				System.out.print(s);
			}
			
			
			netout.flush();
		}
		catch (IOException ioe) {
			System.err.println(ioe);
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

