package criptovaro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class GetPeersRequest extends Request<ArrayList<Peer>> {
    @SuppressWarnings("compatibility:-1142199987874023273")
    private static final long serialVersionUID = 71876723107862001L;
    
    public GetPeersRequest(int t){
        super(t);
    }
    public GetPeersRequest(){
        super();
    }
    @Override
    protected ArrayList<Peer> generateReply(Peer p) {
        return new ArrayList<Peer>(PeerManager.INSTANCE.getPeers());
    }

    @Override
    protected boolean deliver(Peer peer) {
        return true;
    }
}
