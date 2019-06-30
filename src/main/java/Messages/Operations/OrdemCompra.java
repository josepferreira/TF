package Messages.Operations;

public class OrdemCompra extends Ordem {

    public String comprador;

    public OrdemCompra(String id, long quantidade, String holder, String comprador) {
        super(id, quantidade, holder);
        this.comprador = comprador;
    }

    @Override
    public String toString() {
        return "OrdemCompra{" +
                "comprador='" + comprador + '\'' +
                ", quantidade=" + quantidade +
                ", holder='" + holder + '\'' +
                ", id='" + id + '\'' +
                ", resposta=" + resposta +
                '}';
    }
}
