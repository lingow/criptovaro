package criptovaro;

import java.math.BigDecimal;

import java.util.Arrays;

public class Block {
    private Transaction[] transactions;
    private long proof;
    private byte[] previousBlock;

    public Transaction[] getTransactions() {
        return null;
    }

    public void addTransaction(Transaction tran)
    {
        //Validate transaction signature
        //Validate transaction semantics
        //Add transaction inputs
        //Add transaciton outputs
    }

//    private boolean GenerateOutputs()
//    {
//        BigDecimal inputsAmount = BigDecimal.valueOf(0);
//        
//        //Are the inputs enough?
//        for(Transaction t: inputs)
//        {
//            //Validate transaction semantics
//            if(Arrays.equals(t.getDestination(), source))
//                inputsAmount = inputsAmount.add(t.getAmount());
//        }
//        if(inputsAmount.compareTo(this.amount) < 0)
//            return false; //Not enough funds. TODO: Log this event.
//        
//        //Generate change transaction 
//        BigDecimal changeAmount = inputsAmount.subtract(amount);
//        //We generate a change transaction with source and destiantion inverted for the change amount.
//        outputs.add(new Transaction(destination, source, changeAmount));
//        
//        return true;
//    }

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
