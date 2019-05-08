//Sends a single packet to a single recipient using its own thread. It listens for an acknowledgement of the package
//being received, and if one is not received by socketTimeout ms, retries for maxAttempts number of times. Using a new
//thread for every message to every recipient might not be sane, or scalable, but from what I've read, it shouldn't be
//a problem on most hardware unless the user count is in the several hundreds or possibly thousands, which considering
//it's a chat room, shouldn't be the case.

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DatagramSend implements Runnable {
    private DatagramSocket socket;
    private DatagramPacket packet;
    private NetworkManager networkManager;
    private int maxAttempts = 10;
    private int socketTimeout = 2000;
    private boolean done = false;

    //The constructor takes the packet and a reference to NetworkManager.
    public DatagramSend(DatagramPacket packet, NetworkManager networkManager) {
        this.packet = packet;
        this.networkManager = networkManager;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(socketTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {

        for(int i = 0 ; i < maxAttempts && !done ; i++)
        try {
            //Sends the packet.
            socket.send(packet);
            //Prepares for receiving a confirmation packet.
            byte[] buf = new byte[1024];
            DatagramPacket confirmation = new DatagramPacket(buf, 1024);
            socket.receive(confirmation);
            DecentralizedChatPacket received = new DecentralizedChatPacket(confirmation.getData());
            DecentralizedChatPacket sent = new DecentralizedChatPacket(packet.getData());
            if(received.getConfirmationHash().equals(sent.getHash())){
                done = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!done){
            System.err.println("Packet delivery failed.");
        }
        socket.close();
        //Notifies NetworkManager that the thread is done and further threads can be started.

    }
}