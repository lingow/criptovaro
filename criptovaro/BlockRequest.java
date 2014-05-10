package criptovaro;

public class BlockRequest extends Request{
    @SuppressWarnings("compatibility:-8989288114126158226")
    private static final long serialVersionUID = 1829288835774382467L;

    public BlockRequest(){
        super(BlockMessage.class);
        
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
