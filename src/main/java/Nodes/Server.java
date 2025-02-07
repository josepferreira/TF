package Nodes;

import Configuration.Config;
import Configuration.Protocol;
import Log.LogEntry;
import Log.LogInterface;
import Messages.Operations.*;
import Messages.Replication.StateReply;
import Messages.Replication.StateRequest;
import Messages.Replication.StateTransfer;
import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

public class Server {

    public HashMap<String, Holder> acoesHolders = new HashMap<>(); //mapa com o conjunto de empresas existentes e as acoes disponiveis

//    public LinkedList<Ordem> filaOrdens = new LinkedList<>(); //fila com as ordens prontas a executar - FIFO

    public LinkedHashMap<String, Operacao> ordensConcluidas = new LinkedHashMap<>(); //mapa com as ordens já concluídas, se calhar deviamos por uma resposta também aqui nao?

//    public HashMap<String,Ordem> ordensPendentes = new HashMap<>(); //mapa com as ordenas atribuidas ainda nao concluidas

//    public HashSet<String> pedidos = new HashSet<>(); //conjunto com os identificadores de pedidos já vistos

    public SpreadConnection connection = new SpreadConnection();
    public SpreadGroup group = new SpreadGroup();

    public Serializer s = Protocol.newSerializer();
    public String idServer;


    //recuperacao de estado
    public boolean descarta = true;
    public boolean estadoRecuperado;
    public ArrayList<Object> fila = new ArrayList<>();
    public String idState;
    public boolean primeiraMembership = true;
    public AdvancedMessageListener aml = new AdvancedMessageListener() {
        @Override
        public void regularMessageReceived(SpreadMessage spreadMessage) {
            Object o = s.decode(spreadMessage.getData());

            if (!estadoRecuperado) {
                if (!descarta) {

                    if (o instanceof StateTransfer) {
                        //recupera estado
                        StateTransfer st = (StateTransfer) o;
                        System.out.println("Recebi mensagem de estado: " + st.numero);

                        if (!recebidas.contains(st.numero)) {
                            recebidas.add(st.numero);

                            mensagensRecebidas.add(st.msg);
                            tamanhoTotal += st.msg.length;
                            estadoRecuperado = st.last;

                            if (estadoRecuperado) {
                                System.out.println("Estado recuperado");
                                recuperaEstado();
                                trataFila();
                            }
                        } else {
                            System.out.println("Recebi uma repetida!");
                            trataFila();
                        }

                    } else {
                        fila.add(spreadMessage);
                    }
                } else {
                    if (o instanceof StateRequest) {
                        StateRequest sr = (StateRequest) o;

                        descarta = !sr.id.equals(idState);
                    }
                }
                return;
            }

            trataMensagem(spreadMessage);
        }

        @Override
        public void membershipMessageReceived(SpreadMessage spreadMessage) {
            MembershipInfo info = spreadMessage.getMembershipInfo();
            if(primeiraMembership && info.getGroup().toString().equals(Config.nomeGrupo)) {

                for(SpreadGroup sg: info.getMembers()){
                    System.out.println(sg);
                }

                primeiraMembership = false;

                estadoRecuperado = (info.getMembers().length == 1);

                if (!estadoRecuperado) {
                    System.out.println("Tenho: " + nrUpdates + " updates!");
                    StateRequest sr = new StateRequest(idState, nrUpdates);
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
                } else {
                    System.out.println("Tenho o estado recuperado");
                }
            }
        }
    };


    public HashSet<Integer> recebidas = new HashSet<>();
    public ArrayList<byte[]> mensagensRecebidas = new ArrayList<>();
    public int tamanhoTotal = 0;

    public static int MAXSIZE = 80 * 1000;


    public LogInterface log;
    public int nrUpdates = 0;

    public LinkedHashMap<String, Operacao> getStateOrders(int updatesRealizados) {
        LinkedHashMap<String, Operacao> res = new LinkedHashMap<>();
        int updateAtual = 0;

        for (Map.Entry<String, Operacao> entry : ordensConcluidas.entrySet()) {
            if (updateAtual >= updatesRealizados) {
                res.put(entry.getKey(), entry.getValue());
            }
            updateAtual++;
        }

        return res;
    }

    public ArrayList<byte[]> getStateMessages(String id, int updatesRealizados) {
        StateReply sr = new StateReply(getStateOrders(updatesRealizados));

        byte[] aux = s.encode(sr);
        int tamanho = aux.length;

        int nrMensagens = tamanho / MAXSIZE;

        if (tamanho % MAXSIZE != 0) {
            nrMensagens++;
        }

        ArrayList<byte[]> resposta = new ArrayList<>();
        for (int i = 0; i < nrMensagens; i++) {
            int atual = i * MAXSIZE;
            int proximo = (i + 1) * MAXSIZE;

            if (tamanho < proximo) {
                proximo = tamanho;
            }

            StateTransfer st = new StateTransfer(id, Arrays.copyOfRange(aux, atual, proximo), i, (i + 1) == nrMensagens);
            byte[] a = s.encode(st);
            resposta.add(a);
        }

        return resposta;
    }

    private void trataFila() {
        for (Object o : fila)
            trataMensagem(o);
    }

    private boolean executaOrdemCompra(OrdemCompra oc) {
        Holder h = acoesHolders.get(oc.holder);
        Holder aux = acoesHolders.get(oc.comprador); //comprador das acoes
        boolean res = false;
        if (h != null && aux != null) {
            res = h.compra(oc.quantidade);
            if (res) {
                aux.adiciona(oc.quantidade);
            }
        } else {
            if (h == null) {
                // System.out.println("Holder é null! O que fazer???");
            } else {
                //System.out.println("Comprador é null! O que fazer???");
            }
        }
        return res;
    }

    private boolean executaOrdemVenda(Ordem ord) {
        Holder h = acoesHolders.get(ord.holder);
        boolean res = false;
        if (h != null) {
            res = h.venda(ord.quantidade);
        } else {
            //System.out.println("Holder não existe!!! O que fazer???");
//            Holder ho = new Holder(ord.holder,ord.quantidade);
//            acoesHolders.put(ord.holder,ho);
        }

        return res;
    }

    private boolean executaRegisto(Registo r) {
        boolean res = false;

        if (!acoesHolders.containsKey(r.holder)) {

            Holder n = new Holder(r.holder, r.acoes);

            acoesHolders.put(r.holder, n);

            res = true;

        }
        return res;
    }

    private void trataMensagem(Object oA) {
        if (!(oA instanceof SpreadMessage)) {
            //System.out.println("Não tenho em fila uma spread message!" + oA.getClass());
            return;
        }

        SpreadMessage spreadMessage = (SpreadMessage) oA;
        Object o = s.decode(spreadMessage.getData());
//        System.out.println(o);

        if (o instanceof Ordem) {

            Ordem ord = (Ordem) o;

            boolean res = false;

            if (o instanceof OrdemCompra) {
                OrdemCompra oc = (OrdemCompra) ord;
                res = executaOrdemCompra(oc);
            } else {
                res = executaOrdemVenda(ord);
            }

            ord.resposta = res;

            ordensConcluidas.put(ord.id, ord);

            log.writeLogUpdates(ord);

            RespostaOrdem ro = new RespostaOrdem(ord.id, res);
            SpreadMessage sm = new SpreadMessage();
            sm.setData(s.encode(ro));
            sm.addGroup(spreadMessage.getSender());
            sm.setReliable();
            try {
                connection.multicast(sm);
            } catch (SpreadException e) {
                e.printStackTrace();
            }
        } else if (o instanceof Registo) {
            Registo r = ((Registo) o);

            boolean res = executaRegisto(r);

            r.resposta = res;
            ordensConcluidas.put(r.id, r);

            log.writeLogUpdates(r);

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


        } else if (o instanceof PedidoHolders) {
            String id = ((PedidoHolders) o).id;
            RespostaHolders rh = new RespostaHolders(id, acoesHolders);

            SpreadMessage sm = new SpreadMessage();
            sm.setData(s.encode(rh));
            sm.addGroup(spreadMessage.getSender());
            sm.setReliable();

            try {
                File yourFile = new File("estado-" + idServer + ".txt");
                yourFile.createNewFile(); // if file already exists will do nothing
                BufferedWriter writer = new BufferedWriter(new FileWriter(yourFile));
                writer.write(acoesHolders.toString());
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                connection.multicast(sm);
            } catch (SpreadException e) {
                e.printStackTrace();
            }
        } else if (o instanceof StateRequest) {
            System.out.println("Recebi pedido de estado!");

            ArrayList<byte[]> estado = getStateMessages(((StateRequest) o).id, ((StateRequest) o).updates);

            for (byte[] b : estado) {
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
        } else {
//            System.out.println("Erro, recebi algo que não estava à espera! " + o.getClass());
        }


    }

    private void recuperaEstado() {
        byte[] estadoBytes = new byte[tamanhoTotal];
        ByteBuffer buff = ByteBuffer.wrap(estadoBytes);

        for (byte[] b : mensagensRecebidas) {
            buff.put(b);
        }

        byte[] combined = buff.array();

        StateReply sr = s.decode(combined);

        System.out.println("Recupera estado!");
        //System.out.println(sr.ordensConcluidas);

        for (Map.Entry<String, Operacao> entry : sr.ordensConcluidas.entrySet()) {
//            System.out.println(entry.getValue());
            Operacao o = entry.getValue();
            if (o instanceof OrdemCompra) {
                boolean res = executaOrdemCompra((OrdemCompra) o);
                o.resposta = res;
                log.writeLogUpdates(o);
                this.ordensConcluidas.put(entry.getKey(), o);
            } else if (o instanceof Ordem) {
                boolean res = executaOrdemVenda((Ordem)o);
                o.resposta = res;
                log.writeLogUpdates(o);
                this.ordensConcluidas.put(entry.getKey(), o);
            } else if (o instanceof Registo) {
                boolean res = executaRegisto((Registo) o);
                o.resposta = res;
                log.writeLogUpdates(o);
                this.ordensConcluidas.put(entry.getKey(), o);
            }
        }

    }

    public void recuperaEstadoLog() {
        for (LogEntry le : log.readLogUpdates()) {
            //fazer update
            if (le.operacao instanceof OrdemCompra) {
                executaOrdemCompra((OrdemCompra) le.operacao);
            } else if (le.operacao instanceof Ordem) {
                executaOrdemVenda((Ordem) le.operacao);
            } else if (le.operacao instanceof Registo) {
                executaRegisto((Registo) le.operacao);
            }
            ordensConcluidas.put(le.operacao.id, le.operacao);
            nrUpdates++;
        }

//        System.out.println("NR: " + nrUpdates);
    }

    public Server(String id) throws UnknownHostException, SpreadException {


        connection.connect(InetAddress.getByName(Config.spreadHost), 0, null, false, true);
        //para já não precisamos de nos preocupar com o group membership visto que vamos usar ativa

        idState = UUID.randomUUID().toString();
        log = new LogInterface(id + "-update.log");
        recuperaEstadoLog();

        group.join(connection, Config.nomeGrupo);
        connection.add(aml);

        idServer = id;

        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
