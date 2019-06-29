package Log;

import Configuration.Protocol;
import Messages.Operations.Operacao;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;

import java.util.ArrayList;

public class LogInterface {

    public String update;
    SegmentedJournal<Object> logUpdate;
    SegmentedJournalReader<Object> readerUpdate;
    SegmentedJournalWriter<Object> writerUpdate;


    public Serializer s = Protocol.newSerializer();

    public LogInterface(String updates){
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

//        System.out.println("Ler updates");
        while(readerUpdate.hasNext()){
//            System.out.println("Entrada de update");
            res.add((LogEntry)readerUpdate.next().entry());
        }
//        System.out.println("Updates lidos");

        return res;
    }

    public void writeLogUpdates(Operacao o){

        LogEntry le = new LogEntry(o);
        writerUpdate.append(le);
    }

}