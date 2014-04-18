package criptovaro;

public class BlockChain {
    private BlockManager bm;
    private Block header;
    private int length;

    public boolean rollbackBlock() {
        return false;
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
}
