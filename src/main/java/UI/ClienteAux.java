package UI;

import Nodes.Stub;
import com.google.common.primitives.Longs;
import spread.SpreadException;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class ClienteAux {

    public static void main(String[] args) throws SpreadException, UnknownHostException {
        Stub stub = new Stub();

        stub.regista("ola",1000000);
        stub.regista("cenas",1000000);

        for(int i = 0; i < 100000; i++){
            Long enviar = ThreadLocalRandom.current().nextLong(1,200);
            Long aux = ThreadLocalRandom.current().nextLong(0,10);
            if(aux > 6){
                try {
                    stub.venda("cenas", enviar).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            else{
                try {
                    stub.compra("cenas",enviar,"ola").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
