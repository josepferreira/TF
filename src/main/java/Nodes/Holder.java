package Nodes;

public class Holder {
    public String nome;
    public long acoes;

    public Holder(String nome, long acoes) {
        this.nome = nome;
        this.acoes = acoes;
    }

    public boolean compra(long q){
        if(q > acoes){
            return false;
        }

        acoes -= q;
        return true;
    }

    public boolean venda(long q){
        acoes += q;
        return true;

    }
}
