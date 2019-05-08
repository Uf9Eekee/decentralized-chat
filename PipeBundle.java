import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;

public class PipeBundle {

    private Object in;
    private Object out;

    public PipeBundle(PipedInputStream pipedInputStream, PipedOutputStream pipedOutputStream){
        in = pipedInputStream;
        out = pipedOutputStream;
    }
    public Object getInputPipe(){
        return in;
    }
    public Object getOutputPipe(){
        return out;
    }
}