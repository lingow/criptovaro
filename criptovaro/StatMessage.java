package criptovaro;

public class StatMessage extends Message{
    @SuppressWarnings("compatibility:-4273379731594610614")
    private static final long serialVersionUID = -6570888968721497852L;
    private byte[] hash;
    private long length;

    StatMessage(byte[] hash, long length) {
        this.hash=hash;
        this.length = length;
    }

    @Override
    protected boolean deliver(Peer peer) {
        peer.setHash(hash);
        peer.setLength(length);
        PeerManager.INSTANCE.addPeer(peer);
        Miner.INSTANCE.receivedStat(peer);
        return true;
    }
}
