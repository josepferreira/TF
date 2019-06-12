package UI;

import Messages.Ordem;
import Nodes.Stub;
import spread.SpreadException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Menu {

    public ArrayList<String> opcoes = new ArrayList<>();
    public Scanner s = new Scanner(System.in);
    public Stub stub;

    public Menu() throws SpreadException, UnknownHostException {
        opcoes.add("Venda");
        opcoes.add("Compra");
        opcoes.add("Sair");

        stub = new Stub();
    }

    private long leInteiro(String msg){
        long quant = -1;
        do {
            String aux = s.nextLine();

            try {
                quant = Long.parseLong(aux);
            }
            catch(Exception e){
                System.out.println(msg);
            }
        }while(quant <= 0);
        return quant;
    }

    private String leString(String msg){
        String emp = null;
        do {
            try {
                emp = s.nextLine();

            }catch(Exception e){
                System.out.println(msg);
            }
        }while(emp == null);

        return emp;
    }

    public CompletableFuture<Boolean> compra() throws SpreadException {
        System.out.println("Quantidade a comprar: ");
        long q = leInteiro("Coloque uma quantidade válida, superior a 0!");
        System.out.println("Holder a quem comprar: ");
        String h = leString("Coloque um holder válido!");

        return stub.compra(h,q);
    }

    public CompletableFuture<Boolean> venda() throws SpreadException {
        System.out.println("Quantidade a vender: ");
        long q = leInteiro("Coloque uma quantidade válida, superior a 0!");
        System.out.println("Holder a quem vender: ");
        String h = leString("Coloque um holder válido!");

        return stub.venda(h,q);
    }

    public void sair() throws SpreadException {
        stub.close();
    }

    public void apresenta(){
        int i = 1;
        for(String op: opcoes){
            System.out.println(i + " -> " + op);
            i++;
        }
    }

    public int leOpcao(){
        System.out.println("Opção: ");
        return (int)leInteiro("Colque um valor válido, entre 1 e 3");
    }

    public void corre() throws SpreadException {
        boolean continua = true;
        do{
            apresenta();
            int op = leOpcao();
            System.out.println(op);
            switch(op){
                case 1:
                    try {
                        boolean res = venda().get();
                        System.out.println("Resultado da venda: " + res);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        boolean res = compra().get();
                        System.out.println("Resultado da compra: " + res);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
//                    sair();
                    continua = false;
                    System.out.println("Vou sair!");
            }
        }while(continua);
    }
}
