package criptovaro;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.OutputStream;

import java.net.URI;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sun.misc.BASE64Encoder;

/*
 * This class is used to handle request to the main dashboard.html page
 */
public class DashboardHandler implements HttpHandler 
{
    private String root;
    
    public DashboardHandler(String root) 
    {
        super();
        this.root = root;
    }

    @Override
    public void handle(HttpExchange t) throws IOException 
    {
        URI uri = t.getRequestURI();
        File file = new File(root + uri.getPath()).getCanonicalFile();
        if (!file.getPath().startsWith(root)) {
            // Suspected path traversal attack: reject with 403 error.
            String response = "403 (Forbidden)\n";
            t.sendResponseHeaders(403, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else if (file.isFile()) {
            // Object exists and is a file: accept with response code 200.
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.write((new String(Files.readAllBytes(file.toPath()))).getBytes());
            os.close();
        } else if (file.isDirectory()){
            //Send the index
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.write((new String(Files.readAllBytes(new File(file.toString()+"/index.html").toPath())).getBytes()));
            os.close();
        }else{
            if (uri.getPath().matches("/[^/ ]*.json")) {
                Gson json = new Gson();
                String response = "";
                if (uri.getPath().equals("/peers.json")) {
                    response = json.toJson(Ledger.INSTANCE.q_PeerList());
                } else if (uri.getPath().equals("/blocks.json")) {
                    response=json.toJson(Miner.INSTANCE.getChainBranch(null, 0));                
                } else if (uri.getPath().equals("/transactions.json")) {
                    response = json.toJson(Miner.INSTANCE.getPoolTransactions());
                }
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Object does not exist or is not a file: reject with 404 error.
                String response = "404 (Not Found)\n";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    
    public Map<String, String> parseGet(HttpExchange he){
        Map<String, String> result = new HashMap<String, String>();
        for (String param : he.getRequestURI().getQuery().split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }
}
