package criptovaro;

public class GetPeersRequest extends Request {
    @SuppressWarnings("compatibility:-1142199987874023273")
    private static final long serialVersionUID = 71876723107862001L;

    public GetPeersRequest(){
        super(PeerListMessage.class);
    }
    
    @Override
    protected Message generateReply(Peer p) {
        // TODO Implement this method
        return null;
    }

    @Override
    protected boolean deliver(Peer peer) {
        // TODO Implement this method
        return false;
    }
}
