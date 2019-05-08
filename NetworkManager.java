import javax.xml.bind.DatatypeConverter;
import java.net.*;
import java.util.*;

public class NetworkManager {
    private int port;
    private HashMap<String, Peer> peerMap = new HashMap<>();
    private PipeBundle pipeBundle4;
    private String identifier;


    //constructor
    public NetworkManager(PipeBundle pipeBundle4, int port, InetAddress entryInetAddress, String identifier) {
        this.pipeBundle4 = pipeBundle4;
        this.port = port;
        this.identifier = identifier;
        initialize(entryInetAddress);

    }

    //Initiates the connection to the entry host by setting up DatagramListen and running initializeConnection();
    private void initialize(InetAddress entryHost) {
        Thread datagramListen = new Thread(new DatagramListen(pipeBundle4, port, this));
        datagramListen.start();
        initializeConnectionToEntryHost(entryHost);
        try {
            Peer localhost = new Peer(InetAddress.getLocalHost(), identifier);
            peerMap.put(identifier, localhost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Sends a handshake message to the entry host as specified by the second argument on startup.
    private void initializeConnectionToEntryHost(InetAddress entryHost) {
        try {
            Peer entry = new Peer(entryHost, DatatypeConverter.printHexBinary(new byte[32]));
            peerMap.put("entryHost", entry);
            DecentralizedChatPacket s = new DecentralizedChatPacket("");
            s.setInstruction("handshake");
            s.computeHash();
            sendMessage(s);
            s = new DecentralizedChatPacket("");
            s.setInstruction("propagatepeers");
            s.computeHash();
            sendMessage(s);
            System.out.println("Connection to entry host initialized!");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMessage(DecentralizedChatPacket s) {
        try {
            for (Peer peer : peerMap.values()) {
                s.setRecipient(peer.getIdentifier());
                s.setSender(identifier);
                s.computeHash();
                DatagramPacket p = new DatagramPacket(s.getData(), s.getData().length, peer.getAddress(), 4510);
                new Thread(new DatagramSend(p, this)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Datagram sent! IsValidPacket: " + s.isValidPacket());
    }


    public synchronized void propagatePeers(String peerIdentifier) {
        Peer recipient = peerMap.get(peerIdentifier);
        //This loop sends all the addresses in peerList to the recipient.
        for (Peer peer : peerMap.values()) {
            //The IP of the peer will be 127.0.0.1 or something like that to the peer itself, so by excluding your own
            //machine's IP in the peer propagation, this scheme will work. It will lead to some weird stuff internally
            //before a message is sent (the identifier will be wrong on the client that connected), but this won't break
            //anything. The peer obviously has the IP of the peer it's requesting peer propagation from, either way.
            if(!peer.getIdentifier().equals(identifier)) {
                DecentralizedChatPacket s = new DecentralizedChatPacket(peer.getAddress());
                s.setInstruction("peeraddress");
                s.setRecipient(peerIdentifier);
                s.setSender(identifier);
                s.computeHash();
                System.out.println("Outbound peer propagation packet is valid: " + s.isValidPacket());
                DatagramPacket p = new DatagramPacket(s.getData(), 1024, recipient.getAddress(), port);
                new Thread(new DatagramSend(p, this)).start();
                System.out.println("Peer propagation package sent!");
            }
        }
    }

    public synchronized void putPeer(Peer peer) {
            removeDuplicatePeers(peer);
            peerMap.put(peer.getIdentifier(), peer);
            System.out.println("Peer added!");
    }

    private synchronized void removeDuplicatePeers(Peer peer) {
        Iterator<Map.Entry<String, Peer>> it = peerMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, Peer> pair = it.next();
            if(pair.getValue().getAddress().equals(peer.getAddress()) || pair.getKey().equals(peer.getIdentifier())
                    || pair.getValue().getIdentifier().equals(peer.getIdentifier())){
                it.remove();
            }
        }

        for (Peer p : peerMap.values()) {
            if (p.getAddress().equals(peer.getAddress()) || p.getIdentifier().equals(peer.getIdentifier())) {
                peerMap.remove(p.getIdentifier());
                System.out.println("Duplicate peer removed!");
            }
        }
    }

    //for testing.
    public synchronized void printPeerMapContents() {
        for (Peer p : peerMap.values()) {
            System.out.println("Identifier: " + p.getIdentifier() + ", Address: " + p.getAddress());
        }
    }

    public synchronized void sendHandshakeResponse(DecentralizedChatPacket s) {
        s.setInstruction("handshakeresponse");
        InetAddress address = peerMap.get(s.getSender()).getAddress();
        DatagramPacket p = new DatagramPacket(s.getData(), 1024, address, port);
        new DatagramSend(p, this);
        System.out.println("Handshake response sent to " + s.getSender() + "!");
    }
}