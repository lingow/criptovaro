package criptovaro;

import java.math.BigDecimal;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TransactionPool {

    ConcurrentLinkedDeque<Transaction> pool;
    public TransactionPool(){
        super();
        pool = new ConcurrentLinkedDeque<Transaction>();
    }
    public void addTransaction(Transaction tran) {
        pool.add(tran);
    }

    public void addTransactionList(Collection<Transaction> transactions) {
        pool.addAll(transactions);
    }

    public Transaction consumeTransaction() {
        return pool.remove();
    }

    public int getPoolLength() {
        return pool.size();
    }

    public void removeIfExist(Collection<Transaction> transaction) 
    {
    
        pool.removeFirstOccurrence(transaction);
    }
    
    public ArrayList<Transaction> getAllTransactions()
    {
        return new ArrayList(pool);    
    }
}
