package criptovaro;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class Peer {
    private InetAddress iPAddress;
    private int port;
    private boolean reliable;
    private int blockChainLength;

    public void setPort(int port) {
    }

    public int getPort() {
        return 0;
    }

    public void setIPAddress(String address) {
    }

    public String getIPAddress() {
        return "";
    }

    public void setReliable(boolean reliable) {
    }

    public void setBlockChainLength(int length) {
    }

    public int getBlockChainLength() {
        return 0;
    }

    public boolean getReliable() {
        return false;
    }

    Socket createSocket() throws IOException {
        return new Socket(iPAddress,port);
    }
    
    public Peer()
    {}
    
    public Peer(InetAddress ip, int port)
    {
        this.iPAddress = ip;
        this.port = port;
    }
}
