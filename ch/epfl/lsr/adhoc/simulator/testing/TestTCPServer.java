/*
 * Created on Dec 29, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.epfl.lsr.adhoc.simulator.testing;

import java.io.*;
import java.net.*;

/**
 * @author Boris
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestTCPServer {
    public static void main(String[] args) {
        new TcpServer().start();
        new TcpClient().start();

        while (true) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Running!");
        }
    }
}

class TcpServer extends Thread {
    public void run() {

        int port = 1500;
        ServerSocket server_socket;
        BufferedReader input;
        try {

            server_socket = new ServerSocket(port);
            System.out.println(
                "Server waiting for client on port "
                    + server_socket.getLocalPort());

            // server infinite loop
            while (true) {
                Socket socket = server_socket.accept();
                System.out.println(
                    "New connection accepted "
                        + socket.getInetAddress()
                        + ":"
                        + socket.getPort());
                input =
                    new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                // print received data 
                try {
                    while (true) {
                        String message = input.readLine();
                        if (message == null)
                            break;
                        System.out.println(message);
                    }
                }
                catch (IOException e) {
                    System.out.println(e);
                }

                // connection closed by client
                try {
                    socket.close();
                    System.out.println("Connection closed by client");
                }
                catch (IOException e) {
                    System.out.println(e);
                }

            }

        }

        catch (IOException e) {
            System.out.println(e);
        }
    }
}
class TcpClient extends Thread {

    public void run() {

        int port = 1500;
        String server = "localhost";
        Socket socket = null;
        String lineToBeSent;
        BufferedReader input;
        PrintWriter output;
        int ERROR = 1;

        // connect to server
        try {
            socket = new Socket(server, port);
            System.out.println(
                "Connected with server "
                    + socket.getInetAddress()
                    + ":"
                    + socket.getPort());
        }
        catch (UnknownHostException e) {
            System.out.println(e);
            System.exit(ERROR);
        }
        catch (IOException e) {
            System.out.println(e);
            System.exit(ERROR);
        }

        try {
            input = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintWriter(socket.getOutputStream(), true);

            // get user input and transmit it to server
            while (true) {
                lineToBeSent = input.readLine();
                // stop if input line is "."
                if (lineToBeSent.equals("."))
                    break;
                output.println(lineToBeSent);
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }

        try {
            socket.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
