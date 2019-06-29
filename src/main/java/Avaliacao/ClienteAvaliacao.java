package Avaliacao;

import Nodes.Holder;
import Nodes.Stub;
import spread.SpreadException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class ClienteAvaliacao {

    static class Op{
        public int op;
        public long q;
        public String h;
        public String c;
        CompletableFuture<Boolean> res;

        public Op(int op, long q, String h, String c, CompletableFuture<Boolean> res) {
            this.op = op;
            this.q = q;
            this.h = h;
            this.c = c;
            this.res = res;
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

    public static void comparaBoolean(boolean a, boolean b){
        if(a == b){
        }
        else{
            System.out.println("Resultado de operacao DIFERENTE!");
        }
    }

    public static void main(String[] args) throws SpreadException, UnknownHostException, ExecutionException, InterruptedException {
        Stub stub = new Stub();
        ServidorAvaliacao sa = new ServidorAvaliacao();
        int  opers = 100000;
        CompletableFuture<Boolean>[] ops = new CompletableFuture[quantos+opers];
        ArrayList<Op> opsAux = new ArrayList<>();

        for(int i = 0; i < quantos; i++){
            long q = quantidadeRegisto();
            CompletableFuture<Boolean> a = stub.regista(holder + i, q);
            ops[i] = a;
            opsAux.add(new Op(0,q,holder+i,null,a));
            final int aux = i;
            holdersE.add(holder+i);
        }

        holdersE.add(holder+1000);
        holdersE.add(holder+350);
        holdersE.add(holder+600);

        for(int i = 0; i < opers; i++) {

            if(i % 3000 == 0){
                Thread.sleep(2000);
                if(i != 0)
                    System.out.println("Enviei " + i + " pedidos!");
            }
            int op = r.nextInt(10);
            switch(op){
                case 0:
                    //registo
                    String h = newHolder();
                    long q = quantidadeRegisto();
                    CompletableFuture<Boolean> a = stub.regista(h,q);
                    ops[i+quantos] = a;
                    opsAux.add(new Op(0,q,h,null,a));

                    break;
                default:
                    int op2 = r.nextInt(2);
                    switch (op2){
                        case 0:
                            //compra
                            String h2 = holder();
                            long q2 = quantidade();
                            String c = holder();

                            CompletableFuture<Boolean> a2 = stub.compra(h2,q2,c);
                            ops[i+quantos] = a2;
                            opsAux.add(new Op(1,q2,h2,c,a2));


                            break;
                        default:
                            //venda
                            String h3 = holder();
                            long q3 = quantidade();

                            CompletableFuture<Boolean> a3 = stub.venda(h3, q3);
                            ops[i+quantos] = a3;
                            opsAux.add(new Op(2,q3,h3,null,a3));

                            break;
                    }
                    break;

            }
        }
        CompletableFuture.allOf(ops);

        for(Op o : opsAux){
            boolean r = o.res.get();
            if(r) {
                switch (o.op) {
                    case 0:
                        boolean b = sa.regista(o.h, o.q);
                        comparaBoolean(r, b);
                        break;
                    case 1:
                        boolean b1 = sa.compra(o.h, o.q, o.c);
                        comparaBoolean(r, b1);
                        break;
                    default:
                        boolean b2 = sa.venda(o.h, o.q);
                        comparaBoolean(r, b2);
                        break;
                }
            }

        }

        HashMap<String, Holder> estadoSA = sa.holders();
        HashMap<String, Holder> estadoServer = stub.holders().get();

        System.out.println("Estados iguais: " + estadoSA.equals(estadoServer));

        for(String k : estadoSA.keySet()){

            Holder h1 = estadoSA.get(k);
            Holder h2 = estadoServer.get(k);

            if(!h1.equals(h2)) {
                System.out.println("Diferentes");
                System.out.println(h1);
                System.out.println(h2);
            }
        }


    }
}
