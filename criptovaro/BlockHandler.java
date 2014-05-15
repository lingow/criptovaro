package criptovaro;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlockHandler implements HttpHandler {
    public BlockHandler() {
        super();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException 
    {
        ArrayList<String> blocks = new ArrayList<String>();
        Gson json = new Gson();
        String response;
        
        LinkedHashMap<byte[], Integer> headers = Miner.INSTANCE.getChainBranch(null, 0);
        for(Map.Entry<byte[], Integer> b : headers.entrySet())
        {
            blocks.add(json.toJson(b));
        }
        
        response = json.toJson(blocks);
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
