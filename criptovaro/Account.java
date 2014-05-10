package criptovaro;

import java.io.*;
import java.security.*;

public class Account 
{
    private String nickname;
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
            System.out.println("Private key encoding format: " + privkey.getFormat());
            System.out.println("Public key encoding format: " + pubkey.getFormat());            
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
    }
    
    public void setKeys(byte[] public_key, byte[] private_key)
    {
        this.privateKey = private_key;
        this.publicKey = public_key;
    }
    
    public void setKeys(String public_key, String private_key) throws UnsupportedEncodingException
    {
        this.publicKey = public_key.getBytes("UTF8");
        this.privateKey = private_key.getBytes("UTF8");
    }
    
    public byte[] getPublicKey()
    {
        return publicKey;    
    }
    
    public byte[] getPrivateKey()
    {
        return privateKey;    
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
