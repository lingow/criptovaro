package criptovaro;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.util.logging.Level;

import sun.misc.BASE64Encoder;

public class BlockManager 
{
    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    private PreparedStatement pstmt = null;
    private StringBuilder sb = null;
    
    public BlockManager() 
    {
        sb = new StringBuilder();
    }

    public boolean insertBlock(Block b) 
    {
        try
        {
            //Get the needed objects
            BASE64Encoder encoder = new BASE64Encoder();
            conn = Ledger.INSTANCE.connect();
            int affectedRows = 0;
            int totalAffectedRows[];
            
            //Turn off auto-commit to treat this whole block as a transaction
            conn.setAutoCommit(false);
            
            //Prepare the query
            String insertBlock = "INSERT INTO BLOCKS (PREVIOUS_BLOCK_HASH, HASH, LENGTH, SOLVERPUBLICKEY, PROOF) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertBlock);
            pstmt.setString(1, encoder.encode(b.getPreviousBlockHash()));
            pstmt.setString(2, encoder.encode(b.getHash()));
            pstmt.setLong(3, b.getBlockChainPosition());
            pstmt.setString(4, encoder.encode(b.getSolverPublicKey()));
            pstmt.setLong(5, b.getProof());
            affectedRows = pstmt.executeUpdate();
        
            if(affectedRows <= 0 )
            {
                conn.rollback();
                Miner.LOG.log(Level.INFO, "Block failed to insert. Rollingback.");
            }
            
            //Now we insert all the corresponding transactions.
            String selectblockID = "(SELECT BLOCK_ID FROM BLOCKS WHERE HASH='" + encoder.encode(b.getHash()) + "' AND LENGTH =" + b.getBlockChainPosition() + ")";
            String insertTrans = "INSERT OR REPLACE INTO TRANSACTIONS (OWNING_BLOCK_ID, TRANSTYPE, ORIGINTRANS, FROMKEY, TOKEY, AMOUNT, SIGNATURE," +
                                 "TIMESTAMP, SPENTBY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertTrans);
            
            //iterate on all transactions
            for(Transaction t : b.getTransactions())
            {
                pstmt.setString(1, encoder.encode(b.getHash()));
                pstmt.setString(2, "0");
                pstmt.setString(3, encoder.encode(t.getOriginTransaction())); //TODO: Look in the code for change transactions and set this field
                pstmt.setString(4, encoder.encode(t.getSource()));
                pstmt.setString(5, encoder.encode(t.getDestination()));
                pstmt.setDouble(6, t.getAmount().doubleValue()); //ALERT: Possible bug that loses precision in decimals by converting to double
                pstmt.setString(7, encoder.encode(t.getDigitalSignature()));
                pstmt.setString(8, String.valueOf(t.getTimestamp()));
                pstmt.setString(9, encoder.encode(t.getSpentBy())); //TODO: Look in the code for change transactions and set this field
                pstmt.addBatch();
            }
            
            //Execute statement            
            totalAffectedRows = pstmt.executeBatch();
            boolean commit = true;
            
            for(int r : totalAffectedRows)
            {
                if(r < 1)
                {
                    commit = false; 
                }
            }
            
            if(commit)
            {
                conn.commit();
                Miner.LOG.log(Level.INFO, "Block commited. " + affectedRows + " rows affected.");
            }
            else
                conn.rollback();
        }
        catch(SQLException e)
        {
            Miner.LOG.log(Level.INFO, e.toString());
            e.printStackTrace();
        }
        finally
        {
            Ledger.INSTANCE.disconnect(conn);
        }
        return true;
    }

    public void deleteBlock(Block b) 
    {
        
    }

    public Block getBlock(BlockNode bn) 
    {
        //TODO: Implement Method
        return null;
    }
}
