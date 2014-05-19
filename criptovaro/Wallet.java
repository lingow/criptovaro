package criptovaro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.transform.Result;

public class Wallet {
    private File file;
    Map<String,Account> accounts;
    
    public Wallet(String filepath) {
        file = new File(filepath).getAbsoluteFile();
        file.getParentFile().mkdirs();
        System.out.println("Creating or loading Wallet in " + file.getAbsolutePath());
        Statement stmt = null;
        
        String queries[] = {"CREATE TABLE IF NOT EXISTS ACCOUNTS " +
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
                            "CREATE INDEX IF NOT EXISTS ACCOUNTS_INDEX1 ON ACCOUNTS ( ALIAS);"
                            };

        Connection connection = null;
        try {
            connection = connect();
            for (String sql : queries){
                try {
                    stmt = connection.createStatement();
                    stmt.executeUpdate(sql);
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            accounts = getAccounts();
        } catch (Exception e) {
            System.out.println("Error connecting to database.");
            e.printStackTrace();
        } finally {
            disconnect(connection);
        }
    }
    
    public void setWalletFile(Path filepath) {
        this.file = new File(filepath.toString());
    }
    
    public Path getWalletFile() {
        return file.toPath().toAbsolutePath();
    }
    
    public void setMinerPid(Account acc, int pid) {
        acc.setMinerPid(pid);
        saveToWallet(acc);
    }
    
    public long getMinerPid(String accountAlias) {
        try {
            return accounts.get(accountAlias).getMinerPid();
        } catch (NullPointerException e) {
            System.out.println("No such account");
            e.printStackTrace();
        }
        return -1;
    }
    
    private Connection connect(){
        Path filepath = file.toPath().toAbsolutePath();
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

    public void deleteAccount(Account acc) 
    {
        //if path exists and alias exists and account exists, remove row from table wallet
        throw new UnsupportedOperationException();
    }

  
    
    public void addBuddy(String buddy, Path keyFile) throws IOException {
        if(Files.notExists(keyFile)) {
            return;
        }
        String key = null;
        
        //Reads the key in the key file.
        try(BufferedReader br = new BufferedReader(new FileReader(keyFile.toString()))) {
                StringBuilder sb = new StringBuilder();
                key = br.readLine();
                System.out.println(key);
        }
        
        //Creates a file with the list of buddies.
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream("wallets/buddies.txt"), "utf-8"));
            writer.write(buddy+","+key);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            writer.close();
        }
        
    }

    public Map<String,Account> getAccounts() {
        Statement stmt = null;
        String encodedPrivateKey = null;
        String encodedPublicKey = null;
        String alias = null;
        int minerPort = -1;
        int minerPid = -1;
        byte[] decodedPrivateKey;
        byte[] decodedPublicKey;
        String sql = "SELECT * FROM ACCOUNTS;";
        Map<String,Account> walletAccounts = new HashMap<String,Account>();
        
        Connection connection = null;;
        connection = connect();
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
                    walletAccounts.put(alias,acc);
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

    public Account getAccount(String minerAccountAlias) {
        return accounts.get(minerAccountAlias);
    }
    
    public void saveToWallet(Account account) {
        PreparedStatement pstmt = null;
        Connection connection = null;
        String alias = account.getAlias();
        byte[] publicKey = account.getPublicKey();
        byte[] privateKey = account.getPrivateKey();
        String encodedPublicKey = null;
        String encodedPrivateKey = null;
        Path filepath = file.toPath().toAbsolutePath();
        
        // Validates account and wallet.
        if (account == null || filepath == null) {
            System.out.println("Please create a wallet and an account.");
            return;
        }
        
        // Validates keys and encodes them.
        if(publicKey == null || privateKey == null) {
            return;
        } else {
            encodedPublicKey = account.encodeKey(publicKey);
            encodedPrivateKey = account.encodeKey(privateKey);
        }
        
        // Validates existance of wallet file.
        if(Files.notExists(filepath)) {
            System.out.println("Wallet file doesn't exist.");
            return;
        }

        //Connection to database.
        try {
            connection = connect();
            System.out.println("Connected to database: "+filepath);
            pstmt = connection.prepareStatement( "INSERT OR REPLACE INTO ACCOUNTS " +
                                                 "(PRIVATEKEY, PUBLICKEY, ALIAS,MINERPID,MINERPORT) " + 
                                                 "VALUES (?,?,?,?,?);");
            pstmt.setString(1, encodedPrivateKey);
            pstmt.setString(2, encodedPublicKey);
            pstmt.setString(3,alias);
            if ( account.getMinerPid() > 0){
                pstmt.setLong(4, account.getMinerPid());
            }else{
                pstmt.setNull(4, Types.NULL);
            }
            if ( account.getMinerPort() > 0){
                pstmt.setInt(5, account.getMinerPort());
            }else{
                pstmt.setNull(5, Types.NULL);
            }
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error connecting to database: "+e.getMessage());
            e.printStackTrace();
        } finally {
            disconnect(connection);
            System.out.println("Disconnected from database.");    
        }
    }
}
