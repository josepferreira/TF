package Messages;

import io.atomix.utils.serializer.Serializer;

import java.util.HashMap;

public class Protocol {

    public static Serializer newSerializer(){
        return Serializer.builder()
                .withTypes(
                        HashMap.class,
                        Ordem.class,
                        RespostaOrdem.class
                )
                .build();
    }
}
