package Nodes;

import Messages.Ordem;
import Messages.Protocol;
import Messages.RespostaOrdem;
import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Stub {
    public SpreadConnection connection = new SpreadConnection();

    public HashMap<String,Ordem> ordens = new HashMap<>();
    public HashMap<String,CompletableFuture<Boolean>> cfs = new HashMap<>();

    public Serializer s = Protocol.newSerializer();

    public BasicMessageListener bml = new BasicMessageListener() {
        @Override
        public void messageReceived(SpreadMessage spreadMessage) {

            Object o = s.decode(spreadMessage.getData());

            if(o instanceof RespostaOrdem){
                RespostaOrdem ro = (RespostaOrdem)o;
                CompletableFuture<Boolean> cf = cfs.get(ro.id);

                if(cf == null){
                    System.out.println("Erro, cf é null ou seja o pedido não existe!");
                }
                else{
                    cf.complete(ro.resultado);
                }
            }
            else{
                System.out.println("Erro, recebi algo que não estava à espera!");
            }

        }
    };

    public Stub() throws UnknownHostException, SpreadException {
        connection.connect(InetAddress.getByName("localhost"), 0, null, false, false);
        connection.add(bml);
    }

    public CompletableFuture<Boolean> venda(String holder,long quantidade) throws SpreadException {
        CompletableFuture<Boolean> cf = new CompletableFuture<>();

        String id = UUID.randomUUID().toString();
        Ordem o = new Ordem(id,quantidade,holder,false);

        ordens.put(id,o);
        cfs.put(id,cf);

        SpreadMessage sm = new SpreadMessage();
        sm.setData(s.encode(o));
        sm.addGroup(Server.nomeGrupo);
        sm.setAgreed(); // ao defiirmos isto estamos a garantir ordem total, pelo q podemos ter varios stubs
        sm.setReliable();
        connection.multicast(sm);
        /* Depois temos de ver se temos de definir timeouts!
        es.schedule(() -> {
            terminaPedido(id, true);
        }, 30, TimeUnit.SECONDS);
        */

        return cf;
    }

    public CompletableFuture<Boolean> compra(String holder,long quantidade) throws SpreadException {
        CompletableFuture<Boolean> cf = new CompletableFuture<>();

        String id = UUID.randomUUID().toString();
        Ordem o = new Ordem(id,quantidade,holder,true);

        ordens.put(id,o);
        cfs.put(id,cf);

        SpreadMessage sm = new SpreadMessage();
        sm.setData(s.encode(o));
        sm.addGroup(Server.nomeGrupo);
        sm.setAgreed(); // ao defiirmos isto estamos a garantir ordem total, pelo q podemos ter varios stubs
        sm.setReliable();
        connection.multicast(sm);
        /* Depois temos de ver se temos de definir timeouts!
        es.schedule(() -> {
            terminaPedido(id, true);
        }, 30, TimeUnit.SECONDS);
        */

        return cf;
    }

    public void close() throws SpreadException {
        connection.disconnect();
    }
}
