package criptovaro;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Paths;

/*
 * This class is used to handle request to the main dashboard.html page
 */
public class DashboardHandler implements HttpHandler 
{
    private String dashboardPath;
    
    public DashboardHandler(String DashboardPath) 
    {
        super();
        this.dashboardPath = DashboardPath;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException 
    {
        String response;
        response = new String(Files.readAllBytes(Paths.get(this.dashboardPath)));
        
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
