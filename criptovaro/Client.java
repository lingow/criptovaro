package criptovaro;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
 
public class Client {
    //private Wallet wallet;
    //private CriptoVNetwork nwmanager;
    //private BGProcessInfo httpd;
    //private BGProcessInfo[] miners;
    private ArrayList<String> miners = new ArrayList<String>();
 
    private void init() throws IOException {
        System.out.println("Initializing Criptovaro...");
        final Semaphore semaphore = new Semaphore(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Scanner scanner = new Scanner(System.in)) {
                    boolean running = true;
                    while (running) {
                        System.out.println("Type 'help' to see a list of commands or 'quit' to end.");
                        String input = scanner.nextLine().trim().toLowerCase();
                        String command = null;
                       
                        if(input.startsWith("quit")) {
                            command = "quit";
                        } else if (input.startsWith("help")) {
                            command = "help";  
                        } else {
                            String typed[] = input.split("\\s+");
                            command = typed[0] + " " + typed[1];
                        }
                       
                        String arguments[] = input.split("\\s+");
                       
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
                                String walletName = arguments[2];
                                String walletPath = arguments[3];
                                createWallet(walletName, walletPath);
                                break;
                            case "open wallet":
                                System.out.println("Opening wallet.");
                                break;
                            case "create account":
                                System.out.println("Create account.");
                               
                                break;
                            case "delete account":
                                System.out.println("Delete account.");
                                //String account = arguments[1];
                                //client.deleteAccount(account);
                                break;
                            case "check balance":
                                System.out.println("Check balance.");
                                break;
                            case "list transactions":
                                System.out.println("List transactions.");
                                break;
                            case "transfer funds":
                                System.out.println("Transfer funds.");
                                break;
                            case "start miner":
                                System.out.println("Command: "+command);
                                String publicKey = arguments[2];
                                String privateKey = arguments[3];
                                int tcpPort = Integer.parseInt(arguments[4]);
                                int httpPort = Integer.parseInt(arguments[5]);
                                if(publicKey == null || privateKey == null || tcpPort <=0 || tcpPort >= 65535 ||
                                   httpPort <= 0 || httpPort >= 65535) {
                                    break;      
                                } else {
                                    startMiner(publicKey,privateKey,tcpPort,httpPort);
                                }
                                break;
                            case "stop miner":
                                System.out.println("Command: "+command);                          
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
   
    private Wallet createWallet(String walletName, String walletPath) {
        System.out.println("Attempting to create wallet at path "+walletPath);
        Wallet wallet = new Wallet();
        if (walletName!=null && walletPath!=null) {
            wallet.setWalletName(walletName);
            wallet.setWalletPath(walletPath);
        }
        return wallet;    
    }
   
    private boolean startMiner(String publicKey, String privateKey, int tcpPort, int httpPort) {
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
        return true;
    }
   
    private boolean stopMiner(String minerAccount, int port) {
        System.out.println("Stoping Criptovaro miner...");
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        int pid = getPidFromPort(port);
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
   
    public Account createAccount() {
        return null;
    }
   
    private void deleteAccount(String accountName) {
        System.out.println("Deleting account "+accountName);
        //wallet.deleteAccount();
    }
   
    private void openWallet(String filePath) {
       
    }
   
    public boolean startHttpd(int port) {
        return false;
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
   
    private static void printHelp() {
        System.out.println("List of commands: ");
        System.out.println("start miner [Public Key] [Private Key] [TCP Port] [HTTP Port]");
        System.out.println("\t Starts a new miner process and an HTTP server in the specified ports.\n");
        System.out.println("stop miner [TCP Port]");
        System.out.println("\t Stops the miner process that is running in the specified port and its associated HTTP server.\n");
    }
   
    private void close() throws IOException {
        System.out.println("Closing Criptovaro...");
    }
   
    private int getPidFromPort(int port) {
        int pid = 0;
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        String command[] = {"lsof","-i:"+port,"-t"};
       
        try {
            p = rt.exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            p.waitFor();
            String s = null;
            while((s=stdInput.readLine())!=null) {
                System.out.println(s);
                pid = Integer.parseInt(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            p.destroy();
        }
 
        return pid;
    }
 
    public static void main(String[] args) throws IOException, InterruptedException {
        final Client client = new Client();
        System.out.println("Criptovaro 0.1");
 
        client.init();
 
       
        client.close();
        System.exit(0);
    }
 
}