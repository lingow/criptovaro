package criptovaro;

import java.util.Queue;

public class TransactionPool {
    private Set<Transaction> pool;

    public synchronized void addTransaction(Transaction tran) {
    }

    public synchronized void addTransactionList(Transaction[] transactions) {
    }

    public synchronized Transaction consumeTransaction() {
        return null;
    }

    public synchronized void deleteTransaction(Transaction tran) {
    }

    public synchronized int getPoolLength() {
        return 0;
    }
}
