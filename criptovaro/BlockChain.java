package criptovaro;

public class BlockChain {
    private BlockManager bm;
    private Block header;
    private long length;

    public Transaction[] rollbackBlock() {
        return null;
    }

    public boolean appendBlock(Block b) {
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

    public long getLength() 
    {
        return length;
    }

}
