package Nodes;

import Messages.*;
import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Server {

    public HashMap<String,Holder> acoesHolders = new HashMap<>(); //mapa com o conjunto de empresas existentes e as acoes disponiveis

    public LinkedList<Ordem> filaOrdens = new LinkedList<>(); //fila com as ordens prontas a executar - FIFO

    public HashMap<String,Ordem> ordensConcluidas = new HashMap<>(); //mapa com as ordens já concluídas, se calhar deviamos por uma resposta também aqui nao?

    public HashMap<String,Ordem> ordensPendentes = new HashMap<>(); //mapa com as ordenas atribuidas ainda nao concluidas

    public HashSet<String> pedidos = new HashSet<>(); //conjunto com os identificadores de pedidos já vistos

    public SpreadConnection connection = new SpreadConnection();
    public SpreadGroup group = new SpreadGroup();
    public String identificador;
    public static String nomeGrupo = "servidores";

    public Serializer s = Protocol.newSerializer();

    public BasicMessageListener bml = new BasicMessageListener() {
        @Override
        public void messageReceived(SpreadMessage spreadMessage) {

            Object o = s.decode(spreadMessage.getData());

            if(o instanceof Ordem){
                System.out.println("Para já tratamos como nas aulas, ou seja diretamente!");

                Ordem ord = (Ordem)o;

                boolean res = false;

                if(o instanceof OrdemCompra){
                    OrdemCompra oc = (OrdemCompra)ord;
                    Holder h = acoesHolders.get(ord.holder);
                    Holder aux = acoesHolders.get(oc.comprador); //comprador das acoes
                    if(h != null && aux != null){
                        res = h.compra(ord.quantidade);
                        if(res) {
                            aux.adiciona(oc.quantidade);
                        }
                    }
                    else{
                        if(h == null) {
                            System.out.println("Holder é null! O que fazer???");
                        }
                        else{
                            System.out.println("Comprador é null! O que fazer???");
                        }
                    }
                }
                else{
                    Holder h = acoesHolders.get(ord.holder);
                    if(h != null){
                        res = h.venda(ord.quantidade);
                    }
                    else{
                        System.out.println("Holder não existe!!! O que fazer???");
//                        Holder ho = new Holder(ord.holder,ord.quantidade);
//                        acoesHolders.put(ord.holder,ho);
                    }
                }

                ordensConcluidas.put(ord.id,ord);

                RespostaOrdem ro = new RespostaOrdem(ord.id,res);
                SpreadMessage sm = new SpreadMessage();
                sm.setData(s.encode(ro));
                sm.addGroup(spreadMessage.getSender());
                sm.setReliable();
                try {
                    connection.multicast(sm);
                } catch (SpreadException e) {
                    e.printStackTrace();
                }
            }

            else if (o instanceof Registo) {

                Registo r = ((Registo) o);

                boolean res = false;

                if (!acoesHolders.containsKey(r.holder)) {

                    Holder n = new Holder(r.holder, r.acoes);

                    acoesHolders.put(r.holder, n);

                    res = true;

                }

                RespostaOrdem ro = new RespostaOrdem(r.id, res);
                SpreadMessage sm = new SpreadMessage();
                sm.setData(s.encode(ro));
                sm.addGroup(spreadMessage.getSender());
                sm.setReliable();
                try {
                    connection.multicast(sm);
                } catch (SpreadException e) {
                    e.printStackTrace();
                }


            }
            else if(o instanceof PedidoHolders){
                String id = ((PedidoHolders)o).id;
                RespostaHolders rh = new RespostaHolders(id,acoesHolders);

                SpreadMessage sm = new SpreadMessage();
                sm.setData(s.encode(rh));
                sm.addGroup(spreadMessage.getSender());
                sm.setReliable();

                try {
                    connection.multicast(sm);
                } catch (SpreadException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("Erro, recebi algo que não é uma ordem!");
            }

        }
    };

    public Server(String identificador) throws UnknownHostException, SpreadException {
        this.identificador = identificador;

        connection.connect(InetAddress.getByName("localhost"), 0, this.identificador, false, false);
        //para já não precisamos de nos preocupar com o group membership visto que vamos usar ativa

        group.join(connection, nomeGrupo);
        connection.add(bml);

        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void introduzOrdem(Ordem o){
        filaOrdens.push(o);
        pedidos.add(o.id);
    }

    public Ordem getOrdem(){
        Ordem o = filaOrdens.poll();
        ordensPendentes.put(o.id,o);

        return o;
    }

    //aqui podiamos passar so o id nao?
    public boolean concluiOrdem(Ordem o){
        Ordem a = ordensPendentes.remove(o.id);

        if(a == null){
            //a ordem n existe ou n esta pendente?
            return false;
        }

        ordensConcluidas.put(o.id,a);

        return true;
    }

    //falta ver como vamos tratar as mensagens e como vamos executar os pedidos
}
