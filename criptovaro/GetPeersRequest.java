package criptovaro;

public class GetPeersRequest extends Request {
    @SuppressWarnings("compatibility:-1142199987874023273")
    private static final long serialVersionUID = 71876723107862001L;

    public GetPeersRequest(){
        super(PeerListMessage.class);
    }
    
    @Override
    protected Message generateReply(Peer p) {
        return new PeerListMessage(PeerManager.INSTANCE.getPeers());
    }

    @Override
    protected boolean deliver(Peer peer) {
        return true;
    }
}
