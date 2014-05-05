package criptovaro;

public class Account {
    private String nickname;
    private byte[] privateKey;
    private byte[] publicKey;

    public void generateKeys() {
    }
    
    public Account()
    {
        
    }

    public Account(byte[] pubkey, byte[] privkey)
    {
        setKeys(pubkey, privkey);
    }
    
    public void setKeys(byte[] public_key, byte[] private_key)
    {
        this.privateKey = private_key;
        this.publicKey = public_key;
    }
    
    public byte[] getPublicKey()
    {
        return publicKey;    
    }
    
    public byte[] getPrivateKey()
    {
        return privateKey;    
    }
}
