package Messages.Operations;

public class Ordem extends Operacao{
    public long quantidade;
    public String holder;

    public Ordem(String id, long quantidade, String holder) {
        super(id);
        this.quantidade = quantidade;
        this.holder = holder;
    }
}
