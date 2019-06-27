package UI;

import Nodes.Server;
import spread.SpreadException;

import java.net.UnknownHostException;
import java.util.Scanner;

public class Servidor {

    public static void main(String[] args) throws SpreadException, UnknownHostException {
        if(args.length == 0){
            System.out.println("Coloque no mínimo um identificador nos argumentos");
            Server s = new Server("server1");
            return;
        }
        Server s = new Server(args.length == 1, args[0]);
    }
}
