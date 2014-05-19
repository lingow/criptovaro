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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.xml.transform.Result;

public class Wallet {
    private int pid;
    private File file;
    ArrayList<Account> accounts;
    
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
            connection = connect(file.toPath().toAbsolutePath());
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
    
    public void setMinerPid(int pid) {
        this.pid = pid;
    }
    
    public int getMinerPid(String accountAlias) {
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

    public ArrayList<Account> getAccounts() {
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
        
        Connection connection = null;;
        connection = connect(file.toPath().toAbsolutePath());
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
