package criptovaro;
import java.io.File;

import java.nio.file.Path;

import java.util.ArrayList;

public class Wallet {
    private String alias;
    private int pid;
    private Path filepath;
    ArrayList<Account> accounts;
    
    public Wallet() {
        alias = null;
        pid = 0;
        filepath = null;
        accounts = null;
    }
    
    public Wallet(Path walletPath, String walletAlias) {
        try {
            //Connect to JDBC
            //Create a table in the desired path.
            //if(success) {
            filepath = walletPath;
            alias = walletAlias;
            accounts = new ArrayList<Account>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setWalletPath(Path filepath) {
        this.filepath = filepath;
    }
    
    public Path getWalletPath() {
        return filepath;
    }
    
    public void setMinerPid(int pid) {
        this.pid = pid;
    }
    
    public int getMinerPid() {
        return pid;
    }

    public void addAccountToWallet(Account account, Wallet wallet) {
        //if path exists and alias exists, add row to table wallet
    }

    public void deleteAccount(Account acc) {
        //if path exists and alias exists and account exists, remove row from table wallet
    }

    public void getAccount(String name) {
        
    }

    public ArrayList getAccounts() {
        return accounts;
    }
}
