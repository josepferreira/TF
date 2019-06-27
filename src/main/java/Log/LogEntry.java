package Log;

import Messages.Operations.Operacao;
import Messages.Operations.Ordem;

public class LogEntry {
    public int checkpoint;
    public Operacao operacao;

    public LogEntry(int checkpoint, Operacao operacao) {
        this.checkpoint = checkpoint;
        this.operacao = operacao;
    }
}
