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
import java.util.logging.Level;

public class Block implements Serializable {
    @SuppressWarnings("compatibility:-894874531013678188")
    private static final long serialVersionUID = 1L;
    private ArrayList<Transaction> transactions;
    private ArrayList<Transaction> funds;
    private long proof;
    private byte[] previousBlock;

    public ArrayList<Transaction> getTransactions() 
    {
        return transactions;
    }

    public boolean addTransaction(Transaction tran)
    {
        boolean result = false;
        
        //Validate transaction signature
        if(!tran.verify())
        {
            Miner.LOG.log(Level.INFO, "Transaction verification failed. Not adding to block.");    
            return result;
        }
        
        //Validate transaction semantics
        if(!Arrays.equals(tran.getSource(), tran.getDestination()) || tran.getAmount().compareTo(BigDecimal.valueOf(0)) <= 0)
        {
            Miner.LOG.log(Level.INFO, "Transaction semantics are incorrect failed. Not adding to block.");    
            return result;
        }
        
        //Add transaction inputs
        //Add transaciton outputs
        
        return result;
    }


    public byte[] getHash() throws IOException, NoSuchAlgorithmException 
    {
        byte[] result;
        ByteArrayOutputStream theBytes = new ByteArrayOutputStream();
        ObjectOutputStream oos =  new ObjectOutputStream(theBytes);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        
        oos.writeObject(this);
        oos.flush();
        md.update(theBytes.toByteArray());
        result = md.digest();
        
        return result;
    }

    public void addTransactions(Transaction[] trans) 
    {
        for(Transaction t : trans)
        {
            transactions.add(t);    
        }
    }

    public Block(Transaction[] trans) 
    {
        addTransactions(trans);
    }
    
    public Block()
    {
        
    }

    public boolean compare(Block otherBlock) 
    {
        boolean result = false;
        try 
        {
            result =  Arrays.equals(this.getHash(), otherBlock.getHash());
        } 
        catch (NoSuchAlgorithmException e) 
        {
            Miner.LOG.log(Level.INFO, e.toString());
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            Miner.LOG.log(Level.INFO, e.toString());
            e.printStackTrace();
        }
        
        return result;
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
        
        for(Transaction t : funds)
        {
            if(Arrays.equals(t.getDigitalSignature(), tran.getDigitalSignature()))
            {
                return true;
            }
        }        
        
        return false;
    }

    public ArrayList<Transaction> getFunds() 
    {
        return funds;
    }
    
    public void addFunds(ArrayList<Transaction> funds)
    {
        for(Transaction t : funds)
        {
            this.funds.add(t);    
        }
    }

    public byte[] getPreviousBlock() 
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
}
