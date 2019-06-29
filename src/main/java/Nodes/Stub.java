package Nodes;

import Configuration.Config;
import Configuration.Protocol;
import Messages.Operations.*;
import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Stub {
    public SpreadConnection connection = new SpreadConnection();

    public ConcurrentHashMap<String, Ordem> ordens = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String,CompletableFuture<Boolean>> cfs = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String,CompletableFuture<HashMap<String,Holder>>> cfHolder = new ConcurrentHashMap<>();

    public Serializer s = Protocol.newSerializer();

    public BasicMessageListener bml = new BasicMessageListener() {
        @Override
        public void messageReceived(SpreadMessage spreadMessage) {

            Object o = s.decode(spreadMessage.getData());

            if(o instanceof RespostaOrdem){
                RespostaOrdem ro = (RespostaOrdem)o;
                CompletableFuture<Boolean> cf = cfs.get(ro.id);

                while(cf == null){
                    System.out.println("Erro, cf é null ou seja o pedido não existe! RespostaOrdem");
                    System.out.println("ID: " + ro.id);
                    System.out.println("Ordem: " + ordens.get(ro.id));
                    System.out.println("CF: " + cfs.get(ro.id));
                    cf = cfs.get(ro.id);
                }
                cf.complete(ro.resultado);
            }

            else if (o instanceof RespostaRegisto) {

                RespostaRegisto ro = (RespostaRegisto) o;
                CompletableFuture<Boolean> cf = cfs.get(ro.id);

                if(cf == null){
                    System.out.println("Erro, cf é null ou seja o pedido não existe! RespostaRegisto");
                }
                else{
                    cf.complete(ro.resultado);
                }

            }
            else if(o instanceof RespostaHolders){
                RespostaHolders rh = (RespostaHolders)o;
                CompletableFuture<HashMap<String,Holder>> cf = cfHolder.get(rh.id);

                if(cf == null){
                    System.out.println("Erro, cf é null ou seja o pedido não existe! Resposta Holders");
                }
                else{
                    cf.complete(rh.holders);
                }
            }
            else{
                System.out.println("Erro, recebi algo que não estava à espera!");
            }

        }
    };

    public Stub() throws UnknownHostException, SpreadException {
        connection.connect(InetAddress.getByName(Config.spreadHost), 0, null, false, false);
        connection.add(bml);
    }

    public CompletableFuture<Boolean> venda(String holder,long quantidade) throws SpreadException {
        CompletableFuture<Boolean> cf = new CompletableFuture<>();

        String id = UUID.randomUUID().toString();
        Ordem o = new Ordem(id,quantidade,holder);

        ordens.put(id,o);
        cfs.put(id,cf);

        SpreadMessage sm = new SpreadMessage();
        sm.setData(s.encode(o));
        sm.addGroup(Config.nomeGrupo);
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

    public CompletableFuture<Boolean> compra(String holder,long quantidade,String comprador) throws SpreadException {
        CompletableFuture<Boolean> cf = new CompletableFuture<>();

        String id = UUID.randomUUID().toString();
        Ordem o = new OrdemCompra(id,quantidade,holder,comprador);

        ordens.put(id,o);
        cfs.put(id,cf);

        SpreadMessage sm = new SpreadMessage();
        sm.setData(s.encode(o));
        sm.addGroup(Config.nomeGrupo);
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

    public CompletableFuture<Boolean> regista(String holder, long acoes) throws SpreadException {
        // Mandar mensagem aos servers para registarem novo holder

        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        String id = UUID.randomUUID().toString();

        cfs.put(id, cf);

        Registo r = new Registo(id, holder, acoes);

        SpreadMessage sm = new SpreadMessage();
        sm.setData(s.encode(r));
        sm.addGroup(Config.nomeGrupo);
        sm.setAgreed(); // ao defiirmos isto estamos a garantir ordem total, pelo q podemos ter varios stubs
        sm.setReliable();
        connection.multicast(sm);

        return cf;



    }

    public CompletableFuture<HashMap<String,Holder>> holders() throws SpreadException {
        CompletableFuture<HashMap<String,Holder>> cf = new CompletableFuture<>();
        String id = UUID.randomUUID().toString();

        cfHolder.put(id, cf);

        PedidoHolders ph = new PedidoHolders(id);
        SpreadMessage sm = new SpreadMessage();
        sm.setData(s.encode(ph));
        sm.addGroup(Config.nomeGrupo);
        sm.setAgreed(); // ao defiirmos isto estamos a garantir ordem total, pelo q podemos ter varios stubs
        sm.setReliable();
        connection.multicast(sm);

        return cf;
    }
}
