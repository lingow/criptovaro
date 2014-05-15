package criptovaro;

import java.util.Arrays;

public class TransactionMessage extends Message{
    @SuppressWarnings("compatibility:-6004287418379900521")
    private static final long serialVersionUID = 4380053598562242947L;
    private Transaction t;
    
    public TransactionMessage(Transaction t){
        this.t=t;
    }

    @Override
    protected boolean deliver(Peer peer) {
        if (t == null)
            return false;
        if (! t.verify())
            return false;
        if ( Arrays.equals(t.getSource(),t.getDestination()))
            return false;
        if ( t.getType()!= TransactionType.REGULAR)
            return false;
        Miner.INSTANCE.incomingTransaction(t);
        return true;
    }
}
