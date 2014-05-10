package criptovaro;

import java.io.File;

import java.sql.*;

import java.util.ArrayList;
import java.util.HashSet;
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
    protected ArrayList<Peer> q_PeerList() {
        return null;
    }

    /**
     * Inserts passed peer into the Peers table 
     * @param p the peer to insert
     */
    protected void q_InsertPeer(Peer p) {
    }

    /**
     * Deletes a peer from the Peers table
     * @param p the peer to delete
     */
    void q_DeletePeer(Peer p) {
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
        Miner.LOG.log(Level.INFO,"Creating or Verifying database tables");
        try {
            //First make sure we have the right tables
            //To do that create a set of the right tables. Then, substract the queried tables from the set and call
            //the table creation function
            HashSet<String> tables = new HashSet<String>(); 
            tables.add("blocks");
            tables.add("transactions");
            tables.add("spenttransactions");
            tables.add("peers");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select lcase(tbl_name) as name from sqlite_master;");
            while (rs.next()){
                tables.remove(rs.getString(1));
            }
            for(String s : tables){
                 if (s.equals("blocks")){
                     q_CreateTableBlocks(c);
                 } else if (s.equals("transactions")){
                     q_CreateTableTransactions(c);
                 } else if (s.equals("spenttransactions")){
                     q_CreateTableSpentTransactions(c);
                 } else if (s.equals("peers")){
                     q_CreateTablePeers(c);
                 }
            }
            stmt.close();
        } catch (SQLException e) {
            Miner.LOG.log(Level.SEVERE,"Failed to Create Database: " + e.getMessage());
            System.exit(1);
        }
        Miner.LOG.log(Level.INFO,"Database tables were correctly created");
    }

    private void q_CreateTableBlocks(Connection c) {
    }

    private void q_CreateTableTransactions(Connection c) {
    }

    private void q_CreateTableSpentTransactions(Connection c) {
    }

    private void q_CreateTablePeers(Connection c) {
    }
}
