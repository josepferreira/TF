package Avaliacao;

import Nodes.Stub;
import org.json.JSONObject;
import spread.SpreadException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

class AvaliaDesempenho implements Runnable{
    public ArrayList<Long> medicoes = new ArrayList<>();
    private int numeroMedicoes;
    public Stub s;
    public int nrH = 20;
    public String holder = "holder";
    public Random r = new Random();

    public long quantidade(){
        Long enviar = ThreadLocalRandom.current().nextLong(1,100);
        return enviar;
    }

    public long quantidadeRegisto(){
        Long enviar = ThreadLocalRandom.current().nextLong(100,1000);
        return enviar;
    }

    public String holder(){
        return holder + r.nextInt();
    }

    public AvaliaDesempenho(String d, int nm) throws SpreadException, UnknownHostException {
        numeroMedicoes = nm;
        s = new Stub();
    }

    public void clear(){
        medicoes.clear();
    }

    public void run() {
        for(int i = 0; i < numeroMedicoes; i++){
            try{
                long q;
                String h;
                String c;
                int op = r.nextInt(4);
                switch(op) {
                    case 0:
                        //registo
                        h = holder();
                        q = quantidadeRegisto();
                        long timeInicial = System.currentTimeMillis();
                        s.regista(h,q).get();
                        long time = System.currentTimeMillis() - timeInicial;
                        medicoes.add(time);
                        break;
                    default:
                        int op2 = r.nextInt(2);
                        switch (op2) {
                            case 0:
                                //compra
                                h = holder();
                                q = quantidade();
                                c = holder();
                                timeInicial = System.currentTimeMillis();
                                s.compra(h,q,c).get();
                                time = System.currentTimeMillis() - timeInicial;
                                medicoes.add(time);
                                break;
                            default:
                                //venda
                                h = holder();
                                q = quantidade();
                                timeInicial = System.currentTimeMillis();
                                s.venda(h,q).get();
                                time = System.currentTimeMillis() - timeInicial;
                                medicoes.add(time);
                                break;
                        }
                        break;
                }


            }
            catch(Exception e){
                System.out.println("Excepcao");
                i--;
            }
        }
    }
}

public class AvaliacaoDesempenho{
    public static void main(String[] args) throws Exception{
        JSONObject jo = new JSONObject();
        ArrayList<AvaliaDesempenho> clientes = new ArrayList<>();
        int nClientes = 25;
        int nTestes = 1000;
        for(int i = 0; i < nClientes; i++){
            ArrayList<Thread> threads = new ArrayList<>();
            AvaliaDesempenho ad = new AvaliaDesempenho(args[0], nTestes);

            clientes.add(ad);
            for(AvaliaDesempenho adp: clientes){
                Thread t = new Thread(adp);
                threads.add(t);
            }

            for(Thread ta: threads){
                ta.start();
            }

            for(Thread ta: threads){
                ta.join();
            }

            ArrayList<Long> medicoesT = new ArrayList<>();
            for(AvaliaDesempenho c: clientes){
                medicoesT.addAll(c.medicoes);
                c.clear();
            }

            // System.out.println(medicoesT);
            jo.put(""+(i+1),medicoesT);
            System.out.println("Terminei: " + i);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(args[0]));
        writer.write(jo.toString());

        writer.close();

    }
}
