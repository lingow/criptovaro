package criptovaro;

import java.math.BigDecimal;

//Marco se la come 
public class Client {
    private Wallet wallet;
    private BGProcessInfo httpd;
    private BGProcessInfo[] miners;
    private PeerManager pm;

    public boolean startHttpd(int port) {
        return false;
    }

    public boolean startMiner(Account minerAccount, int port) {
        return true;
    }

    public Wallet createWallet() {
        return null;
    }

    public Account createAccount() {
        return null;
    }

    public void setCurrentWallet(String filePath) {
    }

    public void deleteAccount(String accountName) {
    }

    public Account[] getAccounts() {
        return null;
    }

    public void PrintBalances() {
    }

    public Transaction transferFunds(Account source, byte[] destination) {
        return null;
    }

    public BigDecimal getBalance(String accountName) {
        return null;
    }

    public void printTransactionStatus(Transaction tran) {
    }

    public Transaction[] getTransactionHistory() {
        return null;
    }

    public void initClient() {
    }
}
