package Log;

import Messages.Operations.Operacao;

public class LogEntry {
    public Operacao operacao;

    public LogEntry(Operacao operacao) {
        this.operacao = operacao;
    }
}
