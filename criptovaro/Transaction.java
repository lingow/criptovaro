package criptovaro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.io.Serializable;

import java.math.BigDecimal;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;

import java.security.SignatureException;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import java.security.spec.PKCS8EncodedKeySpec;

import java.security.spec.X509EncodedKeySpec;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import sun.misc.BASE64Decoder;

/*
 * To use this class, one must do the following:
 * 1. First instanciate an object of the class with the source account, destination account and amount.
 * 2. Add the inputs necessary to cover the amount; 
 * 3. Call the Prepare() method to finalize the transaction. Prepare requires the source account to use it's private key. Prepare will sign 
 *     the transaction and automatically generate the outputs based on the inputs and amount.
 */
public class Transaction implements Serializable {
    @SuppressWarnings("compatibility:-894874531013678188")
    private static final long serialVersionUID = 1L;
    private byte[] source; //public key for source account
    private byte[] destination; //public key for destination account
    private BigDecimal amount;
    private Date timestamp;
    private byte[] digitalSignature;
    private byte[] spentBy;
    private byte[] originTransaction;
    private TransactionType type;

    public Transaction(byte[] source, byte[] destination, BigDecimal amount)
    {
        this.source = source;
        this.destination = destination;
        this.amount = amount;
        type = TransactionType.REGULAR;
        
        spentBy = new byte[]{};
        originTransaction = new byte[]{};
    }
    
    
    Transaction(ResultSet rs) throws SQLException, IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        
        this.type= TransactionType.values()[(rs.getInt("TRANSTYPE"))];
        this.originTransaction= decoder.decodeBuffer( rs.getString("ORIGINTRANS"));
        this.source= decoder.decodeBuffer( rs.getString("FROMKEY"));
        this.destination= decoder.decodeBuffer(rs.getString("TOKEY"));
        this.amount= BigDecimal.valueOf(rs.getLong("AMOUNT"));
        this.digitalSignature= decoder.decodeBuffer(rs.getString("SIGNATURE")); 
        this.timestamp= rs.getDate("TIMESTAMP");
        this.spentBy= decoder.decodeBuffer(rs.getString("spentby"));
    }

    public boolean verify() 
    {
        boolean retVal = false;
        
        try 
        {
            ByteArrayOutputStream theBytes = new ByteArrayOutputStream();
            ObjectOutputStream oos =  new ObjectOutputStream(theBytes);

            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initVerify(KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(source)));
            
            //Add the data to verify
            oos.writeLong(timestamp.getTime());
            oos.writeUTF(amount.toString());
            oos.write(source);
            oos.write(destination);
            oos.flush();
            sig.update(theBytes.toByteArray());
            
            retVal = sig.verify(digitalSignature);
        } 
        catch (NoSuchAlgorithmException e) 
        {
            System.out.println(e.toString());
            e.printStackTrace();
        } 
        catch (InvalidKeySpecException e) 
        {
            System.out.println(e.toString());
            e.printStackTrace();
        } 
        catch (InvalidKeyException e) 
        {
            System.out.println(e.toString());
            e.printStackTrace();
        } 
        catch (SignatureException e) 
        {
            System.out.println(e.toString());
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        return retVal;
    }

    public byte[] getDigitalSignature()
    {
        return digitalSignature;
    }
    
    public boolean Sign(Account sourceAccount)
    {
        boolean retVal = false;
        //Validate we have a private key that allegedly matches the source public key.
        if(sourceAccount == null&& sourceAccount.getPrivateKey() != null && Arrays.equals(sourceAccount.getPublicKey(), source))
            return false;
        
        try
        {
            ByteArrayOutputStream theBytes = new ByteArrayOutputStream();
            ObjectOutputStream oos =  new ObjectOutputStream(theBytes);
            Signature dsa = Signature.getInstance("SHA1withDSA");
            
            //Initialize the signature object with the private key
            dsa.initSign(KeyFactory.getInstance("DSA").generatePrivate(new PKCS8EncodedKeySpec(sourceAccount.getPrivateKey())));
            
            //Get all the bytes for the transaction: source + destination + inputs + outputs + timestamp
            //Include timestamp for the digital signature to increase security level.
            timestamp = new Date(System.currentTimeMillis());
            oos.writeLong(timestamp.getTime());
            
            //Write transaction amount
            oos.writeUTF(amount.toString());
            
            //Write source account bytes
            oos.write(source);
            
            //Write destination bytes
            oos.write(destination);
            
            //Flush the buffer
            oos.flush();
            
            //Get the signature
            dsa.update(theBytes.toByteArray());
            digitalSignature = dsa.sign();
            
            retVal = true;
        }
        catch(NoSuchAlgorithmException nsae)
        {
            nsae.printStackTrace();
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
        } 
        catch (InvalidKeySpecException e) 
        {
            e.printStackTrace();
        } 
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
        } 
        catch (SignatureException e)
        {
            e.printStackTrace();
        }

        return retVal;
    }
    
    
    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }

    public byte[] getDestination() {
        return destination;
    }

    public void setDestination(byte[] destination) {
        this.destination = destination;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public byte[] getOriginTransaction() {
        return originTransaction;
    }

    public void setOriginTransaction(byte[] originTransaction) {
        this.originTransaction = originTransaction;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp() {
        this.timestamp = new Date();
    }

    public byte[] getSpentBy() {
        return spentBy;
    }

    public void setSpentBy(byte[] spentBy) {
        this.spentBy = spentBy;
    }

    public TransactionType getType() 
    {
        return type;
    }
    
    public boolean equals(Transaction t ){
        return Arrays.equals(this.digitalSignature, t.getDigitalSignature());
    }

    public void setType(TransactionType type) 
    {
        this.type = type;
}
}
