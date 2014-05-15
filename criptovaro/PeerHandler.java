package criptovaro;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;

public class PeerHandler implements HttpHandler {
    public PeerHandler() {
        super();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException 
    {
        ArrayList<String> peers = new ArrayList<String>();
        Gson json = new Gson();
        String response;
        
        for(Peer p : Ledger.INSTANCE.q_PeerList())
        {
            peers.add(json.toJson(p));
        }
        
        response = json.toJson(peers);
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
