package criptovaro;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;

import java.security.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

public class Account 
{
    private String alias;
    private byte[] privateKey;
    private byte[] publicKey;
    private KeyPairGenerator kpg = null;
    
    public void generateKeys() 
    {
        try
        {
            //TODO: Change algorithm to Elliptic curve. Using DSA right now for proof of concept.
            kpg = KeyPairGenerator.getInstance("DSA");
        
            //TODO: Investigate the PKCS11 libraries for more secure random number generators.
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            
            //1024 bit key length.
            kpg.initialize(1024, sr);
            KeyPair kp = kpg.generateKeyPair();
            PrivateKey privkey = kp.getPrivate();
            PublicKey pubkey = kp.getPublic();
            
            //debug line
            //System.out.println("Private key encoding format: " + privkey.getFormat());
            //System.out.println("Public key encoding format: " + pubkey.getFormat());            
            //Save the bytes in the objects internal state.
            privateKey = privkey.getEncoded();
            publicKey = pubkey.getEncoded();
            
        }
        catch(NoSuchAlgorithmException se)
        {
            se.printStackTrace();    
        }
    }
    
    public Account()
    {
        alias = null;
        privateKey = null;
        publicKey = null;
    }

    public Account(String pubkey, String privkey)
    {
        try 
        {
            setKeys(pubkey, privkey);
        } 
        catch (UnsupportedEncodingException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    public void setKeys(byte[] public_key, byte[] private_key)
    {
        this.privateKey = private_key;
        this.publicKey = public_key;
    }
    
    public void setKeys(String public_key, String private_key) throws IOException 
    {
        BASE64Decoder decoder = new BASE64Decoder();
        this.publicKey = decoder.decodeBuffer(public_key);
        this.privateKey = decoder.decodeBuffer(private_key);
    }
    
    public byte[] getPublicKey()
    {
        return publicKey;    
    }
    
    public byte[] getPrivateKey()
    {
        return privateKey;    
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String encodeKey(byte[] unencodedKey) {
        BASE64Encoder e = new BASE64Encoder();
        String encodedKey = e.encode(unencodedKey);
        return encodedKey;
    }
    
    public byte[] decodeKey(String encodedKey) {
        BASE64Decoder d = new BASE64Decoder();
        byte[] decodedKey = null;
        try {
            decodedKey = d.decodeBuffer(encodedKey);
        } catch (IOException e) {
            System.out.println("Could not decode encoded key.");
        }
        return decodedKey;
    }
    
    public void saveToWallet(Account account, Path filepath) {
        Statement stmt = null;
        Connection connection = null;
        String alias = account.getAlias();
        byte[] publicKey = account.getPublicKey();
        byte[] privateKey = account.getPrivateKey();
        String encodedPublicKey = null;
        String encodedPrivateKey = null;
        filepath = filepath.toAbsolutePath();
        
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
        
        String sql = String.format(
        "INSERT OR REPLACE INTO ACCOUNTS " +
        "(PRIVATEKEY, PUBLICKEY, ALIAS) " + 
        "VALUES (\'%s\', \'%s\', \'%s\');",
        encodedPrivateKey,encodedPublicKey,alias);

        //Connection to database.
        try {
            connection = connect(filepath);
            System.out.println("Connected to database: "+filepath);
            stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
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
    
    private Connection connect(Path filepath){
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:"+filepath);
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
    
    
}
