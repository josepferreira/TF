package Log;

import Messages.Operations.Operacao;
import Messages.Operations.Ordem;
import Nodes.Holder;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class StateLog {

    public HashMap<String, Holder> acoesHolders; //mapa com o conjunto de empresas existentes e as acoes disponiveis
    public LinkedHashMap<String, Operacao> ordensConcluidas; //mapa com as ordens já concluídas, se calhar deviamos por uma resposta também aqui nao?
    public int id;

    public StateLog(HashMap<String, Holder> acoesHolders, LinkedHashMap<String, Operacao> ordensConcluidas, int id) {
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


