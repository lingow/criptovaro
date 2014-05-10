package criptovaro;

public class TransactionManager{
    private Ledger l;

    public TransactionManager(Ledger l) {
    }

    public void insertTransaction(Transaction tran) {
    }

    public void inserUnspentTransaction(Transaction tran) {
    }

    public void deleteTrans(Transaction tran) {
    }

    public void deleteUnspentTrans(Transaction tran) {
    }

    public Transaction getTransaction(byte[] tranSignature) {
        return null;
    }

    public Transaction getUnspentTransaction(byte[] tranSignature) {
        return null;
    }
    
}
