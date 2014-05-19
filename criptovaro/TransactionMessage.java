package criptovaro;

import java.util.Arrays;
import java.util.logging.Level;

public class TransactionMessage extends Message{
    @SuppressWarnings("compatibility:-6004287418379900521")
    private static final long serialVersionUID = 4380053598562242947L;
    private Transaction t;
    
    public TransactionMessage(Transaction t){
        this.t=t;
    }

    @Override
    protected boolean deliver(Peer peer) 
    {
        if (t == null)
        {
            Miner.LOG.log(Level.INFO, "TransactionMessage.Deliver - Error. Null transaction. Aborting.");
            return false;
        }
        if (! t.verify())
        {
            Miner.LOG.log(Level.INFO, "TransactionMessage.Deliver - Error - Transaction fails verification. Aborting.");
            return false;
        }
        if ( Arrays.equals(t.getSource(),t.getDestination()))
        {
            Miner.LOG.log(Level.INFO, "TransactionMessage.Deliver - Error - Transaction semantics invalid. Aborting.");
            return false;
        }
        if ( t.getType()!= TransactionType.REGULAR)
        {
            Miner.LOG.log(Level.INFO, "TransactionMessage.Deliver - Error - Only regular transactions are accepted. Aborting.");
            return false;
        }
        Miner.INSTANCE.incomingTransaction(t);
        return true;
    }
}
