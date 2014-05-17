package criptovaro;

/**This guy represents each element of the block chain which we actually conserve in memory
 */
public class BlockNode {
    private int length;
    private byte[] hash;

    public BlockNode(int length, byte[] hash) {
        super();
        this.length = length;
        this.hash = hash;
    }

    public BlockNode() {
        super();
    }
    
    public boolean equals(BlockNode b){
        return this.length==b.length && this.hash.equals(b.hash); 
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }
}
