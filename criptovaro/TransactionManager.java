package criptovaro;

import java.io.IOException;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.logging.Level;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class TransactionManager{

    public TransactionManager() 
    {
    }

    public void deleteUnspentTrans(Transaction tran) 
    {
    }

    public Transaction getTransaction(byte[] tranSignature) 
    {
        throw new UnsupportedOperationException();
        /*
        Transaction t = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM TRANSACTIONS WHERE SIGNATURE = '");
        BASE64Encoder encoder = new BASE64Encoder();
        sb.append(encoder.encode(tranSignature));
        sb.append("' ;");
        ResultSet rs = Ledger.INSTANCE.select(sb.toString());
        if (rs != null ){
            try {
                while (rs.next()) {
                    t = new Transaction(
                                rs.getString("FROMKEY"),
                                rs.getString("TOKEY"),
                                rs.getString("OWNING_BLOCK"))
                }
            } catch (SQLException e) {
                Miner.LOG.log(Level.WARNING,"Failed to get resultset");
            }
        }
        */
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
    
    public ArrayList<Transaction> getAccountFunds(byte[] account)
    {
        ArrayList<Transaction> retList = new ArrayList<Transaction>();
        StringBuilder sb = new StringBuilder();
        Connection c = Ledger.INSTANCE.connect();
        sb.append("SELECT * FROM TRANSACTIONS WHERE TOKEY = '");
        BASE64Encoder encoder = new BASE64Encoder();
        sb.append(encoder.encode(account));
        sb.append("'  AND SPENTBY IS NULL ;");
        ResultSet rs = Ledger.INSTANCE.select(c,sb.toString());
        if (rs != null ){
            try {
                while (rs.next()) {
                    try {
                        retList.add(new Transaction(rs));
                    } catch (SQLException se){
                        Miner.LOG.log(Level.WARNING,"A transaction could not be read");
                        se.printStackTrace();
                    }catch (IOException e) {
                        Miner.LOG.log(Level.WARNING,"A transaction was malformed in the database");
                        e.printStackTrace();
                    } 
                }
            } catch (SQLException e) {
                Miner.LOG.log(Level.WARNING,"Failed to get resultset");
            }
        }
        Ledger.INSTANCE.disconnect(c);
        return retList;
    }
}
