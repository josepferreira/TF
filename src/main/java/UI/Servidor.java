package UI;

import Nodes.Server;
import spread.SpreadException;

import java.net.UnknownHostException;
import java.util.Scanner;

public class Servidor {

    public static void main(String[] args) throws SpreadException, UnknownHostException {
        System.out.println("Insira um identificador: ");
        Scanner sc = new Scanner(System.in);
        String id = sc.nextLine();
        Server s = new Server(id);
    }
}
