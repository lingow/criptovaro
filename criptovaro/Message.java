package criptovaro;

import java.io.Serializable;

public class Message implements Serializable{
    private MessageType type;
    private Serializable data;

    public void Message(byte[] data) {
    }
}
