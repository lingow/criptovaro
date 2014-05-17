package criptovaro;

import java.io.Serializable;

public class JoinMessage extends Message {
    @SuppressWarnings("compatibility:-5302453286374064530")
    private static final long serialVersionUID = -5515922944130505016L;


    @Override
    protected boolean deliver(Peer peer) 
    {
        // TODO:Not sure yet if we need to do something here...
        return true;
    }
}
