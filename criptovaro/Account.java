package criptovaro;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;

import java.security.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.Types;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

public class Account 
{
    private String alias;
    private byte[] privateKey;
    private byte[] publicKey;
    private KeyPairGenerator kpg = null;
    private int minerPID = 0;
    private int minerPort = 0;
    
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

    public int getMinerPid() {
        return this.minerPID;
    }

    public void setMinerPid(int pid) {
        this.minerPID = pid;
    }

    public int getMinerPort() {
        return this.minerPort;
    }
    
    public void setMinerPort(int port) {
        this.minerPort = port;
    }
}
