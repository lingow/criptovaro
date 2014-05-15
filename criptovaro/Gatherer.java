package criptovaro;

import java.util.Collection;
import java.util.Set;

/**
 * This class is used by the Request class to gather objects from every peer
 */
public class Gatherer implements Runnable {
    private Peer p;
    private Request r;
    private Collection c;

    public Gatherer(Request r,Collection c, Peer p) {
        super();
        this.c=c;
        this.r=r;
        this.p = p;
    }
    
    public void run(){
        c.add(r.request(p));
    }
}
