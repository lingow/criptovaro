package criptovaro;

import java.net.InetAddress;

public class CriptoVNetwork {
    private InetAddress ipAddress;
    private int port;

    public boolean pingPeer(Peer remoteMiner) {
        return false;
    }

    public Peer[] bcastGetPeers() {
        return null;
    }

    public void bcastJoin() {
    }

    public InetAddress getIPAddress() {
        return null;
    }

    private void setIPAddress() {
    }

    private void init() {
    }

    public void ucastPeerList(Peer remoteMiner) {
    }

    public void bcastStat(Peer currentMinerStat) {
    }

    public Block ucastGetBlock(byte[] blockHash) {
        return null;
    }

    public Block ucastGetPrevBlock(byte[] referenceBlockHash) {
        return null;
    }

    public Block getNextBlock(byte[] referenceBlockHash) {
        return null;
    }
}
