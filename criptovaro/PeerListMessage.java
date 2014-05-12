package criptovaro;

import java.util.Collection;

public class PeerListMessage extends Message{
    @SuppressWarnings("compatibility:-2121510388819315247")
    private static final long serialVersionUID = -3211186589049456559L;
    Collection<Peer> peers;
    

    PeerListMessage(Collection<Peer> peers) {
        super();
        this.peers=peers;
    }

    @Override
    protected boolean deliver(Peer peer) {
        for(Peer p : peers){
            if ( !p.equals(peer) )
                PeerManager.INSTANCE.addPeer(p);
        }
        return true;
    }
}
