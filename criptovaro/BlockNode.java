package criptovaro;

/**This guy represents each element of the block chain which we actually conserve in memory
 */
public class BlockNode {
    public int lenght;
    public byte[] hash;

    public BlockNode(int lenght, byte[] hash) {
        super();
        this.lenght = lenght;
        this.hash = hash;
    }

    public BlockNode() {
        super();
    }
    
    public boolean equals(BlockNode b){
        return this.lenght==b.lenght && this.hash.equals(b.hash); 
    }
}
