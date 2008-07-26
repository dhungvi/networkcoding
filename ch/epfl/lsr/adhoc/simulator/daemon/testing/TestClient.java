package ch.epfl.lsr.adhoc.simulator.daemon.testing;

import java.io.IOException;
//import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ch.epfl.lsr.adhoc.simulator.daemon.protocol.*;

public class TestClient {

	public static void main(String[] args) {
		try {
			Socket m_socket = new Socket("127.0.0.1", 9999);
			System.out.println("Already Connected!");
			ObjectOutputStream oos = new ObjectOutputStream(m_socket.getOutputStream());
			//oos.writeObject(new CreateSessionMsg());
			SentNodeMsg snm = new SentNodeMsg(1);
			//oos.writeObject(snm);
			//ObjectInputStream ois = new ObjectInputStream(m_socket.getInputStream());
			//Object o = ois.readObject();
			//if (o instanceof AckMsg){
			//	System.out.println("BIG SUCCESS");
			//}else{
			//	System.out.println("Crap!");
			//}
			while (true){}
			//m_socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private static Document getFromFile(String file) throws JDOMException, IOException{
		SAXBuilder m_builder = new SAXBuilder();
		m_builder.setValidation(true);
		return m_builder.build(file);
	}
}
