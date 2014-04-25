package criptovaro;

import java.io.Serializable;

public class Message implements Serializable{
    @SuppressWarnings("compatibility:-5917982315562627774")
    private static final long serialVersionUID = 1L;
    private MessageType type;
    private Serializable data;

    public void Message(byte[] data) {
    }
}
