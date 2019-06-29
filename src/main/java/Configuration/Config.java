package Configuration;

import java.util.Random;

public class Config {

    public static String nomeGrupo = "servidores";
    public static String spreadHost = "localhost";
    public static int operacoesPorCheckpoint = 1000;

    public static int portaInicial = 20000;
    public static int rangePortas = 20000;
    public static String hostAtomix = "localhost";


    public static boolean eLong = true;

    public static int getPorta(){
        Random r = new Random();

        return r.nextInt(Config.rangePortas) + Config.portaInicial;
    }
}
