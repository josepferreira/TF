package Messages;

public class Ordem {
    public String id;
    public long quantidade;
    public String empresa;
    public boolean compra;

    public Ordem(String id, long quantidade, String empresa, boolean compra) {
        this.id = id;
        this.quantidade = quantidade;
        this.empresa = empresa;
        this.compra = compra;
    }
}
