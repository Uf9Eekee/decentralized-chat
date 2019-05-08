import java.net.InetAddress;

public class UdpConnection{
    int listenPort;
    int sendPort;
    InetAddress peerAddress;

    public UdpConnection(int sendPort, int listenPort, InetAddress peerAddress){
        this.listenPort = listenPort;
        this.sendPort = sendPort;
        this.peerAddress = peerAddress;
    }
    public int getListenPort(){
        return listenPort;
    }
    public int getSendPort(){
        return sendPort;
    }
    public InetAddress getPeerAddress(){
        return peerAddress;
    }
}