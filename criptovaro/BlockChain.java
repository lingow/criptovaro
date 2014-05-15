package criptovaro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class BlockChain {
    private BlockManager bm;
    private LinkedList<BlockNode> blockChain;

    /**
     * Rollbacks the latest block from the block chain and from disk
     * @return the leftover regular transactions that these blocks processed before
     */
    public Collection<Transaction> rollbackBlock() {
        Collection<Transaction> leftovers = new ArrayList<Transaction>();
        BlockNode chainTop = blockChain.pop();
        Block blockToKill = bm.getBlock(chainTop);
        
        for(Transaction t : blockToKill.getRegularTrans()){
                leftovers.add(t);
        }
        bm.deleteBlock(blockToKill);
        return leftovers;
    }

    /**
     * Appends the block to the last position in the chain
     * @param b the block to append. It should be fully verified and complete. It will not be modified.
     * @return true if the block was correctly inserted
     */
    public boolean appendBlock(Block b) 
    {
        boolean inserted = bm.insertBlock(b);
        if (inserted)
        {
            blockChain.push(new BlockNode((int)b.getBlockChainPosition(),b.getHash()));

        }
        return inserted;
    }
    
    public BlockChain()
    {
        this.bm = new BlockManager();
        this.blockChain = new LinkedList<BlockNode>();
    }
    
    public Block getLatestBlock()
    {
        return bm.getBlock(blockChain.peek());
    }

    /**
     * @return current chain's total lenght
     */
    public int getLenght() {
        return blockChain.peek().lenght;
    }

    /**
     * @return the last block's hash in this block chain
     */
    public byte[] getHash() {
        return blockChain.peek().hash;
    }

    /**
     * @param hash The hash of the block from where we should begin
     * @param lenght The lenght of the block from where we should begin
     * @return the hashes and lenghts of the block chain in order starting from the specified hash and lenght 
     * in the form of a LinkedHashMap it returns every value in the block chain in the case the specified hash 
     * and lenght are not found
     */
    LinkedHashMap<byte[], Integer> getChainBranch(byte[] hash, int lenght) {
        LinkedHashMap<byte[], Integer> retMap= new LinkedHashMap<byte[], Integer>();
        boolean found=false;
        for (BlockNode bn : blockChain){
            if (found){
                retMap.put(bn.hash, bn.lenght);
            }
            if (bn.hash==hash && bn.lenght==lenght){
                found=true;
                retMap.put(bn.hash, bn.lenght);
            }
        }
        if (found){
           return retMap; 
        }
        for (BlockNode bn : blockChain){
            retMap.put(bn.hash, bn.lenght);
        }
        return retMap;
    }

    /**
     * @return this block chain's BlockNode list in backwards order
     */
    public Collection<BlockNode> getBackwardsBlockChain() {
        LinkedList<BlockNode> retList = new LinkedList<BlockNode>();
        Collections.copy(blockChain, retList);
        Collections.reverse(retList);
        return retList;
    }

    /**
     * Merges the passed branch with current blockchain, beginning from the commonlenght Blocknode.
     * This implies that existing blocks in current chain after the commonlenght node should be discarded, reinserting
     * regular transactions (non change, non prize) into the Transaction Pool, and retrieving those transactions as new
     * peerBlocks are integrated into the blockchain.
     * The branch is not merged if the resulting lenght would be smaller than the current lenght
     * @param commonNode the BlockNode from which to begin the merge
     * @param peerBlocks the blocks offered by a peer to insert into the blockchain
     * @param tp the transaction pool
     */
    void merge(BlockNode commonNode, LinkedHashMap<BlockNode, Block> peerBlocks,TransactionPool tp) {
        if ( commonNode.lenght + peerBlocks.size() > getLenght()){
            while(true){
                if (! blockChain.peek().equals(commonNode)){
                    tp.addTransactionList(rollbackBlock());
                } else {
                    break;
                }
            }
            for(Block b : peerBlocks.values()){
                tp.removeIfExist(b.getRegularTrans());
                appendBlock(b);
            }
        }
    }
}
