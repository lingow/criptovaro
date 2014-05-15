package criptovaro;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;

public class TransactionHandler implements HttpHandler {
    public TransactionHandler() {
        super();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException 
    {
        ArrayList<String> trans = new ArrayList<String>();
        Gson json = new Gson();
        String response;
        Collection<Transaction> data = Miner.INSTANCE.getPoolTransactions();
        
        if(data == null)
            data = new ArrayList<Transaction>();
        
        for(Transaction t : data)
        {
            trans.add(json.toJson(t));
        }
        
        response = json.toJson(trans);
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
