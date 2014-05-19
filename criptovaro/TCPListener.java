package criptovaro;

import java.io.IOException;

import java.io.ObjectInputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.logging.Level;

/**
 * TCPListener is a Thread that listens for Socket connections of a specified port.
 * It reads the Message objects and calls their receive method. It also adds peers to the peer cache as it receives
 * any connections and removes them if any peer is not working the usual way.
 * To run this thread use:
 *
 * listener = new TCPListener(port)
 * listener.start();
 */
public class TCPListener extends Thread{
    private int port = 0;

    /**
     * Create a tcp listener ready to listen for the provided port
     * @param port
     */
    public TCPListener(int port){
        this.port=port;
    }

    /**
     * This is the main method that will run in the TCPListener Thread. It initiates a ServerSocket and accepts
     * connections in a loop. It then reads Messages through sockets and calls the Message's receive method.
     */
    public void run() {
        ServerSocket ss = null;
        Socket socket = null;
        Peer p = null;
        Message m = null;
        try {
            ss = new ServerSocket(port);
            while(true){
                try {
                    socket = ss.accept();
                    p = PeerManager.INSTANCE.getPeer(socket.getInetAddress(), socket.getPort());
                    ObjectInputStream ois = 
                         new ObjectInputStream(socket.getInputStream());
                    m = (Message) ois.readObject();
                    if (p != null && m != null)
                    {
                        Miner.LOG.log(Level.INFO, "Incoming connection, Receiving new message.");
                        m.receive(p,socket);
                    }
                } catch (ClassNotFoundException e) {
                    PeerManager.INSTANCE.deletePeer(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
