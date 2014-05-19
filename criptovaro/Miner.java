package criptovaro;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;

import java.io.IOException;

import java.math.BigDecimal;

import java.io.InputStreamReader;

import java.io.ObjectOutputStream;

import java.lang.reflect.Array;

import java.net.Inet4Address;
import java.net.InetAddress;

import java.net.MalformedURLException;
import java.net.NetworkInterface;

import java.net.URL;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
            String fileName = curDir + File.separator + Miner.class.getName() + "_" + year + "-" + 
                                      month + "-" + day +".log";
            logFile = new FileHandler(fileName);
            LOG = Logger.getLogger(Miner.class.getName());
            logFile.setFormatter(new SimpleFormatter());
            LOG.addHandler(logFile);
            LOG.setLevel(Level.ALL);
            LOG.log(Level.INFO, "Logging to file: " + fileName);
        }
        catch(IOException e)
        {
            System.out.println(e.toString());    
        }
    }

    /**
     * Update checks if any peer has got a new block. It asks that peer ( or one of those peers ) for their blocks,
     * verifies it's not some malicious peer, and prepares a branch to merge it into the current block chain in order to
     * be in the latest block chain version.
     */
    private void Update() 
    {
        LOG.log(Level.INFO, "Miner entering update");
        
        LOG.log(Level.INFO, "Exiting update process");
        while (true){
            Peer bestPeer = PeerManager.INSTANCE.getBestPeer();
            if (bestPeer == null){
                //No peer is best peer. Start working
                break;
    }
            if (bchain.getLenght() < bestPeer.getLength()){
                // He Wins
                LinkedHashMap<byte[],Integer> peerBranchHash = new LinkedHashMap<byte[],Integer>();
                for (BlockNode bn : (new BranchRequest(bchain.getHash(),bchain.getLenght())).request(bestPeer)){
                    peerBranchHash.put(bn.getHash(), bn.getLength());
                }
                //First look for where we are in the incoming branch
                BlockNode commonNode = null;
                for(BlockNode bn : bchain.getBackwardsBlockChain()){
                    Integer i = peerBranchHash.get(bn.getHash());
                    if ( i != null){
                        BlockNode peerNode = new BlockNode(i,bn.getHash());
                        if (bn.equals(peerNode)){
                            commonNode = bn;
                        }
                        break;
                    }
                }
                if (commonNode == null){
                    //We are in completely different branches
                    PeerManager.INSTANCE.deletePeer(bestPeer);
                    continue;
                }
                //Now form the BlockNode list 
                List<BlockNode> peerBranch = new ArrayList<>();
                for(Map.Entry<byte[],Integer> entry:peerBranchHash.entrySet()){
                    if ( entry.getValue().intValue() > commonNode.getLength()){
                        peerBranch.add(new BlockNode(entry.getValue().intValue(),entry.getKey()));
                    }
                }
                //Now start asking for blocks, verify them and add them to a list
                LinkedHashMap<BlockNode,Block> peerBlocks = new LinkedHashMap<BlockNode,Block>(); //Just add them here if we already verified them
                HashMap<byte[], ArrayList<Transaction>> unspentCache = new HashMap<byte[], ArrayList<Transaction>>();
                boolean failed=true;
                for( BlockNode bn : peerBranch){
                    Block peerBlock = (new BlockRequest(bn.getHash(), bn.getLength())).request(bestPeer);
                    if (peerBlock == null){
                        //It's a Trap!
                        failed=true;
                        break;
                    }
                    if ( !peerBlock.verify()){
                        //Bulshit
                        failed=true;
                        break;
                    }

                    Block b = new Block();
                    b.addTransactions(peerBlock.getRegularTrans(), this.tm, unspentCache);
                    b.setProof(peerBlock.getProof());
                    b.setBlockChainPosition(peerBlock.getBlockChainPosition());
                    b.setPreviousBlock(peerBlock.getPreviousBlockHash());
                    b.setPrizeTransaction(createPrizeTransaction(peerBlock.getSolverPublicKey()));
                    if ( !b.verify())
                    {
                        //The proof is wrong
                        failed=true;
                        break;
                    }
                    if (b.compare(peerBlock)){
                        peerBlocks.put(bn, peerBlock);
                    } else {
                        //The blocks don't match
                        failed=true;
                        break;
                    }
                    failed = false;
                }
                if (failed){
                    PeerManager.INSTANCE.deletePeer(bestPeer);
                    continue;
                }
                if (!peerBlocks.isEmpty()){
                    bchain.merge(commonNode,peerBlocks,pool);
                    break;
                } else {
                    //How did we even got here? It must be the peers fault! Burn the Witch!
                    PeerManager.INSTANCE.deletePeer(bestPeer);
                    continue;
                } 
            } else if (bchain.getLenght() > bestPeer.getLength()){
                // I Win
                new StatMessage(bchain.getHash(),bchain.getLenght()).bcast();
                break;
            } 
        }
    }
    
    public synchronized void toggleWork() 
    {
        if(this.interruptWork)
            this.interruptWork = false;
        else
            this.interruptWork = true;
    }

    private Transaction createPrizeTransaction(byte[] winner) {
        return new Transaction(winner, winner ,BigDecimal.valueOf(100));
    }

    private void Work() 
    {
        try
        {
            LOG.log(Level.INFO, "Entering work loop.");
            ArrayList<Transaction> incomingTransactions = null;
            Block CurrentBlock = null;
            long currentProof = 0;
            Random sr = new Random();
            ByteArrayOutputStream theBytes = new ByteArrayOutputStream();
            ObjectOutputStream oos =  new ObjectOutputStream(theBytes);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] blockHash = null;
            byte[] saltHash = null;
            byte[] proofHash = null;
            HashMap<byte[], ArrayList<Transaction>> unspentTransCache = null;
            
            //Not synchronized access to this variable, however it's fine since at most we'll spend one more cycle working. 
            while(!this.interruptWork)
            {
                //Is this the first round for this block?
                if(CurrentBlock == null)
                {
                    CurrentBlock = new Block();
                    unspentTransCache = new HashMap<byte[], ArrayList<Transaction>>();
                    Transaction prize = createPrizeTransaction(this.currentAccount.getPublicKey());
                    CurrentBlock.setPrizeTransaction(prize);
                    if(bchain.getHash() != null)
                    {
                        CurrentBlock.setPreviousBlock(bchain.getHash());
                        CurrentBlock.setBlockChainPosition(bchain.getLenght() + 1);
                    }
                    else
                    CurrentBlock.setPreviousBlock(null);
                }
                
                //Check if new transactions in the work pool.
                incomingTransactions = this.pool.getAllTransactions();
                        
                if(incomingTransactions != null)
                        {
                    CurrentBlock.addTransactions(incomingTransactions, this.tm, unspentTransCache);
                                
                    //If new transaction was added, reset the proof iterator to a new random.
                    currentProof = sr.nextLong();
                        }
                        
                //Done with new transactions, now try to calculate the proof for this block.
                if(CurrentBlock.getTransactions().size() < 1 )
                {
                    //No work to be done. Sleep for some time and continue to the next iteration
                    Thread.sleep(100);
                    continue;
                }
                
                //Create SHA256 hash of proof and get bytes.
                oos.writeLong(currentProof);
                oos.flush();
                md.update(theBytes.toByteArray());
                saltHash = md.digest();
                
                //Get block bytes hash
                CurrentBlock.setProof(currentProof);
                blockHash = CurrentBlock.getHash();
  
                
                //Create SHA256 hash of the whole data set
                oos.write(blockHash);
                oos.write(saltHash);
                oos.flush();
                md.update(theBytes.toByteArray());
                proofHash = md.digest();
                
                ByteArrayInputStream bais = new ByteArrayInputStream(proofHash);
                DataInputStream dis = new DataInputStream(bais);
                
                if(dis.readLong() < getProofTest())
                {
                    //We have a winner!
                    //Commit Block
                    bchain.appendBlock(CurrentBlock);
                    
                    //Now remove them from the transaction pool
                    this.pool.removeIfExist(CurrentBlock.getRegularTrans());
                    
                    //Broadcast solution
                    (new StatMessage(CurrentBlock.getHash(), CurrentBlock.getBlockChainPosition())).bcast();
                    
                    CurrentBlock = null;
                    incomingTransactions = null;
                }
                else
                {
                    //Failed proof :( Increment the proof and try again.
                    currentProof++;
                }
            }
            
            //We have been signaled to stop working.
            LOG.log(Level.INFO, "Exiting work loop.");
        }
        catch (IOException e) 
        {
            LOG.log(Level.INFO, e.toString());
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) 
        {
            LOG.log(Level.INFO, e.toString());
            e.printStackTrace(); 
        }
        catch (InterruptedException e) 
        {
            LOG.log(Level.INFO, e.toString());
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
        boolean debugCon = true;
        while(debugCon)
        {
            debugCon = false;
            //Will loop for new transactions until interruptWork is set.
            //Work(); 
            
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
        LOG.log(Level.INFO, "Entering Miner initialization.");
        Ledger.INSTANCE.init();
        
        //Update current block header
        this.bchain = new BlockChain();
     
        //Crate the Transaction Pool
        this.pool = new TransactionPool();
        
        //Spawn tcp listener
        LOG.log(Level.INFO, "Starting tcp listener in port " + tcp_port);
        listener = new TCPListener(tcp_port);
        listener.start();
        
        //Spawn http server
        LOG.log(Level.INFO, "Starting dashboard server in port " + web_port);
        webServer = new Httpd(web_port);
        webServer.start();
        
        //Initialize PeerManager
        PeerManager.INSTANCE.init();
        
        //Set init ready flag
        this.initialized = true;
            
        LOG.log(Level.INFO, "Exiting miner intialization.");
    }

public static void main(String[] args)
    {
        //Account-transaction test case. Should be moved into a unit test.
        //Make a transaction from account ORIGIN to account DESTINATION of 100 CV, using 2 inputs of 50 and 80 CV respectively.
        //Expected result: Successful transaction with an output of the reminder 30 CV to account 1
        //Test inputs: 2 ficticious transaction from PAST account to ORIGIN account.
        Account acc1 = new Account();
        acc1.setAlias("ORIGIN_ACCOUNT");
        acc1.generateKeys();
        
        Account acc2 = new Account();
        acc2.setAlias("DESTINATION_ACCOUNT");
        acc2.generateKeys();
        
        Account acc3 = new Account();
        acc3.setAlias("PAST_ACCOUNT");
        acc3.generateKeys();
        
        Transaction t1 = new Transaction(acc1.getPublicKey(), acc2.getPublicKey(), BigDecimal.valueOf(100));
        Transaction t2 = new Transaction(acc3.getPublicKey(), acc1.getPublicKey(), BigDecimal.valueOf(50));
        Transaction t3 = new Transaction(acc3.getPublicKey(), acc1.getPublicKey(), BigDecimal.valueOf(80));
        
        //t1.setOriginTransaction(new byte[]{0,1}); //BUG:validate
        //t1.setSpentBy(new byte[]{0,1}); //BUG: validate
        if(t1.Sign(acc1)) //Change this for an assert on unit test
            System.out.println("Transaction successfully signed!!");
        if(t1.verify()) 
            System.out.println("Transaction successfully verified!");
        
        //t2.setOriginTransaction(new byte[]{0,1}); //BUG:validate
        //t2.setSpentBy(new byte[]{0,1}); //BUG: validate
        if(t2.Sign(acc3)) //Change this for an assert on unit test
            System.out.println("Transaction successfully signed!!");
        if(t2.verify()) 
            System.out.println("Transaction successfully verified!");
        
        //t3.setOriginTransaction(new byte[]{0,1}); //BUG:validate
        //t3.setSpentBy(new byte[]{0,1}); //BUG: validate
        if(t3.Sign(acc3)) //Change this for an assert on unit test
            System.out.println("Transaction successfully signed!!");
        if(t3.verify()) 
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
            
            //BlockManaer test case
//            BlockManager bm = new BlockManager();
//            Block b = new Block();
//            if(!b.addTransaction(t1))
//                LOG.log(Level.INFO, "Failed to add transaction!");
//            if(!b.addTransaction(t2))
//                LOG.log(Level.INFO, "Failed to add transaction!");
//            if(!b.addTransaction(t3))
//                LOG.log(Level.INFO, "Failed to add transaction!");
//            bm.insertBlock(b);
            //End BlockManager test case
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private long getProofTest()
    {
        return Long.MAX_VALUE -  (bchain.getLenght() * 100);    
    }

    /**
     * This method is called when any peer is notifying us their stat, probably proposing a new block solution
     * @param peer the peer which this stat is about. It was already added to the peerManager
     */
    public void receivedStat(Peer peer) 
    {
        if (bchain.getLenght() < peer.getLength())
        {
            // He Wins
            toggleWork();
}
        else if (bchain.getLenght() > peer.getLength())
        {
            // I Win
            new StatMessage(bchain.getHash(),bchain.getLenght()).send(peer);
        }
    }

    /**
     * This retrieves from the block that follows from the one with the provided hash and lenght
     * @param hash the hash of the known block
     * @param length the lenght of the known block
     * @return the following block from the one with the provided hash and lenght, or null if we dont have that block 
     */
    public Block getNextBlock(byte[] hash, int length) 
    {
        return bchain.getNextBlock(hash, length);
    }


    /**
     * Gets a branch from the block chain, beginning with the block with passed hash and lenght, and ending in the 
     * latest block. This will be sent to any requesting peer.
     * @param hash The hash of the block where the chain will begin
     * @param lenght The lenght of the block where the chain will begin
     * @return a LinkedHashMap mapping block hashes to block lenght
     */
    public LinkedList<BlockNode> getChainBranch(byte[] hash, int lenght) 
    {
        return bchain.getChainBranch(hash,lenght);
    }

    public void incomingTransaction(Transaction t) 
    {
        pool.addTransaction(t);
}
    
    public synchronized Collection<Transaction> getPoolTransactions()
    {
        return pool.getAllTransactions();
}
}

