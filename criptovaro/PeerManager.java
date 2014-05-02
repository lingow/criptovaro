package criptovaro;

import java.net.InetAddress;

import java.util.Collection;
import java.util.PriorityQueue;

public class PeerManager {
    /**
     * This attribute makes the PeerManager a Singleton.
     */
    public static final PeerManager INSTANCE = new PeerManager();
    private PriorityQueue<Peer> peerQueue;

    /**
     * Because this class is a singleton, the constructor should not be called directly. That's why it's private.
     * Instead, to get the instance use PeerManager.INSTANCE;
     */
    private PeerManager() {
    }

    public void AddPeer(Peer p) {
    }

    public void deletePeer(Peer p) {
    }

    public void updatePeer(Peer p) {
    }

    public Peer getPeer(InetAddress ipaddress,int port) {
        return null;
    }

    public Peer[] substractPeers(Peer[] incomingPeerList) {
        return null;
    }

    public void getPeers() {
    }
}
