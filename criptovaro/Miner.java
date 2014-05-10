package criptovaro;

import java.io.File;

import java.io.IOException;

import java.math.BigDecimal;

import java.net.Inet4Address;
import java.net.InetAddress;

import java.net.NetworkInterface;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.crypto.Data;

public class Miner {
    private boolean initialized;
    private Account currentAccount;
    private BlockChain bchain;
    private TransactionManager tm;
    private PeerManager pm;
    private TransactionPool pool;
    private boolean interruptWork;
    private boolean stopMiner = true; //DEBUG: set to false when ready to test for real. We really need an interface to stop a miner.
    private Peer me;
    private Logger log;
    private FileHandler logFile;
    private TCPListener listener = null;
    private Httpd webServer = null;
    
    /**
     * This attributes makes the miner a singleton
     */
    public static final Miner INSTANCE = new Miner();

    /**
     * If anyone wants to access the miner, they can do so by referring to Miner.INSTANCE. Not by calling the 
     * constructor. This keeps the Miner a singleton class
     */
    private Miner()
    {
        try
        {
            //Initialize the logger properties.
            String curDir = System.getProperty("user.dir");
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(System.currentTimeMillis()));
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1; //Calendar months range 0-11
            int day = c.get(Calendar.DAY_OF_MONTH);
            logFile = new FileHandler(curDir + File.separator + Miner.class.getName() + "_" + year + "-" + 
                                      month + "-" + day +".log");
            log = Logger.getLogger(Miner.class.getName());
            logFile.setFormatter(new SimpleFormatter());
            log.addHandler(logFile);
            log.setLevel(Level.ALL);
        }
        catch(IOException e)
        {
            System.out.println(e.toString());    
        }
    }


    private void startTCPListener(int port, Miner owningMiner, TransactionPool pool) {
    }

    private void Update() {
    }

    public synchronized void toggleWork() {
    }
    
    private Transaction createPrizeTransaction() {
        return null;
    }

    private void Work() {
    }
    
    public void start(Account minerAccount, int tcp_port, int web_port)
    {
        log.log(Level.FINE, "Enter start method.");
        int setupCounter = 0;
        
        //Set the current account
        this.currentAccount = minerAccount;
        
        //Init the miner.    
        do
        {
            Init(tcp_port, web_port);
            setupCounter++;
        }
        while(!this.initialized && setupCounter < 3);
        
        //Update the miner.
        Update();
        
        //Start work main loop.
        while(true)
        {
            //Will loop for new transactions until interruptWork is set.
            Work(); 
            
            //Listener received a stat message with a longer length. Update block chain.
            Update();
            
            if(stopMiner)
            {
                //Print exit message/status
                break;
            }
        }
        
        log.log(Level.FINE, "Exit start method.");
    }

    //TODO: Who can actually call this method? The only other threads on this jvm process are the state-less http daemon and the tcp listener and since it only reacts to network messages,
    //it would be a big security flaw to potentially stop the miner as an action to a nw message.
    public synchronized void stop()
    {
        stopMiner = true;
    }
    
    //TODO: Change all steps of this method to be boolean operations such that if one fais, we can set the flag to false.
    private void Init(int tcp_port, int web_port)
    {
        boolean result = false;
        
        log.log(Level.INFO, "Entering Miner initialization.");
        
        //Get Peer list
        this.pm = PeerManager.INSTANCE;
        pm.LoadPeers();
        
        //Update current block header
        this.bchain = new BlockChain();
     
        //Crate the Transaction Pool
        this.pool = new TransactionPool();
        
        //Get my ip address
        //TODO: Move the getMyIpAddress method to a more suitable class.
        InetAddress myip = getMyIPAddress();
        
        //Add myselt to peer list
        Peer me = new Peer(myip, tcp_port);
        pm.AddPeer(me);
        
        //Spawn tcp listener
        listener = TCPListener.INSTANCE;
        listener.setPort(tcp_port);
        listener.setTransactionPool(pool);
        listener.start();
        
        //Spawn http server
        webServer = new Httpd();
        webServer.start();
        
        //Peer discovery
        result &= PeerDiscovery();
        
        //Set init ready flag
        this.initialized = result;
            
        log.log(Level.INFO, "Exiting miner intialization.");
    }
    
    private InetAddress getMyIPAddress()
    {
        InetAddress res = null;
        try
        {
            String localhost = InetAddress.getLocalHost().getHostAddress();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            
            while(e.hasMoreElements())
            {
                NetworkInterface ni = (NetworkInterface) e.nextElement();
                
                if(ni.isLoopback())
                    continue;
                if(ni.isPointToPoint())
                    continue;
                
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while(addresses.hasMoreElements())
                {
                    InetAddress address = (InetAddress) addresses.nextElement();
                    
                    if(address instanceof Inet4Address)
                    {
                        String ip = address.getHostAddress();
                        if(!ip.equals(localhost))
                            res = address;
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        
        return res;
    }
    
    private boolean PeerDiscovery()
    {
        return true;    
    }

    public static void main(String[] args)
    {
        //Account-transaction test case. Should be moved into a unit test.
        //Make a transaction from account ORIGIN to account DESTINATION of 100 CV, using 2 inputs of 50 and 80 CV respectively.
        //Expected result: Successful transaction with an output of the reminder 30 CV to account 1
        //Test inputs: 2 ficticious transaction from PAST account to ORIGIN account.
        Account acc1 = new Account();
        acc1.setNickname("ORIGIN_ACCOUNT");
        acc1.generateKeys();
        
        Account acc2 = new Account();
        acc2.setNickname("DESTINATION_ACCOUNT");
        acc2.generateKeys();
        
        Account acc3 = new Account();
        acc3.setNickname("PAST_ACCOUNT");
        acc3.generateKeys();
        
        Transaction t = new Transaction(acc1.getPublicKey(), acc2.getPublicKey(), BigDecimal.valueOf(100));
        t.addInput(new Transaction(acc3.getPublicKey(), acc1.getPublicKey(), BigDecimal.valueOf(50)));
        t.addInput(new Transaction(acc3.getPublicKey(), acc1.getPublicKey(), BigDecimal.valueOf(80)));
        if(t.Sign(acc1)) //Change this for an assert on unit test
            System.out.println("Transaction successfully signed!!");
        
        if(t.verify()) 
            System.out.println("Transaction successfully verified!");
        
        //End of test case
        
        Miner theMiner = Miner.INSTANCE;
        String pubkey = null;
        String privkey = null;
        int tcp_port = 0;
        int web_port = 0;
        
        //Get the Miner parameters, otherwise exit
        //TODO: Validate input.
        for(int i=0; i < args.length; i++)
        {
            String s = args[i];
            
            switch(s.toLowerCase())
            {
                case "-public_key": pubkey = args[i+1]; 
                                    break;
                case "-private_key":    privkey = args[i+1];
                                        break;
                case "-tcp_port":   tcp_port = Integer.parseInt(args[i+1]);
                                    break;
                case "web_port":    web_port = Integer.parseInt(args[i+1]);
                                    break;
                default: break;
            };
        }
        
        if(pubkey == null || privkey == null || tcp_port <= 0 || web_port <= 0)
            return;
                                      
        try
        {
            theMiner.start(new Account(pubkey, privkey), tcp_port, web_port);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

