package criptovaro;

public class StatMessage extends Message{
    @SuppressWarnings("compatibility:-4273379731594610614")
    private static final long serialVersionUID = -6570888968721497852L;
    private byte[] hash;
    private int lenght;

    StatMessage(byte[] hash, int lenght) {
        this.hash=hash;
        this.lenght = lenght;
    }

    @Override
    protected boolean deliver(Peer peer) {
        peer.setHash(hash);
        peer.setlenght(lenght);
        PeerManager.INSTANCE.addPeer(peer);
        Miner.INSTANCE.receivedStat(peer);
        return true;
    }
}
