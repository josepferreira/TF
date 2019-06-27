package Messages.Replication;

import Messages.Operations.Operacao;
import Messages.Operations.Ordem;

import java.util.LinkedHashMap;

public class StateReply {
    public LinkedHashMap<String, Operacao> ordensConcluidas; //mapa com as ordens já concluídas, se calhar deviamos por uma resposta também aqui nao?

    public StateReply(LinkedHashMap<String, Operacao> ordensConcluidas) {
        this.ordensConcluidas = ordensConcluidas;
    }
}
