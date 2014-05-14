package criptovaro;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

public class Peer {
    private InetAddress iPAddress;
    private int port;
    private byte[] hash;
    private int lenght;

    public void setPort(int port) {
        this.port=port;
    }

    public int getPort() {
        return port;
    }

    public void setIPAddress(String address) throws UnknownHostException {
        this.iPAddress=InetAddress.getByName(address);
    }

    public String getIPAddress() {
        return iPAddress.toString();
    }

    Socket createSocket() throws IOException {
        return new Socket(iPAddress,port);
    }
    
    public Peer(InetAddress ip, int port)
    {
        this.iPAddress = ip;
        this.port = port;
    }

    /**
     * We need a method to uniquely identify a Peer. This is it. So far, it just Creates an InetSocketAddress object
     * using this peer's iPAddress and Port.
     * @return An object that uniquely identifies this Peer
     */
    public InetSocketAddress getKey() {
        return new InetSocketAddress(iPAddress,port);
    }

    /**
     * Updates this Peer to match the passed Peer
     * @param p the peer to which this peer will update
     */
    public void updateTo(Peer p) {
        
    }

    public boolean equals(Peer p){
        return this.getKey().equals(p.getKey());
    }

    public int getLenght() {
        //TODO: Implement this method
        return 0;
    }

    void setHash(byte[] hash) {
        //TODO: Implement this method
    }

    void setlenght(int lenght) {
        //TODO: Implement this method
    }
    
    void setLenght(int lenght) {
        //TODO: Implement this method
    }
}
