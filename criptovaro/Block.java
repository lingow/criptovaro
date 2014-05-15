package criptovaro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.math.BigDecimal;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

public class Block implements Serializable {
    @SuppressWarnings("compatibility:-894874531013678188")
    private static final long serialVersionUID = 1L;
    private ArrayList<Transaction> transactions;
    private long proof;
    private byte[] previousBlock;
    private byte[] SolverPublicKey;
    private long blockChainPosition;
    private Transaction prize;
    
    public ArrayList<Transaction> getTransactions() 
    {
        return transactions;
    }

    public boolean addTransaction(Transaction newTran, TransactionManager tm, HashMap<byte[], ArrayList<Transaction>> unspentCache)
    {   
        if( newTran != null && newTran.getType() == TransactionType.REGULAR && newTran.getAmount().compareTo(BigDecimal.valueOf(0)) <= 0 &&
            !Arrays.equals(newTran.getSource(), newTran.getDestination()))
        {
            boolean validTransaction = false;
            ArrayList<Transaction> funds = new ArrayList<Transaction>();
                
            //Check that the transaction's signature and semantics are correct.
            if(newTran.verify())
            {
                //First we check Ledger for unspent transactions to solvent this transaction
                ArrayList<Transaction> sourceFunds = unspentCache.get(newTran.getSource());
                ArrayList<Transaction> destinationFunds = unspentCache.get(newTran.getDestination());
                
                if(sourceFunds == null)
                {
                    sourceFunds = tm.getAccountFunds(newTran.getSource());
                    unspentCache.put(newTran.getSource(), sourceFunds);
        }

                if(destinationFunds == null)
                {
                    sourceFunds = tm.getAccountFunds(newTran.getSource());
                    unspentCache.put(newTran.getDestination(), destinationFunds);
                }
                    
                BigDecimal reminder = newTran.getAmount();
                for(Transaction t : sourceFunds)
                {
                    reminder.subtract(t.getAmount());
                    funds.add(t);
                    if(reminder.compareTo(BigDecimal.valueOf(0)) < 1)
                    {
                        //We have enough funds. Now need to check if exact amount or change is needed.
                        if(reminder.compareTo(BigDecimal.valueOf(0)) != 0)
                        {
                            Transaction change = new Transaction(newTran.getDestination(), newTran.getSource(), reminder.abs());
                            change.setOriginTransaction(newTran.getDigitalSignature());
                            change.setType(TransactionType.CHANGE);
                            this.transactions.add(change);
                        }
                        
                        //Mark all the transactions that will fund this transactions as spent and remove them from the unspent cache
                        for(Transaction f : funds)
                        {
                            f.setSpentBy(newTran.getDigitalSignature());
                            sourceFunds.remove(f);
                        }
                        
                        //We are done!
                        validTransaction = true;
                        break;
                    }
                }
            }
            
            //The transaction is golden. Added to the current block.
            if(validTransaction)
            {
                //Update the unspent trans cache for the recipient of this transaction
                unspentCache.get(newTran.getDestination()).add(newTran);
                this.transactions.add(newTran); //Add the Transaction itself
                this.transactions.addAll(funds); //Add the inputs and outputs
                return true;
            }
        }
        
        return false;
    }


    public byte[] getHash()
    {
        
        byte[] result = null;
        ByteArrayOutputStream theBytes = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try 
        {
            oos = new ObjectOutputStream(theBytes);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            oos.writeObject(this);
            oos.flush();
            md.update(theBytes.toByteArray());
            result = md.digest();
        } 
        catch (IOException e) 
        {
            Miner.LOG.log(Level.INFO, e.toString());
            e.printStackTrace();
        } 
        catch (NoSuchAlgorithmException e) 
        {
            Miner.LOG.log(Level.INFO, e.toString());
            e.printStackTrace();
        }
        return result;
    }

    public void addTransactions(ArrayList<Transaction> trans, TransactionManager tm, HashMap<byte[], ArrayList<Transaction>> unspentCache) 
    {
        for(Transaction t : trans)
        {
            this.addTransaction(t, tm, unspentCache);    
        }
    }

    public Block(ArrayList<Transaction> trans, TransactionManager tm, HashMap<byte[], ArrayList<Transaction>> unspentCache) 
    {
        addTransactions(trans, tm, unspentCache);
    }
    
    public Block()
    {
        transactions = new ArrayList<Transaction>();
        previousBlock = new byte[]{};
        SolverPublicKey = new byte[]{};
    }

    public boolean compare(Block otherBlock) 
    {
        
        return  Arrays.equals(this.getHash(), otherBlock.getHash());
    }

    /**
     * This Method uses the block's hash and proof to tell if the proof is satisfactory
     * @return true if it matches. False if either the proof is incorrect or not set
     */
    public boolean verify() {
        //TODO: Implement this Method
        return false;
    }

    public boolean containsTransaction(Transaction tran) 
    {   
        for(Transaction t : transactions)
        {
            if(Arrays.equals(t.getDigitalSignature(), tran.getDigitalSignature()))
            {
                return true;
            }
        }
        
        
        return false;
    }

    public byte[] getPreviousBlockHash() 
    {
        return previousBlock;
    }

    public void setPreviousBlock(byte[] previousBlock) 
    {
        this.previousBlock = previousBlock;
    }

    void setProof(long currentProof) 
    {
        this.proof = currentProof;
    }

    public long getProof() 
    {
        return proof;
    }

    public byte[] getSolverPublicKey() {
        return SolverPublicKey;
    }

    public void setSolverPublicKey(byte[] SolverPublicKey) {
        this.SolverPublicKey = SolverPublicKey;
    }

    public long getBlockChainPosition() {
        return blockChainPosition;
    }

    public void setBlockChainPosition(long blockChainPosition) {
        this.blockChainPosition = blockChainPosition;
    }

    public Collection<Transaction> getRegularTrans() 
    {
        //TODO: Implement Method
        return new ArrayList<Transaction>();
    }
    
    void setPrizeTransaction(Transaction prize) 
    {
        this.prize = prize;
    }
}
