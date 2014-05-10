package criptovaro;

import java.io.BufferedReader;
import java.io.File;

import java.io.IOException;

import java.io.InputStreamReader;

import java.net.Inet4Address;
import java.net.InetAddress;

import java.net.MalformedURLException;
import java.net.NetworkInterface;

import java.net.URL;

import java.util.ArrayList;
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
    static protected Logger LOG;
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
            LOG = Logger.getLogger(Miner.class.getName());
            logFile.setFormatter(new SimpleFormatter());
            LOG.addHandler(logFile);
            LOG.setLevel(Level.ALL);
        }
        catch(IOException e)
        {
            System.out.println(e.toString());    
        }
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

    public static void main(String[] args)
    {
        Miner theMiner = Miner.INSTANCE;
        byte[] pubkey = null;
        byte[] privkey = null;
        int tcp_port = 0;
        int web_port = 0;
        
        //Get the Miner parameters, otherwise exit
        //TODO: Validate input.
        for(int i=0; i < args.length; i++)
        {
            String s = args[i];
            
            switch(s.toLowerCase())
            {
                case "-public_key": pubkey = args[i+1].getBytes(); 
                                    break;
                case "-private_key":    privkey = args[i+1].getBytes();
                                        break;
                case "-tcp_port":   tcp_port = Integer.parseInt(args[i+1]);
                                    break;
                case "-web_port":    web_port = Integer.parseInt(args[i+1]);
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
    
    public void start(Account minerAccount, int tcp_port, int web_port)
    {
        LOG.log(Level.FINE, "Enter start method.");
        int setupCounter = 0;
        
        //Set the current account
        this.currentAccount = minerAccount;
        
        //Init the miner.    
        do
        {
            init(tcp_port, web_port);
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
        
        LOG.log(Level.FINE, "Exit start method.");
    }

    //TODO: Who can actually call this method? The only other threads on this jvm process are the state-less http daemon and the tcp listener and since it only reacts to network messages,
    //it would be a big security flaw to potentially stop the miner as an action to a nw message.
    public synchronized void stop()
    {
        stopMiner = true;
    }
    
    //TODO: Change all steps of this method to be boolean operations such that if one fais, we can set the flag to false.
    private void init(int tcp_port, int web_port)
    {
        boolean result = false;
        
        LOG.log(Level.INFO, "Entering Miner initialization.");
        Ledger.INSTANCE.init();
        
        //Update current block header
        this.bchain = new BlockChain();
     
        //Crate the Transaction Pool
        this.pool = new TransactionPool();
          
        //Spawn tcp listener
        listener = new TCPListener(tcp_port);
        listener.start();
        
        //Spawn http server
        webServer = new Httpd(web_port);
        webServer.start();
        
        //Initialize PeerManager
        PeerManager.INSTANCE.init();
        
        //Set init ready flag
        this.initialized = result;
            
        LOG.log(Level.INFO, "Exiting miner intialization.");
    }
}

