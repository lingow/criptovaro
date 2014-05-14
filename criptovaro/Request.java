package criptovaro;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.Serializable;

import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class provides methods to handle Request messages, which not only send a Message object but wait for
 * the peer to answer with the requested Serializable Object
 */
public abstract class Request<RequestedClass extends Serializable> extends Message {
    @SuppressWarnings("compatibility:4031401296402768935")
    private static final long serialVersionUID = -3022662827348686199L;

    /**
     * This is the ammount of miliseconds a Request will wait for a reply until it timesout
     */
    protected int request_timeout = 5000;

    /**
     * expectrequest is used as a way to know if a received request is actually waiting for a reply. If not, just
     * deliver the message without reply.
     */
    private boolean expectrequest = false;
    
    protected Request(int t){
        this.request_timeout=t;
    }
    
    protected Request(){
        this.request_timeout=5000;
    }

    /**
     * This methods sends this message to target peer and returns the object provided by the peer
     * @param peer The peer to which the request will be made
     * @return The requested serializable object that the peer provided
     * @throws IOException
     */
    public RequestedClass request(Peer peer){
        Socket socket;
        RequestedClass m = null;

        try {
            socket = peer.createSocket();
            this.expectrequest = true;
            this.send(peer, socket,this);
            m = this.getReply(peer, socket, request_timeout);
            socket.close();
        } catch (Exception ce) {
            PeerManager.INSTANCE.deletePeer(peer);
        }
        return m;
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
                send(p,s,this.generateReply(p));
            } catch (IOException e) {
                PeerManager.INSTANCE.deletePeer(p);
            }
    }

    /**
     * This method should be implemented by the Extenders of Request. It generates the reply for current message.
     * @param p The peer for which we should generate the reply
     * @return The Object that should be sent as a reply
     */
    protected abstract RequestedClass generateReply(Peer p);

    /**
     * This method waits for timeout milliseconds to receive a reply from peer sent by the specified socket.
     * @param peer The peer for which we will wait for a reply
     * @param socket The open socket by which the reply will be read. This socket should be open before invoking this 
     * function
     * @param timeout The time in milliseconds for which we will wait for the reply
     * @return the Object sent by the peer, or null if anything went wrong
     * @throws IOException
     */
    private RequestedClass getReply(Peer peer, Socket socket, int timeout) throws IOException {
        ObjectInputStream ois = null;
        RequestedClass m = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            PeerManager.INSTANCE.deletePeer(peer);
        }
        try {
            m = (RequestedClass) ois.readObject();
        } catch (ClassNotFoundException e) {
            PeerManager.INSTANCE.deletePeer(peer);
        }
        return m;
    }

    /**
     * This method sends a request to every known peer in the PeerManager and waits for them to reply. It does so in a 
     * separate thread for each peer. It gathers all replies into a Collection.
     * @return The collection of objects that the peers provided
     */
    public Collection<RequestedClass> bcastRequest(){
        Collection<RequestedClass> ret = Collections.synchronizedCollection( new ArrayList<RequestedClass>());
        HashMap<Peer,Thread> threads = new HashMap<Peer,Thread>();
        for (Peer p: PeerManager.INSTANCE.getPeers()){
            Thread t = new Thread(new Gatherer(this,ret,p));
            threads.put(p,t);
            t.start();
        }
        for (Map.Entry<Peer,Thread> entry: threads.entrySet()){
            try {
                entry.getValue().join(request_timeout);
            } catch (InterruptedException e) {
                PeerManager.INSTANCE.deletePeer(entry.getKey());
            }
        }
        return ret;
    }
}
