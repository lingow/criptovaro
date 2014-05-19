package criptovaro;


import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

public class PeerManager {
    /**
     * This attribute makes the PeerManager a Singleton.
     */
    public static final PeerManager INSTANCE = new PeerManager();
    private Map<InetSocketAddress,Peer> peerCache;

    /**
     * Because this class is a singleton, the constructor should not be called directly. That's why it's private.
     * Instead, to get the instance use PeerManager.INSTANCE;
     */
    private PeerManager() 
    {
    }

    /**
     * Adds a peer to the peer cache or updates it with new information if it's already there. New peers will be
     * added to the ledger as well.
     * Lastly, that peer is pinged to make sure we can reach him.
     * @param p the per to add to the peerCache
     */
    public void addPeer(Peer p) {
        if ( peerCache.containsKey(p.getKey())){
            peerCache.get(p.getKey()).updateTo(p);
        } else {
            peerCache.put(p.getKey(), p);
            Ledger.INSTANCE.q_InsertPeer(p);
        }
        // Al finalizar, hacerle ping al Peer.
        (new PingMessage()).send(p);
    }

    /**
     * Deletes a peer from the Cache as well as from the Ledger.
     * @param p the peer to delete
     * @return true if the peer was indeed deleted. False if the peer was not found in the peerCache
     */
    public boolean deletePeer(Peer p) {
        if (peerCache.containsKey(p.getKey())){
            peerCache.remove(p.getKey());
            Ledger.INSTANCE.q_DeletePeer(p);
            return true;
        }
        return false;
    }

    /**
     * Because PeerManager is a Singleton, it is instantiated since loading the JVM. However, it might not be
     * initialized correctly since then. Invoke this method to initialize it.
     * It Does the following:
     * - Read the Ledger to get the Peer List from last session
     * - Advertize to those Peers our own presence and figure out which of those are still online.
     * - Get the PeerLists of active Peers to discover new Peers
     * - Add every Peer discovered this way to the PeerQueue (This is done as requested peerlists arrive)
     */
    public void init() {
        Miner.LOG.log(Level.FINE,"Initializing PeerManager");
        peerCache= Collections.synchronizedMap(new HashMap<InetSocketAddress,Peer>());
        for(Peer p : Ledger.INSTANCE.q_PeerList()){
            addPeer(p);
        }
        for(ArrayList<Peer> arr : (new GetPeersRequest()).bcastRequest()){
            if( arr != null){
                for (Peer p: arr){
                    if ( p != null){
                        addPeer(p);
                    }
                }
            }
        }
        Miner.LOG.log(Level.FINE,"Successfully initialized PeerManager");
    }

    /**
     * Retrieves the collection of known peers.
     * @return a collection of all the peers in the peerCache
     */
    public Collection<Peer> getPeers() {
        return peerCache.values();
    }

    /**
     * Retrieves a single peer's object in the peer cache
     * @param inetAddress The inetAddress that identifies this peer
     * @param port the port that identifies this peer
     * @return the peer object corresponding to passed parameters or null if it was not found
     */
    Peer getPeer(InetAddress inetAddress, int port) {
        Peer p = peerCache.get(new InetSocketAddress(inetAddress,port));
        if(p == null){
            p = new Peer(inetAddress,port);
            addPeer(p);
        }
        return p;
    }

    /**
     * @return A randomly selected peer from those whith the highest lenght
     */
    public Peer getBestPeer() 
    {
        Peer result = null;
        Random r = new Random();
        
        try 
        {
            result = new Peer(InetAddress.getLocalHost(), -1);
           
            for(Peer p : peerCache.values())
            {
                if(p.getLength() >= result.getLength())
                {
                    if(r.nextInt(1) == 1 || result.getLength() == -1)
                    {
                        result = p;
                    }
                }
            }
        } 
        catch (UnknownHostException e) 
        {
            Miner.LOG.log(Level.INFO, e.toString());
            e.printStackTrace();
        }
        return result;
    }
}
