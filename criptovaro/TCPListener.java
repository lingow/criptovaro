package criptovaro;

import java.net.ServerSocket;

public class TCPListener {
    private Miner owningMiner;
    private ServerSocket ss;
    private int port;
    private TransactionPool pool;

    private void ListenForWork() {
    }

    private void receivedTransaction(byte[] messageBody) {
    }

    private Message buildMessage(byte[] nwData) {
        return null;
    }

    private void receivedSolution(byte[] messageBody) {
    }

    private void receivedPeerListRequest(byte[] messageBody) {
    }

    private void receivedJoin(byte[] messageBody) {
    }

    private void receivedBlockRequest(byte[] messageBody) {
    }

    private void postMiner() {
    }

    public void TCPListener(int port, Miner owningMiner, TransactionPool pool) {
        
    }
}
