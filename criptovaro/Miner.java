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
    private void Update() {
        while (true){
            Peer bestPeer = PeerManager.INSTANCE.getBestPeer();
            if (bestPeer == null){
                //No peer is best peer. Start working
                break;
            }
            if (bchain.getLenght() < bestPeer.getLenght()){
                // He Wins
                LinkedHashMap<byte[],Integer> peerBranchHash = (new BranchRequest(bchain.getHash(),bchain.getLenght())).request(bestPeer);
                
                //First look for where we are in the incoming branch
                int commonlenght=0;
                for(BlockNode bn : bchain.getBackwardsBlockChain()){
                    Integer i = peerBranchHash.get(bn.hash);
                    if ( i != null){
                        if (bn.lenght == i){
                            commonlenght = i.intValue();
                        }
                        break;
                    }
                }
                //Now form the BlockNode list 
                List<BlockNode> peerBranch = new ArrayList<>();
                for(Map.Entry<byte[],Integer> entry:peerBranchHash.entrySet()){
                    if ( entry.getValue().intValue() > commonlenght){
                        peerBranch.add(new BlockNode(entry.getValue().intValue(),entry.getKey()));
                    }
                }
                //Now start asking for blocks, verify them and add them to a list
                LinkedHashMap<BlockNode,Block> peerBlocks = new LinkedHashMap<BlockNode,Block>(); //Just add them here if we already verified them
                boolean failed=true;
                for( BlockNode bn : peerBranch){
                    Block peerBlock = (new BlockRequest(bn.hash, bn.lenght)).request(bestPeer);
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
                    Block b = prepareBlock(peerBlock.getTransactions());
                    if (b == null){
                        //Block is incorrectly formed
                        failed=true;
                        break;
                    }
                    b.setProof(peerBlock.getProof());
                    if ( !b.verify()){
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
                    bchain.merge(commonlenght,peerBlocks,pool);
                    bchain.commitChain();
                    break;
                } else {
                    //How did we even got here? It must be the peers fault! Burn the Witch!
                    PeerManager.INSTANCE.deletePeer(bestPeer);
                    continue;
                } 
            } else if (bchain.getLenght() > bestPeer.getLenght()){
                // I Win
                new StatMessage(bchain.getHash(),bchain.getLenght()).bcast();
                break;
            } 
        }
    }

    public synchronized void toggleWork() {
        //TODO: implement this method
    }
    
    private Transaction createPrizeTransaction() {
        return null;
    }

    private void Work() 
    {
        try
        {
            Transaction newTran = null;
            Block CurrentBlock = null;
            boolean newTransAdded = false;
            long currentProof = 0;
            Random sr = new Random();
            ByteArrayOutputStream theBytes = new ByteArrayOutputStream();
            ObjectOutputStream oos =  new ObjectOutputStream(theBytes);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] blockHash = null;
            byte[] saltHash = null;
            byte[] proofHash = null;
            
            //Not synchronized access to this variable, however it's fine since at most we'll spend one more cycle working. 
            while(!this.interruptWork)
            {
                //Is this the first round for this block?
                if(CurrentBlock == null)
                {
                    CurrentBlock = new Block();
                    if(bchain.getBlockHeader() != null)
                        CurrentBlock.setPreviousBlock(bchain.getBlockHeader().getHash());
                    else
                    CurrentBlock.setPreviousBlock(null);
                }
                //Check if new transactions in the work pool.
                newTran = null;
                do
                {
                    newTran = this.pool.consumeTransaction(); //This returns null when no new transactions are available.    
                    //If new transaction, validate it, then prepare it, then add it to the block.
                    if(newTran != null)
                    {
                        boolean validTransaction = false;
                        ArrayList<Transaction> funds = null;
                        
                        //Check that the transaction's signature and semantics are correct.
                        if(tm.validateTransaction(newTran))
                        {
                            //First we check Ledger for unspent transactions to solvent this transaction
                            funds = tm.getTransactionFunds(newTran);
                                
                            /*
                             * If the ledger indicates there are not enough funds. Ideally we want to check the current 
                             * block's transactions for funds as well, but the logic for multiple transactions getting 
                             * funded from the current block can get messy. Add it as post-game.
                             * TODO: Add logic such that a transaction can be funded from the current block's transactions.
                             */
                            if(funds != null)
                                validTransaction = true;
                        }
                        
                        //The transaction is golden. Added to the current block.
                        if(validTransaction)
                        {
                            
                            CurrentBlock.addTransaction(newTran);
                            CurrentBlock.addFunds(funds);
                            newTransAdded = true;
                        }
                    }
                }
                while(newTran != null);
                
                //Done with new transactions, now try to calculate the proof for this block.
        
                //If new transaction was added, reset the proof iterator to a new random.
                if(newTransAdded)
                {
                    currentProof = sr.nextLong();
                    newTransAdded = false;
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
                    //TODO: Broadast the result.
                    //Create and broadcast prize transaction
                    //TODO: Create prize transaction and broadcast it. This could have been added as part of the block,
                    //      however, not sure how that would be validated by other miners...
                }
                else
                {
                    //Failed proof :( Incremente the proof and try again.
                    currentProof++;
                }
            }
            
            //We have been signaled to stop working.
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
        LOG.log(Level.INFO, "Entering Miner initialization.");
        Ledger.INSTANCE.init();
        
        //Update current block header
        this.bchain = new BlockChain();
     
        //Crate the Transaction Pool
        this.pool = new TransactionPool();
        
        //Spawn tcp listener
        LOG.log(Level.INFO, "Starting tcp listener in port " + web_port);
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

    private long getProofTest()
    {
        return 0;    
    }

    /**
     * This method is called when any peer is notifying us their stat, probably proposing a new block solution
     * @param peer the peer which this stat is about. It was already added to the peerManager
     */
    public void receivedStat(Peer peer) {
        if (bchain.getLenght() < peer.getLenght()){
            // He Wins
            toggleWork();
        } else if (bchain.getLenght() > peer.getLenght()){
            // I Win
            new StatMessage(bchain.getHash(),bchain.getLenght()).send(peer);
        }
    }

    /**
     * This retrieves from the block that follows from the one with the provided hash and lenght
     * @param hash the hash of the known block
     * @param lenght the lenght of the known block
     * @return the following block from the one with the provided hash and lenght, or null if we dont have that block 
     */
    public Block getNextBlock(byte[] hash, int lenght) {
        //TODO: Implement this Method
        return null;
    }

    /**
     *  This Method creates a new block, of which the proof is not known, based on the transaction collection passed
     *  to it. It adds the transaction to the block, finds the inputs and generates the outputs of every transaction it
     *  will insert into it
     * @param transactions A collection of  regular transactions from which the block should be formed
     * @return The formed block, of which only the proof is missing
     */
    public Block prepareBlock(Collection<Transaction> transactions) {
        //TODO: Implement method
        return null;
    }

    /**
     * Gets a branch from the block chain, beginning with the block with passed hash and lenght, and ending in the 
     * latest block. This will be sent to any requesting peer.
     * @param hash The hash of the block where the chain will begin
     * @param lenght The lenght of the block where the chain will begin
     * @return a LinkedHashMap mapping block hashes to block lenght
     */
    public LinkedHashMap<byte[], Integer> getChainBranch(byte[] hash, int lenght) {
        return bchain.getChainBranch(hash,lenght);
    }
}

