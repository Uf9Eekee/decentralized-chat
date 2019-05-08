//This class represents a peer on the network, and takes InetAddress as well as identifier, in hex string form.

import java.net.InetAddress;

public class Peer {
    private InetAddress address;
    private String name;
    private String identifier;

    public Peer(InetAddress address, String identifier){
        this.address = address;
        this.name = name;
        this.identifier = identifier;
    }
    public InetAddress getAddress(){
        return address;
    }
    public String getName(){
        return name;
    }

    //Returns the identifier hash as a hex string.
    public String getIdentifier(){
        return identifier;
    }
}