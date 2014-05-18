package criptovaro;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
 
public class Client {
    Wallet wallet = null;
    Wallet currentWallet = null;
    Account activeAccount = null;
    Path currentPath = null;
    
    private void init() throws IOException {
        
    }
    
    //Status: Completed
    private Wallet createWallet(Path dir, String walletName) {
        System.out.println("createWallet("+dir.toString()+", "+walletName+")");
        //Validates the arguments.
        //If the directory is empty, it assigns the default directory.
        //If the wallet name is empty, it returns an error.
        if(dir == null) {
            dir = Paths.get("wallets");
            return null;
        } else if (walletName == null) {
            System.out.println("Please specify a wallet alias.");
            return null;
        }
        
        //Formats the wallet directory by trimming ending slashes "/"
        String filepath = dir.toString();
        if(filepath.endsWith("/")) {
            filepath = filepath.substring(0,filepath.length()-filepath.length());
            dir = Paths.get(filepath);
        }
        
        //Sets the wallet path to the default directory.
        if(filepath.equals("wallets")) {
           dir = Paths.get("wallets").toAbsolutePath(); 
        } 
        
        //Validates if a wallet file already exists.
        if(!Files.notExists(Paths.get(dir.toString()+"/"+walletName+".wallet"))) {
            System.out.println("Wallet file already exists.");
            return null;
        }
        
        //Validates if the directory exists. If it doesn't, the client creates it.
        //If there's an error, it falls back to the default directory.
        if(Files.notExists(dir)){
            try {
                Files.createDirectory(dir);
            } catch (IOException e) {
                System.out.println("Directory could not be created.");
                dir = Paths.get("wallets");
                dir = dir.toAbsolutePath();
                e.printStackTrace();
            }
        } 
        
        System.out.println("Attempting to create wallet "+walletName+" at path \'"+dir.toString()+"\'");
        dir = Paths.get(filepath+"/"+walletName+".wallet");
        
        //Creates the wallet
        wallet = new Wallet(dir);
        System.out.println("Wallet created: "+dir.toAbsolutePath().toString());
        return wallet;    
    }
    
    //Status: Completed
    private void useWallet() {
        if(wallet != null) {
            currentWallet = wallet;
            System.out.println("Using wallet: "+currentWallet.getWalletFile().toAbsolutePath());
        }
    }
    
    //Status: Completed
    private void useWallet(Path path) {
        wallet = new Wallet();
        wallet.setWalletFile(path);
        wallet.getAccounts();        
        currentWallet = wallet;
        System.out.println("Using wallet: "+currentWallet.getWalletFile().toAbsolutePath());
    }
    
    //Status: Completed
    public Account createAccount(String name) {
        Account account = new Account();
        account.setAlias(name);
        account.generateKeys();
        return account;
    }
    
    //Status: Completed
    public void saveAccount(Account account, Path filepath) {
        account.saveToWallet(account, filepath);
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
    
    public BigDecimal checkBalance(String args) {
        BigDecimal balance = new BigDecimal(0);
        
        return balance;
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
    
    private void executePrompt(InputStream in) {
        try (Scanner scanner = new Scanner(in)) {
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
                        running = false;
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "create wallet":
                        System.out.println("Creating wallet...");
                        Path walletPath = Paths.get("wallets");
                        String walletName = null;
                        try {
                            for(int i=0; i<args.length-1;i++) {
                                if(args[i].equalsIgnoreCase("-path")) {
                                    walletPath = Paths.get(args[i+1]);
                                } else if(args[i].equalsIgnoreCase("-name")) {
                                    walletName = args[i+1];
                                }
                            }
                        } catch(NullPointerException np) {
                            System.out.println("Please specify a wallet alias.");
                            break;
                        }
                        createWallet(walletPath, walletName);
                        currentWallet = wallet;
                        break;
                    case "use wallet":
                        System.out.println("Opening wallet...");
                        if((args.length == 0 || args == null) && wallet!=null) {
                            useWallet();
                            break;
                        }
                        Path path = null;
                        try {
                            if(args[0].equalsIgnoreCase("-path")) {
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
                    case "current wallet":
                        args = null;
                        if(currentWallet == null) {
                            System.out.println("There is no current wallet in use. Use the command \'use wallet\' to select an active wallet.");
                        } else {
                            System.out.println("Current wallet: "+wallet.getWalletFile().toAbsolutePath());
                            if(wallet.getAccounts()!=null) {
                                System.out.println("Accounts: ");
                                ArrayList<Account> activeAccounts = new ArrayList<Account>();
                                activeAccounts = wallet.getAccounts();
                                int i = 1;
                                for (Account account : activeAccounts) {
                                    System.out.println(i+":");
                                    System.out.println("   Alias: "+account.getAlias());
                                    System.out.println("   Private Key: "+account.getPrivateKey());
                                    System.out.println("   Public Key: "+account.getPublicKey());
                                    i++;
                                }
                            } else {
                                System.out.println("Your wallet is empty.");
                            }
                                    
                        }
                        break;
                    case "create account":
                        System.out.println("Create account.");
                        String accountName = null;
                    
                        try {
                            if(args[0].equalsIgnoreCase("-alias") && args[1]!=null) {
                                accountName = args[1];
                            } else {
                                System.out.println("Please specify an account name.");
                                break;
                            }
                        } catch(NullPointerException np) {
                            System.out.println("Please specify an account name.");
                            break;
                        } catch(ArrayIndexOutOfBoundsException ai) {
                            System.out.println("Please specify an account name.");
                            break;
                        }
                        activeAccount = createAccount(accountName);
                        
                        System.out.println("Account created.");
                        System.out.println("account {");
                        System.out.println("   alias: \""+activeAccount.getAlias()+ "\",");
                        System.out.println("   private key: \""+activeAccount.getPrivateKey()+ "\",");
                        System.out.println("   public key: \""+activeAccount.getPublicKey()+"\"");
                        System.out.println("}");
                        System.out.println("Save your account to a wallet using the  \'save account\' command.");                                
                        break;
                    
                    //*
                     /* Saves an account to a wallet file. 
                      * save account
                      *    Saves active account to the current wallet.
                      * save account -wallet <filepath>
                      *    Saves the active account to the specified wallet file.
                      */ 
                    case "save account":
                        System.out.println("command: save account.");
                        String walletAccount = null;
                        Path walletFile = null;
                        
                        // Simplest case. Saves the active account to the current wallet.
                        if(args==null && activeAccount!=null && currentWallet!=null) {
                            saveAccount(activeAccount, currentWallet.getWalletFile());
                            System.out.println("Account saved to wallet.");
                            break;
                        } 

                        // Validates that an active account exists before saving it to a wallet file.
                        if(args!=null && activeAccount == null) {
                            System.out.println("Please create an account first.");
                            break;
                        }
                    
                        // Validates presence of a wallet file.
                        if(args!=null && currentWallet.getWalletFile()==null) {
                            System.out.println("Please specify a wallet.");
                            break;
                        }
                        
                        try {
                            if(args[0].equalsIgnoreCase("-wallet")) {
                                walletFile = Paths.get(args[1]).toAbsolutePath();
                            }
                            if(Files.notExists(walletFile)) {
                                System.out.println("Please specify a valid wallet path.");
                                break;
                            }
                            saveAccount(activeAccount, walletFile);
                        } catch(NullPointerException np) {
                            System.out.println("Please specify a valid wallet path.");
                            break;
                        }
                       
                        break;
                    case "delete account":
                        System.out.println("Delete account.");
                        deleteAccount(args);
                        break;
                    case "check balance":
                        //* Checks the balance of an account with a particular alias.
                        /*  Usage: check balance -account <alias>
                         *         check balance -wallet <wallet> -account <alias>
                         *  1. Go to current wallet.
                         *  2. Obtain public key and private key for the account's alias.
                         *  3. Verifies that the alias matches (ignores case).
                         *  4. Calls the check balance function.
                         *  5. Returns balance.
                         */
                        System.out.println("Check balance.");
                    
                        try {
                            if(args[0].equalsIgnoreCase("-alias") && args[1]!=null) {
                                accountName = args[1];
                            } else {
                                System.out.println("Please specify an account name.");
                                break;
                            }
                        } catch(NullPointerException np) {
                            System.out.println("Please specify an account name.");
                            break;
                        } catch(ArrayIndexOutOfBoundsException ai) {
                            System.out.println("Please specify an account name.");
                            break;
                        }
                    
                        if(currentWallet!=null) {
                            currentWallet.getAccounts();
                        }    
                        
                        checkBalance(args);
                        break;
                    case "list transactions":
                        //
                        System.out.println("List transactions.");
                        listTransactions(args);
                        break;
                    case "transfer funds":
                        //
                        System.out.println("Transfer funds.");
                        byte source[] = null;
                        byte target[] = null;
                        BigDecimal amount = null; 
                        try {
                            for(int i=0; i<args.length-1;i++) {
                                if(args[i].equalsIgnoreCase("-source")) {
                                    source = args[i+1].getBytes();
                                } else if(args[i].equalsIgnoreCase("-target")) {
                                    target = args[i+1].getBytes();
                                } else if(args[i].equalsIgnoreCase("-amount")) {
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
                            if(args[i].equalsIgnoreCase("-pubkey")) {
                                publicKey = args[i+1];
                                if(publicKey == null) {
                                    System.out.println("Please specify a valid public key.");
                                    break;
                                }
                            } else if(args[i].equalsIgnoreCase("-privkey")) {
                                privateKey = args[i+1];
                                if(privateKey == null) {
                                    System.out.println("Please specify a valid private key.");
                                    break;
                                }
                            } else if(args[i].equalsIgnoreCase("-tcp")) {
                                tcpPort = Integer.parseInt(args[i+1]);
                                if(tcpPort <= 0 || tcpPort > 65535) {
                                    tcpPort = 12345;
                                    System.out.println("Invalid port number, running on default (12345)");
                                }
                            } else if(args[i].equalsIgnoreCase("-http")) {
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
   
    private static void printHelp() {
        System.out.println("List of commands: ");
        System.out.println("create wallet [-path <Wallet path>] [-name <Wallet filename>]");    
        System.out.println("\t Creates a wallet file with the specified name in the specified path, and sets it as the current wallet.\n");
        System.out.println("create account [-name <Account name>]");
        System.out.println("\t Creates an account, specifies an alias for easy reference, and sets the account as the current account.\n");
        System.out.println("save account");
        System.out.println("\t Saves the current account in the current wallet.\n");
        System.out.println("save account [-wallet <Wallet filename>]");
        System.out.println("\t Saves the current account in the specified wallet.\n");
        System.out.println("start miner [-pubkey <Public Key>] [-privkey <Private Key>] [-tcp <TCP Port>] [-http <HTTP Port>]");
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
        //Parse Command line Arguments
        LinkedList<String> arglist = new LinkedList(Arrays.asList(args));
        InputStream is = System.in;
        while (!arglist.isEmpty()){
            String s = arglist.pop();
            switch(s){
            case "-f":
                if(arglist.isEmpty()){
                    System.out.println("No file provided");
                } else {
                    String f = arglist.pop();
                    if (!(new File(f)).exists()){
                        System.out.println("Specified input file does not exist");
                    }
                    is = new FileInputStream(f);
                }
                break;
            default:
                break;
            }
        }
        client.executePrompt(is);
        client.close();
        System.exit(0);
    }
 
}