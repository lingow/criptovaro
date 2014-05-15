package criptovaro;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TransactionPool extends ConcurrentLinkedDeque<Transaction> {

    public TransactionPool(){
        super();
    }
    public void addTransaction(Transaction tran) {
        this.add(tran);
    }

    public void addTransactionList(Collection<Transaction> transactions) {
        this.addAll(transactions);
    }

    public Transaction consumeTransaction() {
        return this.remove();
    }

    public int getPoolLength() {
        return this.size();
    }

    public void removeIfExist(Collection<Transaction> transaction) 
    {
    
        this.removeFirstOccurrence(transaction);
    }
    
    public ArrayList<Transaction> getAllTransactions()
    {
        return null;    
    }
}
