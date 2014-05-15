package criptovaro;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;

import java.net.InetSocketAddress;

import java.util.logging.Level;

public class Httpd extends Thread
{
    private int port;
    private HttpServer server = null;
    private String dashboardPath = System.getProperty("user.dir") +  File.separator + "criptovaro" + File.separator + 
                                   "public_html" + File.separator +"dashboard.html"; //TODO: Change this path to something less fragile
    private int connectionBacklog = 0; //System default. Adjust as necessary.
    
    public Httpd(int web_port)
    {
        this.port=web_port;
    }

    public void run()
    {
        try
        {
            server = HttpServer.create(new InetSocketAddress(port), connectionBacklog);
            server.createContext("/Dashboard", new DashboardHandler(dashboardPath));
            server.createContext("/PeerData", new PeerHandler());
            server.createContext("/BlockData", new BlockHandler());
            server.createContext("/TransactionData", new TransactionHandler());
            server.setExecutor(null);
            server.start();
            
            System.out.println("Server started");
            Miner.LOG.log(Level.INFO, "Serving the page in " + dashboardPath);
            System.in.read();
            System.out.println("Server stopped");
        } 
        catch (IOException e) 
        {
            Miner.LOG.log(Level.INFO, e.toString());
            e.printStackTrace();
        }

    }
}
