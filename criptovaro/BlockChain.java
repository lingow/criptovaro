package criptovaro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class BlockChain {
    private BlockManager bm;
    private Block header;
    private int length;

    public Transaction[] rollbackBlock() {
        return null;
    }

    public boolean appendBlock(Block b) 
    {
        return true;
    }

    public Block getPrevBlock(Block b) {
        return null;
    }

    public boolean containsBlock(byte[] blockHash) {
        return true;
    }
    
    public BlockChain()
    {
        //Set block header here    
    }
    
    public Block getBlockHeader()
    {
        return null;    
    }

    /**
     * @return current chain's total lenght
     */
    public int getLenght() {
        //TODO: Implement this method
        return 0;
    }

    /**
     * @return the last block's hash in this block chain
     */
    public byte[] getHash() {
        //TODO: Implement this method
        return null;
    }

    /**
     * @param hash The hash of the block from where we should begin
     * @param lenght The lenght of the block from where we should begin
     * @return the hashes and lenghts of the block chain in order starting from the specified hash and lenght 
     * in the form of a LinkedHashMap it returns every value in the block chain in the case the specified hash 
     * and lenght are not found
     */
    LinkedHashMap<byte[], Integer> getChainBranch(byte[] hash, int lenght) {
        //TODO: Implement this method
        return new LinkedHashMap<byte[],Integer>();
    }

    /**
     * @return this block chain's BlockNode list in backwards order
     */
    Collection<BlockNode> getBackwardsBlockChain() {
        //TODO: Implement this method
        return new ArrayList<BlockNode>();
    }

    /**
     * Merges the passed branch with current blockchain, beginning from the commonlenght Blocknode.
     * This implies that existing blocks in current chain after the commonlenght node should be discarded, reinserting
     * regular transactions (non change, non prize) into the Transaction Pool, and retrieving those transactions as new
     * peerBlocks are integrated into the blockchain.
     * The branch is not merged if the resulting lenght would be smaller than the current lenght
     * @param commonlenght the lenght from which to begin the merge
     * @param peerBlocks the blocks offered by a peer to insert into the blockchain
     * @param tp the transaction pool
     */
    void merge(int commonlenght, LinkedHashMap<BlockNode, Block> peerBlocks,TransactionPool tp) {
        //TODO: Implement this method
    }

    /** 
     * Writes this blockChain into the Ledger. We begin from the latest block and go back until we hit a block which is
     * already in the database. As we do this, we get rid of the in memory block structure.
     */
    void commitChain() {
        //TODO: Implement this method
    }
}
