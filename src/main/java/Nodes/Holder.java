package Nodes;

import java.util.Objects;

public class Holder {
    public String nome;
    public long acoes;
    public long acoesVenda;

    public Holder(String nome, long acoes) {
        this.nome = nome;
        this.acoes = acoes;
        this.acoesVenda = 0;
    }

    public boolean compra(long q){
        if(q > acoesVenda){
            return false;
        }

        acoesVenda -= q;
        return true;
    }

    public void adiciona(long q){
        acoes += q;
    }


    public boolean venda(long q){
        if(acoes < q){
            return false;
        }
        acoes -= q;
        acoesVenda += q;
        return true;

    }

    @Override
    public String toString() {
        return "Holder{" +
                "nome='" + nome + '\'' +
                ", acoes=" + acoes +
                ", acoesVenda=" + acoesVenda +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holder holder = (Holder) o;
        return acoes == holder.acoes &&
                acoesVenda == holder.acoesVenda &&
                Objects.equals(nome, holder.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, acoes, acoesVenda);
    }
}
