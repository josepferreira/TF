package UI;

import Nodes.Server;
import spread.SpreadException;

import java.net.UnknownHostException;
import java.util.Scanner;

public class Servidor {

    public static void main(String[] args) throws SpreadException, UnknownHostException {
        Server s = new Server(args.length == 0);
    }
}
