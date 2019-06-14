package Messages;

public class OrdemCompra extends Ordem {

    public String comprador;

    public OrdemCompra(String id, long quantidade, String holder, String comprador) {
        super(id, quantidade, holder);
        this.comprador = comprador;
    }
}
