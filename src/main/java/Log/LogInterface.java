package Log;

import Configuration.Protocol;
import Messages.Operations.Operacao;
import Messages.Operations.Ordem;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;

import java.util.ArrayList;
import java.util.HashMap;

public class LogInterface {
    public String checkPoint;
    SegmentedJournal<Object> logCheckPoint;
    SegmentedJournalReader<Object> readerCheckPoint;
    SegmentedJournalWriter<Object> writerCheckPoint;


    public String update;
    SegmentedJournal<Object> logUpdate;
    SegmentedJournalReader<Object> readerUpdate;
    SegmentedJournalWriter<Object> writerUpdate;


    public Serializer s = Protocol.newSerializer();

    public LogInterface(String checkP, String updates){
        this.checkPoint = checkP;
        logCheckPoint = SegmentedJournal.builder()
                .withName(this.checkPoint)
                .withSerializer(s)
                .build();

        readerCheckPoint = logCheckPoint.openReader(0);
        writerCheckPoint = logCheckPoint.writer();

        this.update = updates;
        logUpdate = SegmentedJournal.builder()
                .withName(this.update)
                .withSerializer(s)
                .build();

        readerUpdate = logUpdate.openReader(0);
        writerUpdate = logUpdate.writer();


    }

    public ArrayList<LogEntry> readLogUpdates(){
        ArrayList<LogEntry> res = new ArrayList<>();

        System.out.println("Ler updates");
        while(readerUpdate.hasNext()){
            System.out.println("Entrada de update");
            res.add((LogEntry)readerUpdate.next().entry());
        }
        System.out.println("Updates lidos");

        return res;
    }

    public void writeLogUpdates(int c, Operacao o){

        LogEntry le = new LogEntry(c,o);
        writerUpdate.append(le);
        System.out.println("Escrito");
    }

    public StateLog readLogCheckpoint(){
        System.out.println("Ler logs");
        StateLog res = null;
        while(readerCheckPoint.hasNext()){
            res = (StateLog) readerCheckPoint.next().entry();
            System.out.println(res);
        }
        System.out.println("Li checkpoints");
        return res;
    }

    public void writeCheckpoint(StateLog sl){
        writerCheckPoint.append(sl);
    }

}