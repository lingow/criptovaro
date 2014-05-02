package criptovaro;

import java.io.File;

import java.sql.*;

public class Ledger {
    private File ledgerFile;
    private Connection conn;
    private Statement stmt;
    private ResultSet rs;
    
    /**
     * This attribute makes the Ledger a singleton.
     */
    public static final Ledger INSTANCE = new Ledger();

    protected boolean connect(Connection c) {
        return false;
    }

    protected boolean disconnect() {
        return false;
    }

    /**
     * Because the Ledger is a singleton, to invoke it's instance one should invoke Ledger.INSTANCE instead of calling
     * this constructor. That's why it's private.
     */
    private Ledger(){
    }
}
