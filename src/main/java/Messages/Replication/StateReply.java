package Messages.Replication;

import Messages.Operations.Ordem;
import Nodes.Holder;

import java.util.HashMap;

public class StateReply {

    public HashMap<String, Holder> acoesHolders = new HashMap<>(); //mapa com o conjunto de empresas existentes e as acoes disponiveis

    public HashMap<String, Ordem> ordensConcluidas = new HashMap<>(); //mapa com as ordens já concluídas, se calhar deviamos por uma resposta também aqui nao?

    public StateReply(HashMap<String, Holder> acoesHolders, HashMap<String, Ordem> ordensConcluidas) {
        this.acoesHolders = acoesHolders;
        this.ordensConcluidas = ordensConcluidas;
    }
}
