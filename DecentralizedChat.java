//DecentralizedChat is a decentralized chat application. It takes a port number as its first argument, and an IP address to
//any active peer in the desired group as the second argument.
//This class sets up the program by creating the various objects that do stuff, and the lines of communication between
//them. See those classes for more information on what they actually do.

import java.io.*;
import java.net.*;
import java.lang.*;
import java.security.SecureRandom;

public class DecentralizedChat {
    private NetworkManager networkManager;
    private GUI userInterface;
    private String entryHostAsString;
    private int port;
    private InetAddress hostInetAddress;
    private String identifier;


    public static void main(String[] args){
        String port = "4510";
        if(args.length > 0){
            new DecentralizedChat(args[0], port).run();
        }
        else{
            new DecentralizedChat("localhost", port).run();
        }
    }
    private DecentralizedChat(String host, String portAsString){
        generateIdentifier();
        entryHostAsString = host;
        port = Integer.parseInt(portAsString);

    }

    private void generateIdentifier(){
        SecureRandom secureRandom;
        byte[] random = new byte[32];
        secureRandom = new SecureRandom();
        secureRandom.nextBytes(random);
        CryptoManager cryptoManager = new CryptoManager();
        identifier = cryptoManager.getSHA256AsHexString(random);

    }

    private void run(){
        setUp();
        closeDown();
    }

    private void setUp(){

        try {
            hostInetAddress = InetAddress.getByName(entryHostAsString);
        }catch(Exception e){e.printStackTrace();}

        //The following sets up all the stuff that the various classes need to talk to each other and bundles
        //them into a PipeBundle to be sent onwards.

        //Pipe from GUI to UserInputHandler
        PipedInputStream pipeIn1 = new PipedInputStream();
        PipedOutputStream pipeOut1 = new PipedOutputStream();

        //Pipe from UserInputHandler to GUI.
        PipedInputStream pipeIn2 = new PipedInputStream();
        PipedOutputStream pipeOut2 = new PipedOutputStream();

        //Pipe from IncomingMessageHandler to GUI.
        PipedInputStream pipeIn3 = new PipedInputStream();
        PipedOutputStream pipeOut3 = new PipedOutputStream();

        //Pipe from DatagramListen to IncomingMessageHandler
        PipedOutputStream pipeOut4 = new PipedOutputStream();
        PipedInputStream pipeIn4 = new PipedInputStream();

        try {
            pipeIn1.connect(pipeOut1);
            pipeIn2.connect(pipeOut2);
            pipeIn3.connect(pipeOut3);
            pipeIn4.connect(pipeOut4);

        }catch(Exception e){
            e.printStackTrace();
        }
        PipeBundle pipeBundle1 = new PipeBundle(pipeIn1, pipeOut1);
        PipeBundle pipeBundle2 = new PipeBundle(pipeIn2, pipeOut2);
        PipeBundle pipeBundle3 = new PipeBundle(pipeIn3, pipeOut3);
        PipeBundle pipeBundle4 = new PipeBundle(pipeIn4, pipeOut4);
        networkManager = new NetworkManager(pipeBundle4, port, hostInetAddress, identifier);
        Thread incomingMessageHandler = new Thread(new IncomingMessageHandler(pipeBundle3, pipeBundle4, networkManager));
        System.out.println("NetworkManager running!");
        incomingMessageHandler.start();
        Thread userInputHandler = new Thread(new UserInputHandler(pipeBundle1, pipeBundle2, networkManager));
        userInputHandler.start();
        userInterface = new GUI(pipeBundle1, pipeBundle2, pipeBundle3);




    }
    private void closeDown(){
        System.exit(0);
    }

}