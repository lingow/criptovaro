package criptovaro;

import java.util.ArrayList;

public class TransactionManager{
    private Ledger l;

    public TransactionManager(Ledger l) {
    }

    public void insertTransaction(Transaction tran) {
    }

    public void insertUnspentTransaction(Transaction tran) {
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
    
    public boolean validateTransaction(Transaction tran)
    {
        return true;    
    }
    
    /*
     * Using the amount of the passed transaction, it returns an array of unspent transations that can be used to solvent
     * the current transaction.
     * If there are enough funds in the ledger, the transactions MUST be marked as spent to avoid double spending in the same block.
     * If the block using this funds is not committed, the funds must be returned.
     * On success: returns the array of unspent transactions necessary to solvent this transaction as well as the corresponding
     *             output transaction.
     * On failure: returns null, logs error.
     */
    public ArrayList<Transaction> getTransactionFunds(byte[] publicKey)
    {
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        return transactions;
    }
}
