package criptovaro;
 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.PrintWriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;

import java.nio.file.StandardOpenOption;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
 
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

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
    
    private void deleteAccount(String args[]) {
        System.out.println("Deleting account...");
        //wallet.deleteAccount();
    }
    
    public Transaction transferFunds(byte source[], String buddy, BigDecimal amount) {
        byte[] target = null;
        target = getBuddiesKey(buddy);
        System.out.println("Transferring "+amount+" to buddy "+target+".");
        Transaction transaction = new Transaction(source, target, amount);
        //(new TransactionMessage(transaction)).bcast();
        System.out.println("Funds transferred.");
        return transaction;
    }
    
    public BigDecimal checkBalance(ArrayList<Account> accounts) {
        BigDecimal balance = new BigDecimal(0);
        for(Account account : accounts) {
            checkBalance(account);
        }
        return balance;
    }
        
    public void exportKey(String filename, String key) {
        try {
            File file = new File(filename+".key");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(key);
            output.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
    
    public void addBuddy(String buddy, Path key) {
        //Append to files buddies.txt the pair <buddy,key>
        String encodedKey = null;
        Path buddies = Paths.get("buddies.txt");
        //Read key
        if(Files.notExists(buddies)) {
            try {
                File file = new File("buddies.txt");
                BufferedWriter output = new BufferedWriter(new FileWriter(file));
                output.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        
        } else if(Files.notExists(key.toAbsolutePath())){
            System.out.println("Key file doesn't exist.");
            return;
        } else {
            File file = new File(key.toAbsolutePath().toString());
            try {
                FileReader reader = new FileReader(file);
                char[] chars = new char[(int) file.length()];
                reader.read(chars);
                encodedKey = new String(chars);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
               
        }
        //Add key to buddies.txt
        try {
            File file = new File("buddies.txt");
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(buddy+","+encodedKey);
            bw.close();
            System.out.println("Buddy added to contact.");
        } catch (IOException e) {
            System.out.println("Could not add buddy.");
        }
    }

    public byte[] getBuddiesKey(String buddy) {
        File file = null;
        //FileReader fr = null;
        //BufferedReader br = null;
        byte[] decodedKey = null;
        String key = null;

        try {
            file = new File("buddies.txt");
            //fr = new FileReader(file);
            //br = new BufferedReader(fr);
            //String line = null;

            String[] buddyInfo = null;
            System.out.println("buddy: "+buddy);
            try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                for(String line; (line = br.readLine()) != null; ) {
                    System.out.println(line);
                    buddyInfo = line.split(",");
                    if(buddy.equals(buddyInfo[0])) {
                        System.out.println("buddyinfo[0]: "+buddyInfo[0]);
                        System.out.println("buddyinfo[1]: "+buddyInfo[1]);
                        key = buddyInfo[1];
                    }
                    // process the line.
                }
                // line is not visible here.
            }
            
            //while ((line = br.readLine()) != null) {
            ///    System.out.println(line);
            //    buddyInfo = line.split(",");
            //    if(buddy.equals(buddyInfo[0])) {
            //        key = buddyInfo[1];
            //    }
            //}
        } catch (Exception e) {
                e.printStackTrace();
        } //finally {
          //  try {
          //       if (null != fr) {
          //          fr.close();
          //      }
            //} catch (Exception e2) {
            //    e2.printStackTrace();
            //}
        //}
        System.out.println("buddie's key: "+key);
        
        BASE64Decoder d = new BASE64Decoder();
        
        try {
            decodedKey = d.decodeBuffer(key);
        } catch (IOException e) {
            System.out.println("Could not decode encoded key.");
        }
        
        return decodedKey;
    }
    
    public BigDecimal checkBalance(Account account) {
        BigDecimal balance = new BigDecimal(0);
        System.out.println("Checking balance for account: "+account.getAlias());
        byte[] publicKey = account.getPublicKey();
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        
        TransactionManager tm = new TransactionManager();
        transactions = tm.getAccountFunds(publicKey);
        for(Transaction transaction : transactions) {
            //Obtains transactions and sum it.
            balance = balance.add(transaction.getAmount());
        }
        System.out.println("Balance for account "+account.getAlias()+"("+account.getPublicKey()+","+account.getPrivateKey()+"): "+balance.toString());
        return balance;
    }
   
    //Status: Completed
    private boolean startMiner(byte[] publicKey, byte[] privateKey, int tcpPort, int httpPort) {
        int minerPid = 0;
        System.out.println("Starting Criptovaro miner...");
        Runtime rt = Runtime.getRuntime();
 
        BASE64Encoder e = new BASE64Encoder();
        String encodedPublicKey = e.encode(publicKey);
        String encodedPrivateKey = e.encode(privateKey);
 
        Process p = null;
        String command[] = {
            "java",
            "Miner",
            "-publicKey", encodedPublicKey,
            "-privateKey", encodedPrivateKey,
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
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error waiting for process.");
            return false;
        } finally {
            p.destroy();
        }
        
        minerPid = obtainMinerPid();
        if(minerPid!=0) {
            wallet.setMinerPid(minerPid);
        }
        System.out.println("Miner started. Use command stop miner to stop the Criptovaro miner.");
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
 
    public void printTransactionStatus(String args[]) 
    {
        throw new UnsupportedOperationException();
    }
 
    public Transaction[] listTransactions(String args[]) 
    {
        throw new UnsupportedOperationException();
    }

    private void executePrompt(InputStream is) {
        Scanner sc = new Scanner(is);
        boolean running = true;

        while (running) {
            System.out.println("Type 'help' to see a list of commands or 'quit' to end.");
            String input;
            while (sc.hasNextLine()) {
                input = sc.nextLine().trim();

                //String input = scanner.nextLine().trim();
                String command = input.toLowerCase();
                String typed[] = null;
                String args[] = null;

                //Extract command
                if (command.startsWith("quit")) {
                    command = "quit";
                } else if (command.startsWith("help")) {
                    command = "help";
                } else {
                    typed = command.split("\\s+");
                    command = typed[0] + " " + typed[1];
                }
                System.out.println("command: " + command);

                //Extract arguments
                String arguments = null;
                int commandLength = command.length();
                arguments = input.substring(commandLength).trim();
                if (arguments.length() == 0) {
                    args = null;
                } else {
                    args = arguments.split("\\s+");
                }
                System.out.println("arguments: " + arguments);

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
                        for (int i = 0; i < args.length - 1; i++) {
                            if (args[i].equalsIgnoreCase("-path")) {
                                walletPath = Paths.get(args[i + 1]);
                            } else if (args[i].equalsIgnoreCase("-name")) {
                                walletName = args[i + 1];
                            }
                        }
                    } catch (NullPointerException np) {
                        System.out.println("Please specify a wallet alias.");
                        break;
                    }
                    createWallet(walletPath, walletName);
                    currentWallet = wallet;
                    break;
                case "use wallet":
                    System.out.println("Opening wallet...");
                    if ((args.length == 0 || args == null) && wallet != null) {
                        useWallet();
                        break;
                    }
                    Path path = null;
                    try {
                        if (args[0].equalsIgnoreCase("-path")) {
                            path = Paths.get(args[1]).toAbsolutePath();
                            if (path == null) {
                                System.out.println("Please specify a wallet path.");
                                break;
                            }
                        } else {
                            break;
                        }
                    } catch (NullPointerException np) {
                        System.out.println("Please specify a wallet path.");
                    }
                    useWallet(path);
                    break;
                case "current wallet":
                    args = null;
                    if (currentWallet == null) {
                        System.out.println("There is no current wallet in use. Use the command \'use wallet\' to select an active wallet.");
                    } else {
                        System.out.println("Current wallet: " + currentWallet.getWalletFile().toAbsolutePath());
                        if (currentWallet.getAccounts() != null) {
                            System.out.println("Accounts: ");
                            ArrayList<Account> activeAccounts = new ArrayList<Account>();
                            activeAccounts = currentWallet.getAccounts();
                            int i = 1;
                            for (Account account : activeAccounts) {
                                System.out.println(i + ".");
                                System.out.println("   Alias: " + account.getAlias());
                                System.out.println("   Private Key: " + account.getPrivateKey());
                                System.out.println("   Public Key: " + account.getPublicKey());
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
                        if (args[0].equalsIgnoreCase("-alias") && args[1] != null) {
                            accountName = args[1];
                        } else {
                            System.out.println("Please specify an account name.");
                            break;
                        }
                    } catch (NullPointerException np) {
                        System.out.println("Please specify an account name.");
                        break;
                    } catch (ArrayIndexOutOfBoundsException ai) {
                        System.out.println("Please specify an account name.");
                        break;
                    }
                    activeAccount = createAccount(accountName);

                    System.out.println("Account created.");
                    System.out.println("account {");
                    System.out.println("   alias: \"" + activeAccount.getAlias() + "\",");
                    System.out.println("   private key: \"" + activeAccount.getPrivateKey() + "\",");
                    System.out.println("   public key: \"" + activeAccount.getPublicKey() + "\"");
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
                    if (args == null && activeAccount != null && currentWallet != null) {
                        saveAccount(activeAccount, currentWallet.getWalletFile());
                        System.out.println("Account saved to wallet.");
                        break;
                    }

                    // Validates that an active account exists before saving it to a wallet file.
                    if (args != null && activeAccount == null) {
                        System.out.println("Please create an account first.");
                        break;
                    }

                    // Validates presence of a wallet file.
                    if (args != null && currentWallet.getWalletFile() == null) {
                        System.out.println("Please specify a wallet.");
                        break;
                    }

                    try {
                        if (args[0].equalsIgnoreCase("-wallet")) {
                            walletFile = Paths.get(args[1]).toAbsolutePath();
                        }
                        if (Files.notExists(walletFile)) {
                            System.out.println("Please specify a valid wallet path.");
                            break;
                        }
                        saveAccount(activeAccount, walletFile);
                    } catch (NullPointerException np) {
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
                    /*  Usage: check balance -all : Checks balance for all accounts in the wallet
                                 *         check balance -account <alias> : Checks balance for a particular account.
                                 *         check balance -wallet <wallet> -all
                                 *         check balance -wallet <wallet> -account <alias>
                                 *  1. Go to current wallet.
                                 *  2. Obtain public key and private key for the account's alias.
                                 *  3. Verifies that the alias matches (ignores case).
                                 *  4. Calls the check balance function.
                                 *  5. Returns balance.
                                 */
                    System.out.println("check balance.");
                    String alias = null;
                    Path walletFilepath = null;
                    boolean all = false;

                    try {
                        for (int i = 0; i < args.length - 1; i++) {
                            if (args[i].equalsIgnoreCase("-account")) {
                                alias = args[i + 1];
                            } else if (args[i].equalsIgnoreCase("-wallet")) {
                                walletFilepath = Paths.get(args[i + 1]).toAbsolutePath();
                            } else if (args[i].equalsIgnoreCase("-all")) {
                                all = true;
                            }
                        }
                    } catch (NullPointerException np) {
                        System.out.println("Please specify a valid account.");
                        break;
                    }

                    if (all == true && walletFilepath == null && alias == null) {
                        //check balance -all
                        ArrayList<Account> accounts = new ArrayList<Account>();
                        checkBalance(currentWallet.getAccounts());
                    } else if (alias != null && all == false && walletFilepath == null) {
                        //check balance -account <alias>
                        if (currentWallet != null) {
                            ArrayList<Account> walletAccounts = new ArrayList<Account>();
                            walletAccounts = currentWallet.getAccounts();
                            for (Account account : walletAccounts) {
                                if (account.getAlias().equals(alias)) {
                                    checkBalance(account);
                                }
                            }
                        }
                    } else if (walletFilepath != null && all == true && alias == null) {
                        //check balance -wallet <wallet> -all
                        //ToDo: Verify that filepath exists.
                        Wallet wallet = new Wallet();
                        wallet.setWalletFile(walletFilepath);
                        ArrayList<Account> walletAccounts = new ArrayList<Account>();
                        walletAccounts = wallet.getAccounts();
                        checkBalance(walletAccounts);
                    } else if (walletFilepath != null && alias != null && all == false) {
                        //check balance -wallet <wallet> -account <alias>
                        //ToDo: Verify that filepath exists.
                        Wallet wallet = new Wallet();
                        wallet.setWalletFile(walletFilepath);
                        ArrayList<Account> walletAccounts = new ArrayList<Account>();
                        walletAccounts = wallet.getAccounts();
                        for (Account account : walletAccounts) {
                            if (account.getAlias().equals(alias)) {
                                checkBalance(account);
                            }
                        }
                    }

                    break;
                case "list transactions":
                    System.out.println("List transactions.");
                    break;
                case "export key":
                    //Exports the public key to a .key file
                    //Usage: export key
                    System.out.println("export key");
                    String account = null;
                    byte[] unencodedKey = null;
                    String encodedKey = null;
                    String filename = null;

                    if (args[0].equalsIgnoreCase("-account")) {
                        account = args[1];
                    }

                    Account acc = new Account();
                    ArrayList<Account> accounts = new ArrayList<Account>();
                    accounts = currentWallet.getAccounts();
                    for (Account ac : accounts) {
                        if (ac.getAlias().equals(account)) {
                            acc = ac;
                        }
                    }

                    System.out.println("account: " + acc.getAlias());
                    unencodedKey = acc.getPublicKey();
                    System.out.println("unencoded key: " + unencodedKey);
                    encodedKey = acc.encodeKey(unencodedKey);
                    System.out.println("encoded key: " + encodedKey);
                    filename = acc.getAlias();
                    System.out.println("filename: " + filename);
                    exportKey(filename, encodedKey);

                    break;
                case "add buddy":
                    String name = null;
                    Path key = null;
                    try {
                        for (int i = 0; i < args.length - 1; i++) {
                            if (args[i].equalsIgnoreCase("-name")) {
                                name = args[i + 1];
                            } else if (args[i].equalsIgnoreCase("-key")) {
                                key = Paths.get(args[i + 1]).toAbsolutePath();
                            }
                        }
                    } catch (NullPointerException np) {
                        np.printStackTrace();
                    }
                    if (Files.notExists(key)) {
                        System.out.println("Key file doesn't exist.");
                        break;
                    }
                    if (name == null) {
                        System.out.println("Buddy name can't be empty.");
                        break;
                    }
                    addBuddy(name, key);
                    System.out.println("Buddy added.");
                    break;
                case "transfer funds":
                    //Transfers funds to a buddy.
                    System.out.println("Transfer funds.");
                    byte source[] = null;
                    String target = null;
                    BigDecimal amount = null;

                    try {
                        for (int i = 0; i < args.length - 1; i++) {
                            if (args[i].equalsIgnoreCase("-source")) {
                                source = args[i + 1].getBytes();
                            } else if (args[i].equalsIgnoreCase("-target")) {
                                target = args[i + 1];
                            } else if (args[i].equalsIgnoreCase("-amount")) {
                                amount = new BigDecimal(args[i + 1]);
                            }
                        }
                    } catch (NullPointerException np) {
                        System.out.println("Please specify a wallet alias.");
                        break;
                    } catch (NumberFormatException nf) {
                        System.out.println("Please enter a valid amount.");
                        break;
                    }

                    transferFunds(source, target, amount);
                    break;
                case "start miner":
                    String minerAccount = null;
                    byte[] publicKey = null;
                    byte[] privateKey = null;
                    int tcpPort = 0;
                    int httpPort = 0;
                    for (int i = 0; i < args.length - 1; i++) {
                        if (args[i].equalsIgnoreCase("-account")) {
                            minerAccount = args[i + 1];
                            if (minerAccount == null) {
                                System.out.println("Please specify an account.");
                                break;
                            }
                        } else if (args[i].equalsIgnoreCase("-tcp")) {
                            tcpPort = Integer.parseInt(args[i + 1]);
                            if (tcpPort <= 0 || tcpPort > 65535) {
                                tcpPort = 12345;
                                System.out.println("Invalid port number, running on default (12345)");
                            }
                        } else if (args[i].equalsIgnoreCase("-http")) {
                            httpPort = Integer.parseInt(args[i + 1]);
                            if (httpPort <= 0 || httpPort > 65535) {
                                httpPort = 8080;
                                System.out.println("Invalid port number, running on default (8080)");
                            }
                        }
                    }
                    ArrayList<Account> minerAccounts = currentWallet.getAccounts();

                    for (Account minAccount : minerAccounts) {
                        if (minAccount.getAlias().equals(minerAccount)) {
                            publicKey = minAccount.getPublicKey();
                            privateKey = minAccount.getPrivateKey();
                        }
                    }
                    System.out.println("public key: " + publicKey);
                    System.out.println("private key: " + privateKey);
                    System.out.println("http port: " + httpPort);
                    System.out.println("tcp port: " + tcpPort);
                    System.out.println("Starting miner for account " + minerAccount);
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
        sc.close();
    }
        
    private static void printHelp() {
        System.out.println("List of commands: ");
        System.out.println("create wallet [-path <Wallet path>] [-name <Wallet filename>]");    
        System.out.println("\t Creates a wallet file with the specified name in the specified path, and sets it as the current wallet.\n");
        System.out.println("create account [-name <Account name>]");
        System.out.println("\t Creates an account, specifies an alias for easy reference, and sets the account as the current account.\n");
        System.out.println("save account");
        System.out.println("\t Saves the current account in the current wallet.\n");
        System.out.println("save account [-wallet <Wallet: filename>]");
        System.out.println("\t Saves the current account in the specified wallet.\n");
        System.out.println("start miner [-pubkey <Public Key>] [-privkey <Private Key>] [-tcp <TCP Port>] [-http <HTTP Port>]");
        System.out.println("\t Starts a new miner process and an HTTP server in the specified ports.\n");
        System.out.println("add buddy [-name <Buddy name>] [-key <Key file>");
        System.out.println("\t Adds an account to the list of buddies.\n");
        System.out.println("export key [-account <Account alias>");
        System.out.println("\t Exports an account to a .key file.\n");
        System.out.println("transfer funds [-source <Account alias>] [-target <Buddy name>] [-amount <Amoung of criptovaros>]");
        System.out.println("\t Transfers criptovaros from one account to another.\n");
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