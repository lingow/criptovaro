package criptovaro;
import java.math.BigDecimal;
import java.util.Date;

public class Transaction {
    private Transaction[] inputs;
    private Transaction[] outputs;
    private byte[] source;
    private byte[] destination;
    private BigDecimal amount;
    private Date timestamp;
    private byte[] digitalSignature;

    public void addInput(Transaction input) {
    }

    public void addOutput(Transaction output) {
    }

    public int verify() {
        return 0;
    }

    public byte[] getDigitalSignature() {
        return null;
    }
}
