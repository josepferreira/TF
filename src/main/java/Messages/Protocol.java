package Messages;

import Nodes.Holder;
import io.atomix.utils.serializer.Serializer;

import java.util.HashMap;

public class Protocol {

    public static Serializer newSerializer(){
        return Serializer.builder()
                .withTypes(
                        HashMap.class,
                        Ordem.class,
                        OrdemCompra.class,
                        Registo.class,
                        RespostaOrdem.class,
                        Holder.class,
                        PedidoHolders.class,
                        RespostaHolders.class
                )
                .build();
    }
}
