package Log;

import Messages.Operations.Ordem;
import Nodes.Holder;

import java.util.HashMap;

public class StateLog {

    public HashMap<String, Holder> acoesHolders = new HashMap<>(); //mapa com o conjunto de empresas existentes e as acoes disponiveis
    public HashMap<String, Ordem> ordensConcluidas = new HashMap<>(); //mapa com as ordens já concluídas, se calhar deviamos por uma resposta também aqui nao?
    public int id;

    public StateLog(HashMap<String, Holder> acoesHolders, HashMap<String, Ordem> ordensConcluidas, int id) {
        this.acoesHolders = acoesHolders;
        this.ordensConcluidas = ordensConcluidas;
        this.id = id;
    }

    @Override
    public String toString() {
        return "StateLog{" +
                "acoesHolders=" + acoesHolders +
                ", ordensConcluidas=" + ordensConcluidas +
                ", id=" + id +
                '}';
    }
}


