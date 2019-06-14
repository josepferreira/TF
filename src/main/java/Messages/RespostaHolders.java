package Messages;

import Nodes.Holder;

import java.util.HashMap;

public class RespostaHolders {
    public String id;
    public HashMap<String, Holder> holders;

    public RespostaHolders(String id, HashMap<String, Holder> holders) {
        this.id = id;
        this.holders = holders;
    }
}
