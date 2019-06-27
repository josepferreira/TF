package Messages.Replication;

public class StateRequest {
    public String id;
    public int updates;

    public StateRequest(String id, int updates) {
        this.id = id;
        this.updates = updates;
    }
}
