package criptovaro;

public class Block {
    private Transaction[] transactions;
    private long proof;
    private byte[] previousBlock;

    public Transaction[] getTransactions() {
        return null;
    }

    public void addTransaction(Transaction tran) {
    }

    public byte[] getHash() {
        return null;
    }

    public void addTransactions(Transaction[] trans) {
    }

    public void Block(Transaction[] trans) {
    }

    public boolean compare(Block otherBlock) {
        return false;
    }

    public int verify() {
        return 0;
    }

    public boolean containsTransaction(Transaction tran) {
        return false;
    }
}
