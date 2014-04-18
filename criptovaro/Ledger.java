package criptovaro;

import java.io.File;

import java.sql.*;

public class Ledger {
    private File ledgerFile;
    private Connection conn;
    private Statement stmt;
    private ResultSet rs;

    protected boolean connect(Connection c) {
        return false;
    }

    protected boolean disconnect() {
        return false;
    }
}
