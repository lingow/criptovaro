package criptovaro;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.Collection;

/**
 * This abstract class provides methods to handle Request messages, which not only send a Message object but wait for
 * a reply from the peer to which the message was sent.
 */
public abstract class Request extends Message {
    @SuppressWarnings("compatibility:4031401296402768935")
    private static final long serialVersionUID = -3022662827348686199L;

    /**
     * This is the ammount of miliseconds a Request will wait for a reply until it timesout
     */
    protected static final int REQUEST_TIMEOUT = 5000;

    /**
     * expectrequest is used as a way to know if a received request is actually waiting for a reply. If not, just
     * deliver the message without reply.
     */
    private boolean expectrequest = false;

    /**
     * expectedClass specifies a Message subclass that is being waited for. If the recieved reply doesn't match this
     * class, there was some error so we can't trust the peer who sent it.
     */
    private Class expectedClass = Message.class;

    /**
     * This constructor should only be called by subclasses. It's very important to call it because otherwise the
     * expected class won't be set
     * @param c The expected Message class that is expected as a reply for this request
     */
    protected Request(Class c) {
        this.expectedClass = c;
    }

    /**
     * This methods sends this message to target peer, waits for a reply and delivers it if it matches the expectedclass
     * @param peer The peer to which the request will be made
     * @throws IOException
     */
    public void request(Peer peer) throws IOException {
        Socket socket;
        Message m = null;

        try {
            socket = peer.createSocket();
            this.expectrequest = true;
            this.send(peer, socket);
            m = this.getReply(peer, socket, REQUEST_TIMEOUT);
            if (m != null)
                if (this.expectedClass == m.getClass())
                    m.receive(peer, socket);
            socket.close();
        } catch (ConnectException ce) {
            PeerManager.INSTANCE.deletePeer(peer);
        }
    }

    /**
     * This method is called either by the listener who obtains the request or by the message which receives a reply, 
     * which can be a request on its own. It delivers the message and generates a reply if it expects one.
     * 
     * @param p the peer who sent this request and to which we must send a reply
     * @param s the open socket by which the peer sent the request and by which we should send the reply
     */
    @Override
    public void receive(Peer p, Socket s) {
        if (!this.deliver(p))
            PeerManager.INSTANCE.deletePeer(p);
        if (this.expectrequest)
            try {
                this.generateReply(p).send(p, s);
            } catch (IOException e) {
                PeerManager.INSTANCE.deletePeer(p);
            }
    }

    /**
     * This method should be implemented by the Extenders of Request. It generates the reply for current message.
     * @param p The peer for which we should generate the reply
     * @return The message that should be sent as a reply
     */
    protected abstract Message generateReply(Peer p);

    /**
     * This method waits for timeout milliseconds to receive a reply from peer sent by the specified socket.
     * @param peer The peer for which we will wait for a reply
     * @param socket The open socket by which the reply will be read. This socket should be open before invoking this 
     * function
     * @param timeout The time in milliseconds for which we will wait for the reply
     * @return the reply message sent by the peer, or null if anything went wrong
     * @throws IOException
     */
    private Message getReply(Peer peer, Socket socket, int timeout) throws IOException {
        ObjectInputStream ois = null;
        Message m = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            PeerManager.INSTANCE.deletePeer(peer);
        }
        try {
            m = (Message) ois.readObject();
        } catch (ClassNotFoundException e) {
            PeerManager.INSTANCE.deletePeer(peer);
        }
        return m;
    }
    
    public void bcastRequest(){
        
    }
}
