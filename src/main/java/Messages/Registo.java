package Messages;

public class Registo {
    public String id;
    public String holder;
    public long acoes;

    public Registo(String id, String holder, long acoes) {
        this.id = id;
        this.holder = holder;
        this.acoes = acoes;
    }
}
