package Avaliacao;

import Nodes.Holder;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class ServidorAvaliacao {

    public HashMap<String,Holder> acoesHolders = new HashMap<>(); //mapa com o conjunto de empresas existentes e as acoes disponiveis

    public boolean venda(String holder, long quantidade){
        Holder h = acoesHolders.get(holder);
        boolean res = false;
        if(h != null){
            h.venda(quantidade);
            res = true;
        }

        return res;
    }

    public boolean compra(String holder,long quantidade,String comprador){
        Holder h = acoesHolders.get(holder);
        Holder aux = acoesHolders.get(comprador); //comprador das acoes
        boolean res = false;
        if(h != null && aux != null){
            h.compra(quantidade);
            aux.adiciona(quantidade);
            res = true;
        }
        return res;
    }

    public boolean regista(String holder, long acoes) {
        boolean res = false;

        if (!acoesHolders.containsKey(holder)) {

            Holder n = new Holder(holder, acoes);

            acoesHolders.put(holder, n);

            res = true;

        }

        return res;
    }

    public HashMap<String, Holder> holders(){

        return acoesHolders;
    }
}
