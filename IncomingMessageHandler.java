//IncomingMessageHandler gets sent incoming data from DatagramListen, parses the data, and passes it off to where it
//needs to be.

import javax.xml.bind.DatatypeConverter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;

public class IncomingMessageHandler implements Runnable{
    private PipedOutputStream pipeOut3;
    private PipedInputStream pipeIn4;
    private CryptoManager cryptoManager;
    private NetworkManager networkManager;


    public IncomingMessageHandler(PipeBundle pipeBundle3, PipeBundle pipeBundle4, NetworkManager networkManager){
        pipeOut3 = (PipedOutputStream) pipeBundle3.getOutputPipe();
        pipeIn4 = (PipedInputStream) pipeBundle4.getInputPipe();
        cryptoManager = new CryptoManager();
        this.networkManager = networkManager;
    }
    //Converts the incoming data to a DecentralizedChatPacket and figures out what to do with it based on its instruction.
    public void run() {
            while (true) {
                try {
                    byte[] payload = new byte[1024];
                    boolean messageHandled = false;
                    for (int i = 0; i < 1024; i++) {
                        payload[i] = (byte) pipeIn4.read();
                    }
                    DecentralizedChatPacket s = new DecentralizedChatPacket(payload);
                    if(s.getInstructions().equals(DatatypeConverter.printHexBinary(cryptoManager.getSHA256AsByteArray("message".getBytes())))) {
                        displayMessage(s);
                        System.out.println("Message being displayed!");
                        messageHandled = true;
                    }
                    if(s.getInstructions().equals(DatatypeConverter.printHexBinary(cryptoManager.getSHA256AsByteArray("propagatepeers".getBytes())))){
                        networkManager.propagatePeers(s.getSender());
                        System.out.println("Propagating peers!");
                        messageHandled = true;
                    }
                    if(s.getInstructions().equals(DatatypeConverter.printHexBinary(cryptoManager.getSHA256AsByteArray("peeraddress".getBytes())))){
                        System.out.println("Peer received!");
                        networkManager.putPeer(new Peer(InetAddress.getByAddress(s.getPeerAddress()), s.getSender()));
                        System.out.println("Peer propagated from" + s.getSender() + " added!");
                        messageHandled = true;
                    }
                    if(s.getInstructions().equals(DatatypeConverter.printHexBinary(cryptoManager.getSHA256AsByteArray("handshake".getBytes())))){
                        networkManager.sendHandshakeResponse(s);
                        messageHandled = true;
                    }
                    if(s.getInstructions().equals(DatatypeConverter.printHexBinary(cryptoManager.getSHA256AsByteArray("handshakeresponse".getBytes())))){
                        messageHandled = true;
                    }
                    if(!messageHandled){
                        System.out.println("A valid message was unable to be handled because its instructions were unknown.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    }
    private void displayMessage(DecentralizedChatPacket s) throws Exception{
        String message = s.getSender().substring(0,8) + ": " + s.getMessage();
        pipeOut3.write(message.getBytes("UTF-8"));
        pipeOut3.flush();
        System.out.println("Message sent to GUI!");
    }
    private void peerRequest(byte[] payload){
        byte[] group = new byte[160];
        for(int i = 0 ; i < 160 ; i++){
            group[i] = payload[i+1]; // The i+1 disregards the initial "!" in payload.
        }


    }
}
