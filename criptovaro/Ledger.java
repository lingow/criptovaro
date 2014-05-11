package criptovaro;

import java.io.File;

import java.net.InetAddress;

import java.net.UnknownHostException;

import java.sql.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Ledger {
    /**
     * This is the constant which identifies where are we going to look for a ledger database file
     */
    private static final String LEDGERFILE="jdbc:sqlite:ledger.db";
    
    /**
     * This attribute makes the Ledger a singleton.
     */
    public static final Ledger INSTANCE = new Ledger();
    
    private static final String[] createTableQueries = 
        {"CREATE TABLE IF NOT EXISTS BLOCKS \n" + 
        "( BLOCKS_ID NUMBER NOT NULL \n" + 
        ", PREVIOUS_BLOCK_ID NUMBER NOT NULL DEFAULT 1 \n" + 
        ", HASH RAW(32) NOT NULL \n" + 
        ", LENGHT NUMBER NOT NULL \n" + 
        ", SOLVERPUBLICKEY RAW(128) NOT NULL \n" + 
        ", PROOF NUMBER NOT NULL \n" + 
        ", CONSTRAINT BLOCKS_PREVIOUS_BLOCK_ID FOREIGN KEY ( PREVIOUS_BLOCK_ID )\n" + 
        "    REFERENCES BLOCKS ( BLOCKS_ID )\n" + 
        "     \n" + 
        ", CONSTRAINT BLOCKS_PK PRIMARY KEY ( BLOCKS_ID ) \n" + 
        ", CONSTRAINT BLOCKS_UNIQUE_LENGHT UNIQUE ( LENGHT ) \n" + 
        ");\n" , 
        "CREATE INDEX IF NOT EXISTS BLOCKS_INDEX1 ON BLOCKS ( BLOCKS_ID);\n" , 
        "CREATE INDEX IF NOT EXISTS BLOCKS_INDEX2 ON BLOCKS ( HASH,  LENGHT DESC);\n" , 
        "CREATE TABLE IF NOT EXISTS TRANSACTIONS \n" + 
        "( TRANSACTIONS_ID NUMBER NOT NULL \n" + 
        ", OWNING_BLOCK_ID NUMBER NOT NULL \n" + 
        ", TRANSTYPE NUMBER NOT NULL \n" + 
        ", ORIGINTRANS NUMBER \n" + 
        ", FROMKEY RAW(128) NOT NULL \n" + 
        ", TOKEY RAW(128) NOT NULL \n" + 
        ", SALT RAW(8) NOT NULL \n" + 
        ", AMMOUNT NUMBER NOT NULL \n" + 
        ", SIGNATURE RAW(128) NOT NULL \n" + 
        ", TIMESTAMP TIMESTAMP NOT NULL \n" + 
        ", spentby NUMBER \n" + 
        ", CONSTRAINT TRANSACTIONS_ORIGINTRANS FOREIGN KEY ( TRANSACTIONS_ID )\n" + 
        "    REFERENCES TRANSACTIONS ( TRANSACTIONS_ID )\n" + 
        "    ON DELETE CASCADE \n" + 
        ", CONSTRAINT TRANSACTIONS_OWNING_BLOCK FOREIGN KEY ( OWNING_BLOCK_ID )\n" + 
        "    REFERENCES BLOCKS ( BLOCKS_ID )\n" + 
        "    ON DELETE CASCADE \n" + 
        ", CONSTRAINT TRANSACTIONS_SPENTBY FOREIGN KEY ( TRANSACTIONS_ID )\n" + 
        "    REFERENCES TRANSACTIONS ( TRANSACTIONS_ID )\n" + 
        "    ON DELETE SET NULL \n" + 
        ", CONSTRAINT TRANSACTIONS_PK PRIMARY KEY ( TRANSACTIONS_ID ) \n" + 
        ", CONSTRAINT TRANSACTIONS_UK1 UNIQUE ( FROMKEY, SALT, TOKEY, AMMOUNT, SIGNATURE, TIMESTAMP ) \n" + 
        ");",
        "CREATE INDEX IF NOT EXISTS TRANSACTIONS_INDEX1 ON TRANSACTIONS ( TRANSACTIONS_ID);\n" , 
        "CREATE INDEX IF NOT EXISTS TRANSACTIONS_INDEX2 ON TRANSACTIONS ( OWNING_BLOCK_ID);\n" , 
        "CREATE INDEX IF NOT EXISTS TRANSACTIONS_INDEX3 ON TRANSACTIONS ( FROMKEY,  TOKEY,  SALT,  AMMOUNT,  TIMESTAMP);\n" , 
        "CREATE INDEX IF NOT EXISTS TRANSACTIONS_INDEX4 ON TRANSACTIONS ( spentby);\n" ,
        "CREATE TABLE IF NOT EXISTS PEERS \n" + 
        "( ip VARCHAR2(20) NOT NULL \n" + 
        ", port NUMBER NOT NULL \n" + 
        ", CONSTRAINT PEERS_UK1 UNIQUE ( ip, port ) \n" + 
        ");\n"};
    /**
     * Because the Ledger is a singleton, to invoke it's instance one should invoke Ledger.INSTANCE instead of calling
     * this constructor. That's why it's private.
     */
    private Ledger(){
    }

    /**
     * Invoke init before using the Ledger. It locates or creates a database file to use for the Ledger. 
     */
    void init() {
        Connection c = connect();
        createLedger(c);
        disconnect(c);
    }

    /**
     * Reads every Peer in the database and appends them into an ArrayList
     * @return An ArrayList with every Peer in the ledger
     */
    protected Set<Peer> q_PeerList() {
        Connection c = null;
        Set<Peer> ret = new HashSet<Peer>();
        Statement stmt = null;
        String sql = "SELECT * FROM PEERS;";
        c = connect();
        try {
            stmt = c.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(sql);
                while( rs.next()){
                    try {
                        String ip = rs.getString("IP");
                        int port = rs.getInt("PORT");
                        ret.add(new Peer(InetAddress.getByName(ip), port ));
                    } catch (UnknownHostException e) {
                        Miner.LOG.log(Level.WARNING,"Found a peer with a badly formed IP. Deleting it. : " + e.getMessage());
                        //TODO: Add code to delete badly formed peers
                    } catch (SQLException se) {
                        Miner.LOG.log(Level.WARNING,"Failed to read Peers table fields: " + se.getMessage() );
                        //TODO: add something here
                    }
                }
            } catch (SQLException e) {
                Miner.LOG.log(Level.WARNING,"Could not execute statement:\n" + sql + "\n" + e.getMessage());
            }
            stmt.close();
        } catch (SQLException e) {
            Miner.LOG.log(Level.WARNING,"Could not create statement" + e.getMessage());
        }
        disconnect(c);
        return ret;
    }

    /**
     * Inserts passed peer into the Peers table 
     * @param p the peer to insert
     */
    protected void q_InsertPeer(Peer p) {
        Connection c = null;
        Statement stmt = null;
        String sql = "INSERT OR REPLACE INTO PEERS (IP,PORT) " +
            "VALUES (" + p.getIPAddress().toString() + "," + p.getPort()+");";
        c = connect();
        try {
            stmt = c.createStatement();        
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch (SQLException e) {
            Miner.LOG.log(Level.WARNING,
                "Failed to insert a peer("+ p.getIPAddress().toString() + "," + p.getPort()+"): "+ e.getMessage());
        }
        disconnect(c);
    }

    /**
     * Deletes a peer from the Peers table
     * @param p the peer to delete
     */
    void q_DeletePeer(Peer p) {
        Connection c = null;
        Statement stmt = null;
        String sql = "DELETE FROM PEERS WHERE " +
            "IP=" + p.getIPAddress().toString() + " AND PORT=" + p.getPort()+";";
        c = connect();
        try {
            stmt = c.createStatement();        
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
        } catch (SQLException e) {
            Miner.LOG.log(Level.WARNING,
                "Failed to delete a peer("+ p.getIPAddress().toString() + "," + p.getPort()+"): "+ e.getMessage());
        }
        disconnect(c);
    }

    /**
     * Call this method to get the Connection object to the ledger database
     * Make sure to then invoke the disconnect method on that connection when it will no longer be used
     * @return the Connection Object that represents the ledger in use
     */
    private Connection connect() {
        Connection c = null;
        Miner.LOG.log(Level.INFO, "Connecting to DB");
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(LEDGERFILE);
        } catch ( ClassNotFoundException cnfe ) {
            Miner.LOG.log(Level.SEVERE, "Failed to load sqllite library: " + cnfe.getMessage());
            System.err.println( cnfe.getClass().getName() + ": " + cnfe.getMessage() );
            System.exit(1);
        } catch (SQLException se){
            Miner.LOG.log(Level.SEVERE, "Failed to connect to sqlite database: " + se.getMessage());
            System.err.println( se.getClass().getName() + ": " + se.getMessage() );
            System.exit(1);
        }
        Miner.LOG.log(Level.INFO, "Opened database successfully");
        return c;
    }

    /**
     * This method closes the passed connection in case that it's open
     * @param c the connection to close
     */
    private void disconnect(Connection c) {
        try {
            if (!c.isClosed()) {
                c.close();
            }
        } catch (SQLException e) {
            Miner.LOG.log(Level.WARNING,"Failed to close SqlLite: " + e.getMessage());
        }
    }

    /**
     * This method makes sure that the database has the correct tables, and creates them if necessary
     * @param c the jdbc connection to the database
     */
    private void createLedger(Connection c) {
        Statement stmt = null;
        Miner.LOG.log(Level.INFO,"Initializing database tables");
        for (String sql : createTableQueries){
            try {
                stmt = c.createStatement();
                stmt.executeUpdate(sql);
                stmt.close();
            } catch (SQLException e) {
                Miner.LOG.log(Level.WARNING,"Failed executing this statement: " + sql + "\n" + e.getMessage());
            }
        }
        Miner.LOG.log(Level.INFO,"Finished creating database tables");
    }
}
