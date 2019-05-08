//This class processes all the user input from the GUI. It separates commands from chat messages and handles both
//accordingly.

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class UserInputHandler implements Runnable{
    private byte[] message;
    private NetworkManager networkManager;
    private PipedInputStream pipeIn1;
    private PipedOutputStream pipeOut2;
    private PipedOutputStream pipeOut4;


    public UserInputHandler(PipeBundle pipeBundle1,PipeBundle pipeBundle2, NetworkManager networkManager){
        pipeIn1 = (PipedInputStream) pipeBundle1.getInputPipe();
        pipeOut2 = (PipedOutputStream) pipeBundle2.getOutputPipe();

        this.networkManager = networkManager;
    }
    //If there is to be any sort of logic beyond "forward message to everyone", this is where it goes.
    public void run(){
        try {
            while (true) {
                byte[] input = new byte[1024];
                pipeIn1.read(input, 0, 1024);

                String message = new String(input, "UTF-8");
                if("!".equals(message.substring(0,1))){
                    System.out.println("Parsing command in UserInputHandler!");
                    parseCommand(message);
                }
                else {
                    byte[] output = new byte[1024];
                    for (int i = 96; i < input.length - 96 && i < 992; i++) {
                        output[i] = input[i - 96];
                    }
                    sendMessage(output);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void sendMessage(byte[] s){
        if(s.length > 0) {
            DecentralizedChatPacket p = new DecentralizedChatPacket(s);
            p.setInstruction("message");
            networkManager.sendMessage(p);
        }
    }
    private void parseCommand(String command){
        if("!peers".equals(command.substring(0,6))){
            networkManager.printPeerMapContents();
        }
    }
}
