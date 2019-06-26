package Nodes;

import Configuration.Config;
import Configuration.Protocol;
import Log.LogInterface;
import Log.StateLog;
import Messages.Operations.*;
import Messages.Replication.StateReply;
import Messages.Replication.StateRequest;
import Messages.Replication.StateTransfer;
import UI.Servidor;
import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

public class Server {

    public HashMap<String,Holder> acoesHolders = new HashMap<>(); //mapa com o conjunto de empresas existentes e as acoes disponiveis

//    public LinkedList<Ordem> filaOrdens = new LinkedList<>(); //fila com as ordens prontas a executar - FIFO

    public HashMap<String, Ordem> ordensConcluidas = new HashMap<>(); //mapa com as ordens já concluídas, se calhar deviamos por uma resposta também aqui nao?

//    public HashMap<String,Ordem> ordensPendentes = new HashMap<>(); //mapa com as ordenas atribuidas ainda nao concluidas

//    public HashSet<String> pedidos = new HashSet<>(); //conjunto com os identificadores de pedidos já vistos

    public SpreadConnection connection = new SpreadConnection();
    public SpreadGroup group = new SpreadGroup();
    public static String nomeGrupo = "servidores";

    public Serializer s = Protocol.newSerializer();


    //recuperacao de estado
    public boolean descarta = true;
    public boolean estadoRecuperado;
    public ArrayList<Object> fila = new ArrayList<>();
    public String idState;

    public HashSet<Integer> recebidas = new HashSet<>();
    public ArrayList<byte[]> mensagensRecebidas = new ArrayList<>();
    public int tamanhoTotal = 0;

    public static int MAXSIZE = 10;//*1000;


    public LogInterface log;



    public ArrayList<byte[]> getStateMessages(String id){
        StateReply sr = new StateReply(acoesHolders,ordensConcluidas);

        byte[] aux = s.encode(sr);
        int tamanho = aux.length;

        int nrMensagens = tamanho / MAXSIZE;

        if(tamanho % MAXSIZE != 0){
            nrMensagens++;
        }

        ArrayList<byte[]> resposta = new ArrayList<>();
        for(int i = 0; i < nrMensagens; i++){
            System.out.println("Mensagem de estado: " + i);
            int atual = i * MAXSIZE;
            int proximo = (i+1) * MAXSIZE;

            if(tamanho < proximo){
                proximo = tamanho;
            }

            StateTransfer st = new StateTransfer(id, Arrays.copyOfRange(aux,atual,proximo),i,(i+1)==nrMensagens);
            byte[] a = s.encode(st);
            resposta.add(a);
        }

        return resposta;
    }


    public BasicMessageListener bml = new BasicMessageListener() {
        @Override
        public void messageReceived(SpreadMessage spreadMessage) {

            Object o = s.decode(spreadMessage.getData());

            if(!estadoRecuperado){
                if(!descarta){

                    if(o instanceof StateTransfer){
                        //recupera estado
                        StateTransfer st = (StateTransfer)o;
                        System.out.println("Recebi mensagem de estado: " + st.numero);

                        if(!recebidas.contains(st.numero)){
                            System.out.println("Vou considerar a mensagem de estado: " + st.numero);
                            recebidas.add(st.numero);

                            mensagensRecebidas.add(st.msg);
                            tamanhoTotal += st.msg.length;
                            estadoRecuperado = st.last;

                            if(estadoRecuperado){
                                System.out.println("Estado recuperado");
                                recuperaEstado();
                                trataFila();
                                System.out.println("Fila tratada");
                            }
                        }
                        else{
                            System.out.println("Recebi uma repetida!");
                        }

                    }
                    else{
                        fila.add(spreadMessage);
                    }
                }
                else{
                    if(o instanceof StateRequest){
                        StateRequest sr = (StateRequest)o;

                        descarta = !sr.id.equals(idState);
                    }
                }
                return;
            }

            trataMensagem(spreadMessage);

            /*if(o instanceof Ordem){
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
            else if(o instanceof StateRequest){

                System.out.println("Recebi mensagem de pedido de estado!");

                ArrayList<byte[]> estado = getStateMessages(((StateRequest)o).id);

                for(byte[] b: estado){
                    SpreadMessage sm = new SpreadMessage();
                    sm.addGroup(spreadMessage.getSender());
                    sm.setData(b);
                    sm.setReliable();
                    sm.setFifo();
                    try {
                        connection.multicast(sm);
                    } catch (SpreadException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                System.out.println("Erro, recebi algo que não estava à espera! " + o.getClass());
            }*/

        }
    };

    private void trataFila() {
        for(Object o: fila)
            trataMensagem(o);
    }

    private void trataMensagem(Object oA) {
        if(!(oA instanceof SpreadMessage)){
            System.out.println("Não tenho em fila uma spread message!" + oA.getClass());
            return;
        }

        SpreadMessage spreadMessage = (SpreadMessage)oA;
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
        else if(o instanceof StateRequest){
            System.out.println("Recebi pedido de estado!");

            ArrayList<byte[]> estado = getStateMessages(((StateRequest)o).id);

            for(byte[] b: estado){
                SpreadMessage sm = new SpreadMessage();
                sm.addGroup(spreadMessage.getSender());
                sm.setData(b);
                sm.setReliable();
                sm.setFifo();
                try {
                    connection.multicast(sm);
                } catch (SpreadException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            System.out.println("Erro, recebi algo que não estava à espera! " + o.getClass());
        }

        log.writeCheckpoint(new StateLog(acoesHolders,ordensConcluidas,0));
    }

    private void recuperaEstado() {
        byte[] estadoBytes = new byte[tamanhoTotal];
        ByteBuffer buff = ByteBuffer.wrap(estadoBytes);

        for(byte[] b : mensagensRecebidas){
            buff.put(b);
        }

        byte[] combined = buff.array();

        StateReply sr = s.decode(combined);

        this.acoesHolders.putAll(sr.acoesHolders);
        this.ordensConcluidas.putAll(sr.ordensConcluidas);
    }

    public Server(boolean recupera, String id) throws UnknownHostException, SpreadException {

        log = new LogInterface(id+"-update.log",id+"checkpoint.log");
        log.readLogCheckpoint();

        connection.connect(InetAddress.getByName(Config.spreadHost), 0, null, false, false);
        //para já não precisamos de nos preocupar com o group membership visto que vamos usar ativa

        estadoRecuperado = !recupera;
        idState = UUID.randomUUID().toString();

        group.join(connection, Config.nomeGrupo);
        connection.add(bml);


        if(!estadoRecuperado) {
            StateRequest sr = new StateRequest(idState);
            SpreadMessage sm = new SpreadMessage();
            sm.setData(s.encode(sr));
            sm.addGroup(Config.nomeGrupo);
            sm.setAgreed();
            sm.setReliable();
            try {
                connection.multicast(sm);
            } catch (SpreadException e) {
                e.printStackTrace();
            }
        }

        while(true){
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /*public void introduzOrdem(Ordem o){
        filaOrdens.push(o);
        pedidos.add(o.id);
    }*/

    /*public Ordem getOrdem(){
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
    }*/

    //falta ver como vamos tratar as mensagens e como vamos executar os pedidos
}
