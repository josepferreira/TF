package Messages.Replication;

public class StateTransfer {

    public String id;
    public byte[] msg;
    public int numero;
    public boolean last;

    public StateTransfer(String id, byte[] msg, int numero, boolean last) {
        this.id = id;
        this.msg = msg;
        this.numero = numero;
        this.last = last;
    }
}
