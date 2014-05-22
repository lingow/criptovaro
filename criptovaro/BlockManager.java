package criptovaro;

import java.io.IOException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

import java.sql.SQLException;

import java.sql.Time;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import sun.misc.BASE64Decoder;
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
            String insertTrans = "INSERT OR REPLACE INTO TRANSACTIONS (OWNING_BLOCK_ID, TRANSTYPE, ORIGINTRANS, FROMKEY, TOKEY, AMOUNT, SIGNATURE," +
                                 "TIMESTAMP, SPENTBY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertTrans);
            
            //iterate on all transactions
            ArrayList<Transaction> arr = new ArrayList<Transaction>(b.getTransactions());
            arr.add(b.getPrize());
            for(Transaction t : b.getTransactions())
            {
                pstmt.setString(1, encoder.encode(b.getHash()));
                pstmt.setString(2, "0");
                pstmt.setString(3, encoder.encode(t.getOriginTransaction()));
                pstmt.setString(4, encoder.encode(t.getSource()));
                pstmt.setString(5, encoder.encode(t.getDestination()));
                pstmt.setDouble(6, t.getAmount().doubleValue()); //ALERT: Possible bug that loses precision in decimals by converting to double
                pstmt.setString(7, encoder.encode(t.getDigitalSignature()));
                pstmt.setTimestamp(8, new Timestamp(new Date().getTime()));
                pstmt.setString(9, encoder.encode(t.getSpentBy())); 
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
        } catch (Exception e){
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
        //Get the needed objects
        BASE64Encoder encoder = new BASE64Encoder();
        conn = Ledger.INSTANCE.connect();
        int affectedRows = 0;
        long blockID = -1;
        boolean commit = true;
        
        //Turn off auto-commit to treat this whole block as a transaction
        try 
        {
            conn = Ledger.INSTANCE.connect();
            conn.setAutoCommit(false);
            
            String selectblockID = "SELECT BLOCK_ID FROM BLOCKS WHERE HASH=? AND LENGTH=?";
            String deleteTrans = "DELETE FROM TRANSACTIONS WHERE OWNING_BLOCK_ID=?";
            String deleteBlock = "DELETE FROM BLOCKS WHERE BLOCK_ID=?";
            
            //Get the block id
            pstmt = conn.prepareStatement(selectblockID);
            pstmt.setString(1, encoder.encode(b.getHash()));
            pstmt.setLong(2, b.getBlockChainPosition());
            rs = pstmt.executeQuery();
            
            while(rs.next())
            {
                blockID = rs.getLong(1);
            }
            
            if(blockID < 0)
            {
                Miner.LOG.log(Level.INFO, "Block not found. Exiting deleteBlock");
                return;
            }
            rs.close(); //Close the result set to reuse the statement
            
            //Now delete all transactions
            pstmt = conn.prepareStatement(deleteTrans);
            pstmt.setLong(1, blockID);
            affectedRows = pstmt.executeUpdate();
            if(affectedRows < 1)
                commit = false;
            
            //Now delete the block
            pstmt = conn.prepareStatement(deleteBlock);
            pstmt.setLong(1, blockID);
            affectedRows = pstmt.executeUpdate();
                if(affectedRows < 1)
                    commit = false;
            
            if(commit)
                conn.commit();
            Miner.LOG.log(Level.INFO, "Block deleted successfully.");
        } 
        catch (SQLException e) 
        {
            Miner.LOG.log(Level.INFO, e.toString());
            e.printStackTrace();
        }
        finally
        {
            Ledger.INSTANCE.disconnect(conn);
        }
    }

    public Block getBlock(BlockNode bn) 
    {
        conn = Ledger.INSTANCE.connect();
        BASE64Encoder encoder = new BASE64Encoder();
        String selectblockID = "SELECT * FROM BLOCKS WHERE HASH=? AND LENGTH=?";
        Block b = null;
        try {
            pstmt = conn.prepareStatement(selectblockID);
            pstmt.setString(1, encoder.encode(bn.getHash()));
            pstmt.setLong(2, bn.getLength());
            rs = pstmt.executeQuery();
            while (rs.next()){
                b = new Block();
                BASE64Decoder decoder = new BASE64Decoder();
                b.setPreviousBlock(decoder.decodeBuffer(rs.getString("PREVIOUS_BLOCK_HASH")));
                b.setBlockChainPosition(rs.getLong("LENGTH"));
                b.setSolverPublicKey(decoder.decodeBuffer(rs.getString("SOLVERPUBLICKEY")));
                b.setProof(rs.getLong("PROOF"));
                String selectBlockTransactions = "SELECT * FROM TRANSACTIONS WHERE OWNING_BLOCK_ID=?";
                PreparedStatement pstmt2 = conn.prepareStatement("SELECT * FROM TRANSACTIONS WHERE OWNING_BLOCK_ID=? ORDER BY TIMESTAMP");
                pstmt2.setInt(1, rs.getInt("BLOCK_ID"));
                ResultSet rs2 = pstmt2.executeQuery();
                while (rs2.next()){
                    Transaction t = new Transaction(rs2);
                    if (t.getType()==TransactionType.PRIZE){
                        b.setPrizeTransaction(t);
                    }else{
                        b.pushTransaction(t);
                    }
                }
                rs2.close();
            }
            rs.close();
            Ledger.INSTANCE.disconnect(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }
}
