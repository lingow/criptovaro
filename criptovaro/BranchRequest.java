package criptovaro;

import java.util.LinkedHashMap;

public class BranchRequest extends Request<LinkedHashMap<byte[],Integer>>{
    @SuppressWarnings("compatibility:8423995755533858870")
    private static final long serialVersionUID = 1L;
    private byte[] hash;
    private int lenght;

    public BranchRequest(byte[] hash, int lenght) {
        this.hash=hash;
        this.lenght=lenght;
    }

    @Override
    protected LinkedHashMap<byte[], Integer> generateReply(Peer p) {
        return Miner.INSTANCE.getChainBranch(hash,lenght);
    }

    @Override
    protected boolean deliver(Peer peer) {
        // TODO Implement this method
        return true;
    }
}
