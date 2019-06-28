package UI;

import Nodes.Holder;
import Nodes.Stub;
import com.google.common.primitives.Longs;
import spread.SpreadException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class ClienteAvaliacao {

    static class Difere{
        public String operacao;
        public long quantidade;
        public String holder;
        public String comprador;

        public boolean resSA;
        public boolean resServer;

        public Difere(String operacao, long quantidade, String holder, String comprador, boolean resSA, boolean resServer) {
            this.operacao = operacao;
            this.quantidade = quantidade;
            this.holder = holder;
            this.comprador = comprador;
            this.resSA = resSA;
            this.resServer = resServer;
        }

        @Override
        public String toString() {
            return "Difere{" +
                    "operacao='" + operacao + '\'' +
                    ", quantidade=" + quantidade +
                    ", holder='" + holder + '\'' +
                    ", comprador='" + comprador + '\'' +
                    ", resSA=" + resSA +
                    ", resServer=" + resServer +
                    '}';
        }
    }
    static String holder = "holder";
    static int quantos = 20;
    static Random r = new Random();
    static ArrayList<String> holdersE = new ArrayList<>();

    public static long quantidade(){
        Long enviar = ThreadLocalRandom.current().nextLong(1,100);
        return enviar;
    }

    public static long quantidadeRegisto(){
        Long enviar = ThreadLocalRandom.current().nextLong(100,1000);
        return enviar;
    }

    public static String holder(){
        return holdersE.get(r.nextInt(holdersE.size()));
    }

    public static String newHolder(){
        int a = r.nextInt(20) + quantos - 5;
        return holder+a;
    }

    public static void comparaBoolean(boolean a, boolean b, int i){
        if(a == b){
        }
        else{
            System.out.println("Resultado de operacao: " + i + " DIFERENTE!");
        }
    }

    public static void main(String[] args) throws SpreadException, UnknownHostException, ExecutionException, InterruptedException {
        Stub stub = new Stub();
        ServidorAvaliacao sa = new ServidorAvaliacao();

        for(int i = 0; i < quantos; i++){
            long q = quantidadeRegisto();
            boolean a = stub.regista(holder+i,q).get();
            boolean b = sa.regista(holder+i,q);
            comparaBoolean(a,b,i);
            holdersE.add(holder+i);
        }

        holdersE.add(holder+1000);
        holdersE.add(holder+350);
        holdersE.add(holder+600);

        ArrayList<Difere> diferem = new ArrayList<>();
        for(int i = 0; i < 10000; i++) {

            int op = r.nextInt(10);
            switch(op){
                case 0:
                    //registo
                    String h = newHolder();
                    long q = quantidadeRegisto();
                    boolean a = stub.regista(h,q).get();
                    boolean b = sa.regista(h,q);
                    comparaBoolean(a,b,i+quantos);
                    break;
                default:
                    int op2 = r.nextInt(2);
                    switch (op2){
                        case 0:
                            //compra
                            String h2 = holder();
                            long q2 = quantidade();
                            String c = holder();

                            boolean a2 = stub.compra(h2,q2,c).get();
                            boolean b2 = sa.compra(h2,q2,c);
                            comparaBoolean(a2,b2,i+quantos);
                            if(a2 != b2){
                                diferem.add(new Difere("venda",q2,h2,c,b2,a2));
                            }
                            break;
                        default:
                            //venda
                            String h3 = holder();
                            long q3 = quantidade();

                            boolean a3 = stub.venda(h3,q3).get();
                            boolean b3 = sa.venda(h3,q3);
                            comparaBoolean(a3,b3,i+quantos);
                            if(a3 != b3){
                                diferem.add(new Difere("venda",q3,h3,null,b3,a3));
                            }
                            break;
                    }
                    break;

            }
        }

        HashMap<String, Holder> estadoSA = sa.holders();
        HashMap<String, Holder> estadoServer = stub.holders().get();

        System.out.println("Estados iguais: " + estadoSA.equals(estadoServer));

        /*System.out.println("Servidor");
        System.out.println(estadoServer);
        System.out.println();
        System.out.println("SA");
        System.out.println(estadoSA)*/;

        for(Difere d: diferem){
            System.out.println(d);
        }
    }
}
