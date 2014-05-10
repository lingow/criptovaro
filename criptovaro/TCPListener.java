package criptovaro;

import java.io.IOException;

import java.io.ObjectInputStream;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * TCPListener is a Singleton Thread that listens for Socket connections of a specified port.
 * It reads the Message objects and calls their receive method. It also adds peers to the peer cache as it receives 
 * any connections and removes them if any peer is not working the usual way.
 * To run this thread use:
 * 
 * TCPListener.INSTANCE.setPort(port);
 * TCPListener.INSTANCE.setTransactionPool(pool);
 * TCPListener.INSTANCE.start();
 */
public class TCPListener extends Thread{
    private int port = 0;
    private TransactionPool pool = null;
    
    /**
     * This field makes TCPListener a singleton
     */
    public static final TCPListener INSTANCE = new TCPListener();

    /**
     * Because TCPListener, it should not be instantiated through the constructor
     * Use TCPListener.INSTANCE instead. You must set the Port by calling setPort() before starting it though.
     */
    private TCPListener(){
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
                        m.receive(p,socket);
                } catch (ClassNotFoundException e) {
                    PeerManager.INSTANCE.deletePeer(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method sets the listener port. Better use it before calling start() method
     * @param port the port that should be set. 
     * @return true if everything's ok. False if it's an invalid port.
     */
    public boolean setPort(int port){
        this.port=port;
        return true;
    }
    
    public void setTransactionPool(TransactionPool thepool)
    {
        pool = thepool;
    }
}
