package criptovaro;

import java.util.ArrayList;
import java.util.logging.Level;

public class StatMessage extends Message{
    @SuppressWarnings("compatibility:-4273379731594610614")
    private static final long serialVersionUID = -6570888968721497852L;

    @Override
    protected boolean deliver(Peer peer) 
    {
        boolean retVal = false;
        
        //Check Peer length
        //If equal or less, ignore
        //If greater, we need to validate the branch. This involves network messages and could take a while, 
        //so we spawn a new thread to deal with this to not block the tcp listener.
        //If branch is validated, we signal the miner to stop working.
        if(peer.getChain_length() <= Miner.INSTANCE.getBChainLenght())
        {
            Miner.LOG.log(Level.INFO, "Received Stat Message with lenght " + peer.getChain_length() + 
                          ". Ignoring since own chain length is " + Miner.INSTANCE.getBChainLenght());
            return true;
        }
        
        //Ok so the peer allegedly has a better solution than we do. Let's put that to the test.
        //We do this in another thread to not block the tcp listener.
        
        return retVal;
    }
    
    private boolean validatePeerSolution(Peer peer)
    {
        ArrayList<Block> peerBranch = new ArrayList<Block>();
        ArrayList<Block> currentBranch = new ArrayList<Block>();
        PeerManager pm = PeerManager.INSTANCE;
        Block currentBlock = null;

        //Go back into the block chain until we find the first common point in the branch.
        
        
        //We found it. Now we validate each block from teh peer's solution chain individually
        //until we reach the block chain header. If all blocks pass, we accept the solution and 
        //tell the miner to stop mining and update itself.
     
        return false;   
    }
}
