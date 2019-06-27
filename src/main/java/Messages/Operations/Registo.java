package Messages.Operations;

public class Registo extends Operacao{
    public String holder;
    public long acoes;

    public Registo(String id, String holder, long acoes) {
        super(id);
        this.holder = holder;
        this.acoes = acoes;
    }
}
