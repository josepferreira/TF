package Configuration;

import Log.LogEntry;
import Log.StateLog;
import Messages.Operations.*;
import Messages.Replication.StateReply;
import Messages.Replication.StateRequest;
import Messages.Replication.StateTransfer;
import Nodes.Holder;
import io.atomix.utils.serializer.Serializer;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Protocol {

    public static Serializer newSerializer(){
        return Serializer.builder()
                .withTypes(
                        HashMap.class,
                        LinkedHashMap.class,
                        Operacao.class,
                        Ordem.class,
                        OrdemCompra.class,
                        Registo.class,
                        RespostaOrdem.class,
                        Holder.class,
                        PedidoHolders.class,
                        RespostaHolders.class,
                        StateReply.class,
                        StateRequest.class,
                        StateTransfer.class,
                        StateLog.class,
                        LogEntry.class
                )
                .build();
    }
}
