package criptovaro;

public class Miner {
    private boolean initialized;
    private Account currentAccount;
    private BlockChain bchain;
    private TransactionManager tm;
    private PeerManager pm;
    private TransactionPool pool;
    private boolean interruptWork;
    private Peer me;

    /**
     * This attributes makes the miner a singleton
     */
    public static final Miner INSTANCE = new Miner();

    /**
     * If anyone wants to access the miner, they can do so by referring to Miner.INSTANCE. Not by calling the 
     * constructor. This keeps the Miner a singleton class
     */
    private Miner() {
        
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

    private static void main(String[] argv) {
    }

    public void start() {
    }

    public void stop() {
    }
}
