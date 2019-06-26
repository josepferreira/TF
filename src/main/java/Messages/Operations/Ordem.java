package Messages.Operations;

public class Ordem {
    public String id;
    public long quantidade;
    public String holder;

    public Ordem(String id, long quantidade, String holder) {
        this.id = id;
        this.quantidade = quantidade;
        this.holder = holder;
    }
}
