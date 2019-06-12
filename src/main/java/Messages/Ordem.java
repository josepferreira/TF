package Messages;

public class Ordem {
    public String id;
    public long quantidade;
    public String holder;
    public boolean compra;

    public Ordem(String id, long quantidade, String holder, boolean compra) {
        this.id = id;
        this.quantidade = quantidade;
        this.holder = holder;
        this.compra = compra;
    }
}
