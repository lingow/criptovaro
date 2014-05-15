package criptovaro;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;

public class TransactionPool {
    private Set<Transaction> pool;

    public synchronized void addTransaction(Transaction tran) {
    }

    public synchronized void addTransactionList(Collection<Transaction> transactions) {
    }

    public synchronized Transaction consumeTransaction() {
        return null;
    }

    public synchronized void deleteTransaction(Transaction tran) {
    }

    public synchronized int getPoolLength() {
        return 0;
    }

    public void removeIfExist(Collection<Transaction> transaction) {
    }
}
