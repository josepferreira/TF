package UI;

import Nodes.Server;
import spread.SpreadException;

import java.net.UnknownHostException;
import java.util.Scanner;

public class Servidor {

    public static void main(String[] args) throws SpreadException, UnknownHostException {
        if(args.length == 0){
            System.out.println("Coloque no mínimo um identificador nos argumentos");
            return;
        }
        Server s = new Server(args[0]);
    }
}
