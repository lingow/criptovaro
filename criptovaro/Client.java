package criptovaro;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.file.Path;

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
 
public class Client {
    Wallet wallet = null;
    
    private void init() throws IOException {
        
    }
    
    //Status: Completed
    private Wallet createWallet(Path walletPath, String walletAlias) {
        if(walletPath == null) {
            System.out.println("Please specify a filepath.");
            return null;
        } else if (walletAlias == null) {
            System.out.println("Please specify a wallet alias.");
            return null;
        }
        
        System.out.println("Attempting to create wallet "+walletAlias+" at path \'"+walletPath.toString()+"\'");
        wallet = new Wallet(walletPath, walletAlias);
        
        return wallet;    
    }
    
    private void useWallet(Path path) {
        wallet = new Wallet();
        wallet.setWalletPath(path);
        //jalar accounts
        //jalar alias
    }
    
    //Status: Completed
    public Account createAccount(String name) {
        Account account = new Account();
        account.setNickname(name);
        account.generateKeys();
        wallet.addAccountToWallet(account, wallet);
        return account;
    }
    
    public Account[] getAccounts() {
        return null;
    }
    
    private void deleteAccount(String args[]) {
        System.out.println("Deleting account...");
        //wallet.deleteAccount();
    }
    
    public Transaction transferFunds(byte source[], byte target[], BigDecimal amount) {
        Transaction transaction = new Transaction(source, target, amount);
        return transaction;
    }
    
    public BigDecimal getBalance(String args) {
        return null;
    }
   
    //Status: Completed
    private boolean startMiner(String publicKey, String privateKey, int tcpPort, int httpPort) {
        int minerPid = 0;
        System.out.println("Starting Criptovaro miner...");
        Runtime rt = Runtime.getRuntime();
 
        Process p = null;
        String command[] = {
            "java",
            "Miner",
            "-publicKey", publicKey,
            "-privateKey", privateKey,
            "-tcp_port", Integer.toString(tcpPort),
            "-web_port", Integer.toString(httpPort)
        };
   
        try {
            p = rt.exec(command);
            p.getErrorStream();
        } catch (IOException io) {
            io.printStackTrace();
            System.out.println("Error starting miner.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error waiting for process.");
            return false;
        } finally {
            p.destroy();
        }
        
        minerPid = obtainMinerPid();
        if(minerPid!=0) {
            wallet.setMinerPid(minerPid);
        }
        
        
        return true;
    }
   
    //Status: Completed
    private boolean stopMiner() {
        System.out.println("Stoping Criptovaro miner...");
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        int pid = wallet.getMinerPid();
        
        String command[] = {"kill","-s TERM"+Integer.toString(pid)};
       
        try {
            p = rt.exec(command);
            p.getErrorStream();
            p.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            p.destroy();
        }
       
        return true;
    }
 
    public void checkBalance(String args[]) {
    }
 
    public void printTransactionStatus(String args[]) {
    }
 
    public Transaction[] listTransactions(String args[]) {
        return null;
    }
    
    private void executePrompt() {
        final Semaphore semaphore = new Semaphore(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Scanner scanner = new Scanner(System.in)) {
                    boolean running = true;
                    while (running) {
                        System.out.println("Type 'help' to see a list of commands or 'quit' to end.");
                        String input = scanner.nextLine().trim();
                        String command = input.toLowerCase();
                        String typed[] = null;
                        String args[] = null;
                       
                        //Extract command
                        if(command.startsWith("quit")) {
                            command = "quit";
                        } else if (command.startsWith("help")) {
                            command = "help";  
                        } else {
                            typed = command.split("\\s+");
                            command = typed[0] + " " + typed[1];
                        }
                        System.out.println("command: "+command);
                        
                        //Extract arguments
                        String arguments = null;
                        int commandLength = command.length();
                        arguments = input.substring(commandLength).trim();
                        if(arguments.length() == 0) {
                            args = null;
                        } else {
                            args = arguments.split("\\s+");
                        }
                        System.out.println("arguments: "+ arguments);
                       
                        switch (command) {
                            case "quit":
                                semaphore.release();
                                running = false;
                                break;
                            case "help":
                                printHelp();
                                break;
                            case "create wallet":
                                System.out.println("Creating wallet...");
                                Path walletPath = null;
                                String walletAlias = null;
                                try {
                                    for(int i=0; i<args.length-1;i++) {
                                        if(args[i].toLowerCase().equals("-path")) {
                                            walletPath = Paths.get(args[i+1]);
                                        } else if(args[i].toLowerCase().equals("-alias")) {
                                            walletAlias = args[i+1];
                                        }
                                    }
                                } catch(NullPointerException np) {
                                    System.out.println("Please specify a wallet alias.");
                                    break;
                                }
                                createWallet(walletPath, walletAlias);
                                break;
                            case "use wallet":
                                System.out.println("Opening wallet...");
                                Path path = null;
                                try {
                                    if(args[0].equals("-path")) {
                                        path = Paths.get(args[1]);
                                        if(path == null) {
                                            System.out.println("Please specify a wallet path.");
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                } catch(NullPointerException np) {
                                    System.out.println("Please specify a wallet path.");
                                }
                                useWallet(path);
                                break;
                            case "create account":
                                System.out.println("Create account.");
                                String accountName = null;
                            
                                if(wallet==null) {
                                    System.out.println("Please create a wallet first.");
                                    break;
                                }
                            
                                try {
                                    if(args[0].equals("-name")) {
                                        accountName = args[1];
                                        if(accountName == null) {
                                            System.out.println("Please specify a wallet path.");
                                            break;
                                        }
                                    }
                                } catch(NullPointerException np) {
                                    System.out.println("Please specify a wallet path.");
                                }
                                createAccount(accountName);
                                break;
                            case "delete account":
                                System.out.println("Delete account.");
                                deleteAccount(args);
                                break;
                            case "check balance":
                                System.out.println("Check balance.");
                                checkBalance(args);
                                break;
                            case "list transactions":
                                System.out.println("List transactions.");
                                listTransactions(args);
                                break;
                            case "transfer funds":
                                System.out.println("Transfer funds.");
                                byte source[] = null;
                                byte target[] = null;
                                BigDecimal amount = null; 
                                
                                try {
                                    for(int i=0; i<args.length-1;i++) {
                                        if(args[i].toLowerCase().equals("-source")) {
                                            source = args[i+1].getBytes();
                                        } else if(args[i].toLowerCase().equals("-target")) {
                                            target = args[i+1].getBytes();
                                        } else if(args[i].toLowerCase().equals("-amount")) {
                                            amount = new BigDecimal(args[i+1]);
                                        }
                                    }
                                } catch(NullPointerException np) {
                                    System.out.println("Please specify a wallet alias.");
                                    break;
                                }
                                transferFunds(source, target, amount);
                                break;
                            case "start miner":
                                String publicKey = null;
                                String privateKey = null;
                                int tcpPort = 0;
                                int httpPort = 0;
                                for(int i=0; i<args.length-1;i++) {
                                    if(args[i].toLowerCase().equals("-pubkey")) {
                                        publicKey = args[i+1];
                                        if(publicKey == null) {
                                            System.out.println("Please specify a valid public key.");
                                            break;
                                        }
                                    } else if(args[i].toLowerCase().equals("-privkey")) {
                                        privateKey = args[i+1];
                                        if(privateKey == null) {
                                            System.out.println("Please specify a valid private key.");
                                            break;
                                        }
                                    } else if(args[i].toLowerCase().equals("-tcp")) {
                                        tcpPort = Integer.parseInt(args[i+1]);
                                        if(tcpPort <= 0 || tcpPort > 65535) {
                                            tcpPort = 12345;
                                            System.out.println("Invalid port number, running on default (12345)");
                                        }
                                    } else if(args[i].toLowerCase().equals("-http")) {
                                        httpPort = Integer.parseInt(args[i+1]);
                                        if(httpPort <=0 || httpPort > 65535) {
                                            httpPort = 8080;
                                            System.out.println("Invalid port number, running on default (8080)");
                                        }
                                    }
                                }    
                                System.out.println("public key: "+publicKey);
                                System.out.println("private key: "+privateKey);
                                System.out.println("http port: "+httpPort);
                                System.out.println("tcp port: "+tcpPort);
                                startMiner(publicKey, privateKey, tcpPort, httpPort);
                                break;
                            case "stop miner":
                                stopMiner();
                                break;
                            default:
                                System.out.println("Not a valid command.");
                                break;
                        }
                    }
                }
            }
        }).start();
        try {
            semaphore.acquire();
        } catch (InterruptedException ie) {
            System.out.println("Error waiting for process.");
            ie.printStackTrace();
        }
        
    }
   
    private static void printHelp() {
        System.out.println("List of commands: ");
        System.out.println("create wallet -path [Wallet path] -alias [Alias]");
        System.out.println("\t Creates a wallet in the specified path and assigns an alias for easy reference.\n");
        System.out.println("start miner -pubkey [Public Key] -privkey [Private Key] -tcp [TCP Port] -http [HTTP Port]");
        System.out.println("\t Starts a new miner process and an HTTP server in the specified ports.\n");
        System.out.println("stop miner");
        System.out.println("\t Stops the miner process that is running in the specified port and its associated HTTP server.\n");
    }
   
    private void close() throws IOException {
        System.out.println("Closing Criptovaro...");
    }

    //Status: Completed
    private int obtainMinerPid() {
        int pid = 0;
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        String result = null;
        String command = "jps | grep Miner";
       
        try {
            p = rt.exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            p.waitFor();
            result = stdInput.readLine();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            p.destroy();
        }
        if(result!=null) {
            pid = Integer.parseInt(result.split("\\s+")[0]);
        }
        return pid;
    }
 
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Criptovaro 0.1");
        final Client client = new Client();
        client.init();
        client.executePrompt();
        client.close();
        System.exit(0);
    }
 
}