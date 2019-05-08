//Listens for incoming packages. Only one instance is used, created from NetworkManager.

import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DatagramListen implements Runnable {
    private PipedOutputStream pipeOut4;
    private int localPort;
    private boolean running = true;
    private DatagramSocket socket;
    private NetworkManager networkManager;


    public DatagramListen(PipeBundle pipeBundle4, int localPort, NetworkManager networkManager) {
        pipeOut4 = (PipedOutputStream) pipeBundle4.getOutputPipe();
        this.localPort = localPort;
        this.networkManager = networkManager;
    }

    public void run() {
        try {
            socket = new DatagramSocket(localPort);
            byte[] buf;
            buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (running) {
                System.out.println("Listening for packages!");
                socket.receive(packet);
                DecentralizedChatPacket s = new DecentralizedChatPacket(buf);
                if(s.isValidPacket()) {
                    networkManager.putPeer(new Peer(packet.getAddress(), s.getSender()));
                    buf = packet.getData();
                    pipeOut4.write(buf);
                    pipeOut4.flush();
                    s.makeConfirmationPacket();
                    DatagramPacket confirmation = new DatagramPacket(s.getData(), 1024, packet.getAddress(), packet.getPort());
                    socket.send(confirmation);
                    System.out.println("Packet received!");
                }
                else System.out.println("Discarded invalid packet!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}