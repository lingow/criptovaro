package criptovaro;

public class BlockRequest extends Request<Block>{
    @SuppressWarnings("compatibility:-8989288114126158226")
    private static final long serialVersionUID = 1829288835774382467L;
    private byte[] hash;
    private int lenght;

    public BlockRequest(byte[] bs, int i){
        super();
        this.hash=bs;
        this.lenght = i;
    }
    public BlockRequest(byte[] bs, int i, int t){
        super(t);
        this.hash=bs;
        this.lenght = i;
    }

    @Override
    protected Block generateReply(Peer p) {
        return Miner.INSTANCE.getNextBlock(hash, lenght);
    }

    @Override
    protected boolean deliver(Peer peer) {
        // TODO Implement this method
        return true;
    }
}
