package criptovaro;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.xml.transform.Result;

public class Wallet {
    private int pid;
    private Path filepath;
    ArrayList<Account> accounts;
    
    public Wallet() {
        pid = 0;
        filepath = null;
        accounts = new ArrayList<Account>();
    }
    
    public Wallet(Path filepath) {
        this.filepath = filepath;
        Statement stmt = null;
        
        String queries[] = {"CREATE TABLE ACCOUNTS " +
                            "( PRIVATEKEY VARCHAR(1024) NOT NULL " +
                            ", PUBLICKEY VARCHAR(1024) NOT NULL " +
                            ", MINERPID NUMBER " +
                            ", MINERPORT NUMBER " +
                            ", ALIAS VARCHAR2(32) NOT NULL " +
                            ", CONSTRAINT ACCOUNTS_PK PRIMARY KEY ( ALIAS ) " +
                            ", CONSTRAINT ACCOUNTS_UK1 UNIQUE ( MINERPID ) " +
                            ", CONSTRAINT ACCOUNTS_UK2 UNIQUE ( MINERPORT ) " +
                            ", CONSTRAINT ACCOUNTS_UK3 UNIQUE ( PRIVATEKEY, PUBLICKEY ) " +
                            ");",
                            "CREATE INDEX ACCOUNTS_INDEX1 ON ACCOUNTS ( ALIAS);"
                            };

        Connection connection = null;
        try {
            connection = connect(filepath);
            for (String sql : queries){
                try {
                    stmt = connection.createStatement();
                    stmt.executeUpdate(sql);
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Error connecting to database.");
            e.printStackTrace();
        } finally {
            disconnect(connection);
        }
    }
    
    public void setWalletFile(Path filepath) {
        this.filepath = filepath;
    }
    
    public Path getWalletFile() {
        return filepath;
    }
    
    public void setMinerPid(int pid) {
        this.pid = pid;
    }
    
    public int getMinerPid() {
        return pid;
    }
    
    private Connection connect(Path filepath){
        Connection connection = null;
        String dbfile = filepath.toAbsolutePath().toString();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:"+dbfile);
        } catch(ClassNotFoundException ce) {
            ce.printStackTrace();    
        } catch(SQLException se) {
            se.printStackTrace();
            
        }
        return connection;
    }

    private void disconnect(Connection c) {
        try {
            c.close();
        } catch(SQLException se) {
            se.printStackTrace();
        }
    }

    public void deleteAccount(Account acc) {
        //if path exists and alias exists and account exists, remove row from table wallet
    }

    public void getAccount(String name) {
        
    }

    public ArrayList getAccounts() {
        Statement stmt = null;
        String encodedPrivateKey = null;
        String encodedPublicKey = null;
        String alias = null;
        int minerPort = -1;
        int minerPid = -1;
        byte[] decodedPrivateKey;
        byte[] decodedPublicKey;
        String sql = "SELECT * FROM ACCOUNTS;";
        ArrayList<Account> walletAccounts = new ArrayList<Account>();
        
        Connection connection = null;
        filepath = filepath.toAbsolutePath();
        connection = connect(filepath);
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            try {
                while(rs.next()) {
                    encodedPrivateKey = rs.getString("PRIVATEKEY");
                    encodedPublicKey = rs.getString("PUBLICKEY");
                    minerPid = rs.getInt("MINERPID");
                    minerPort = rs.getInt("MINERPORT");
                    alias = rs.getString("ALIAS");
                    Account acc = new Account();
                    decodedPrivateKey = acc.decodeKey(encodedPrivateKey);
                    decodedPublicKey = acc.decodeKey(encodedPublicKey);
                    acc.setKeys(decodedPublicKey, decodedPrivateKey);
                    acc.setAlias(alias);
                    walletAccounts.add(acc);
                }
            } catch (SQLException se) {
                se.printStackTrace();
            } catch (NullPointerException np) {
                System.out.println("Couldn't fetch data.");
                np.printStackTrace();
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error fetching results.");
            e.printStackTrace();
        } finally {
            disconnect(connection);
        }
        
        return walletAccounts;
    }
}
