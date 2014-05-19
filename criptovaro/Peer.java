package criptovaro;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Peer {
    private InetAddress iPAddress;
    private int port;
    private byte[] hash;
    private long length;

    public void setPort(int port) {
        this.port=port;
    }

    public int getPort() {
        return port;
    }

    public void setIPAddress(String address) throws UnknownHostException {
        this.iPAddress=InetAddress.getByName(address);
    }

    public String getIPAddressString() {
        return iPAddress.getHostAddress();
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
        this.port=p.getPort();
        this.iPAddress=p.getIPAddress();
        this.length=p.getLength();
        this.hash=p.getHash();
    }

    public boolean equals(Peer p){
        return this.getKey().equals(p.getKey());
    }

    public long getLength() {
       return this.length;
    }

    void setHash(byte[] hash) {
       this.hash=hash;
    }
    
    void setLength(long l) {
        this.length=l;
    }

    private InetAddress getIPAddress() {
        return this.iPAddress;
    }

    private byte[] getHash() {
        return this.hash;
    }
}
