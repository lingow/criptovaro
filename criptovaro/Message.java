package criptovaro;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.net.ConnectException;
import java.net.Socket;

import java.net.SocketTimeoutException;

import java.util.Collection;

/**
 * This abstract class provides methods to send, broadcast and receive Messages.
 */
public abstract class Message implements Serializable{
    @SuppressWarnings("compatibility:-894874531013678188")
    private static final long serialVersionUID = 1L;

    /**
     * This Method creates a socket to the passed peer and sends current Message as a serializable object.
     * Make sure to fill in this Message's attributes before sending it.
     * 
     * @param p The peer to which this Message will be sent
     */
    public void send(Peer p){
       try {
            Socket s = p.createSocket();
            this.send(p, s);
            s.close();
        } catch (Exception ce) {
            PeerManager.INSTANCE.deletePeer(p);
        }
    }

    /**
     * This Method is to be called by a listener method whenever this Message is obtained from a socket as a
     * serializable object. It calls this particular kind of message's deliver() method to do whatever is needed.
     * Notice that this Receive method has a Socket parameter which is actually not used, but is keeped for easily
     * override this method by the Request class
     * @param p The peer from which this message came from
     * @param s The open socket by which this message came from
     */
    public void receive(Peer p, @SuppressWarnings("unused") Socket s){
        if ( ! this.deliver(p) )
            PeerManager.INSTANCE.deletePeer(p);
    }

    /**
     * This method invokes the send method for each Peer in the collection
     * @param peers A collection of peers to which this message should be sent.
     */
    public void bcast(){
        for(Peer p: PeerManager.INSTANCE.getPeers()){
                this.send(p);
        }
    }

    /**
     * This method is overriden by every Message subclass. It actually does some action based on the received message.
     * What will be done depends on the particular kind of message.
     * @param peer The peer from which this message came from
     * @return true if the action was done flawlessly. false otherwise.
     */
    protected abstract boolean deliver(Peer peer);

    /**
     * This is a generalized method to send this message to a peer using an already open socket
     * @param p the peer to which this message should be sent
     * @param s the socket by which this message will be sent. It should be open before invoking this method.
     * @throws IOException If the server is not reading from this socket
     */
    protected void send(Peer p, Socket s) throws IOException {
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(this);
    }
}
