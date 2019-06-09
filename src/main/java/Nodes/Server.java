package Nodes;

import Messages.Ordem;
import spread.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Server {

    public HashMap<String,Long> acoesEmpresas = new HashMap<>(); //mapa com o conjunto de empresas existentes e as acoes disponiveis

    public LinkedList<Ordem> filaOrdens = new LinkedList<>(); //fila com as ordens prontas a executar - FIFO

    public HashMap<String,Ordem> ordensConcluidas = new HashMap<>(); //mapa com as ordens já concluídas

    public HashMap<String,Ordem> ordensPendentes = new HashMap<>(); //mapa com as ordenas atribuidas ainda nao concluidas

    public HashSet<String> pedidos = new HashSet<>(); //conjunto com os identificadores de pedidos já vistos

    public SpreadConnection connection = new SpreadConnection();
    public SpreadGroup group = new SpreadGroup();
    public String identificador;
    public static String nomeGrupo = "servidores";

    public BasicMessageListener bml = new BasicMessageListener() {
        @Override
        public void messageReceived(SpreadMessage spreadMessage) {

        }
    };

    public Server(String identificador) throws UnknownHostException, SpreadException {
        this.identificador = identificador;

        connection.connect(InetAddress.getByName("localhost"), 0, this.identificador, false, false);
        //para já não precisamos de nos preocupar com o group membership visto que vamos usar ativa

        group.join(connection, nomeGrupo);
        connection.add(bml);

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
