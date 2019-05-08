//This represents the DecentralizedChat protocol as relevant to DecentralizedChat.

import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Arrays;

public class DecentralizedChatPacket implements Serializable {

    private CryptoManager cryptoManager = new CryptoManager();

    //This is the raw data
    private byte[] data = new byte[1024];

    //These are used for testing, and represent the SHA256 hash of "message" in various formats for easy access.
    private String messageHash = DatatypeConverter.printHexBinary(cryptoManager.getSHA256AsByteArray("message".getBytes()));

    //This is the raw constructor, just takes a full array of bytes. It is useful when reconstructing a
    //DecentralizedChatPacket that has been received, less so when crafting one to be sent.
    public DecentralizedChatPacket(byte[] bytes) {
        data = bytes;
    }

    //This constructor takes an InetAddress and is used for propagating addresses of users to new peers.
    public DecentralizedChatPacket(InetAddress address) {
        byte[] addressArray = address.getAddress();
        System.out.print("InetAddress as stored in DecentralizedChatPacket InetAddress constructor: ");
        for(int i = 0 ; i < addressArray.length ; i++){
            System.out.print(addressArray[i] + " ");
        }
        System.out.println();
        setContent(address.getAddress());
        System.out.println("Size of InetAddress.getAddress() array: " + address.getAddress().length);
        setInstruction("peeraddress");
    }

    //This forms a regular message packet for mass delivery to every known peer. This is currently the only type of
    //message supported.
    public DecentralizedChatPacket(String message) {
        try {
            byte[] messageArray = message.getBytes("UTF-8");
            for (int i = 0; i < messageArray.length && i < 896; i++) {
                data[i + 96] = messageArray[i];
            }
            setInstruction("message");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getData() {
        return data;
    }

    public String getSender() {
        byte[] sender = Arrays.copyOfRange(data, 0, 32);
        return DatatypeConverter.printHexBinary(sender);
    }

    public String getRecipient() {
        byte[] recipient = Arrays.copyOfRange(data, 32, 64);
        return DatatypeConverter.printHexBinary(recipient);
    }

    public String getInstructions() {
        byte[] instructions = Arrays.copyOfRange(data, 64, 96);
        return DatatypeConverter.printHexBinary(instructions);
    }

    public String getHash() {
        byte[] hash = Arrays.copyOfRange(data, 992, 1024);
        return DatatypeConverter.printHexBinary(hash);
    }

    public String getContent() {
        byte[] content = Arrays.copyOfRange(data, 96, 992);
        return new String(content);
    }

    public void setContent(byte[] content) {
        for (int i = 0; i < content.length && i < 896; i++) {
            data[i+96] = content[i];
        }
    }

    public void setInstruction(String instructionString) {
        try {
            byte[] instructionHash = cryptoManager.getSHA256AsByteArray(instructionString.getBytes("UTF-8"));

            for (int i = 0; i < 32; i++) {
                data[i+64] = instructionHash[i];
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void setSender(String sender) {
        byte[] senderArray = DatatypeConverter.parseHexBinary(sender);
        for (int i = 0; i < 32; i++) {
            data[i] = senderArray[i];
        }
    }
    public void setSender(byte[] sender){
        for(int i = 0 ; i < 32 ; i++){
            data[i] = sender[i];
        }
    }

    public void setRecipient(String sender) {
        byte[] senderArray = DatatypeConverter.parseHexBinary(sender);
        for (int i = 0; i < 32; i++) {
            data[i + 32] = senderArray[i];
        }
    }
    public void setRecipient(byte[] recipient){
        for(int i = 0 ; i < 32 ; i++){
            data[i+32] = recipient[i];
        }
    }

    public boolean isConfirmation() {
        if (DatatypeConverter.printHexBinary(cryptoManager.getSHA256AsByteArray("confirmation".getBytes())).equals(getInstructions())) {
            return true;
        }
        return false;
    }

    //Use this to return content as a text message. If the instructions field is not set to the message, returns null.
    public String getMessage() {
        if (messageHash.equals(getInstructions())) {
            return getContent();
        }
        return null;
    }

    //Returns the hex string of the first 32 bytes of data in the content part of the packet, which is where the hash is
    //stored in a confirmation packet.
    public String getConfirmationHash() {
        if (isConfirmation()) {
            return DatatypeConverter.printHexBinary(Arrays.copyOfRange(data, 96, 128));
        }
        return null;
    }

    //Used when the instruction is peeraddress, and returns the first 32 bytes of content. Currently only works with
    //IPv4 addresses.
    public byte[] getPeerAddress() {
        byte[] address = Arrays.copyOfRange(data, 96, 100);
        try {
            InetAddress peerAddress = InetAddress.getByAddress(address);
            System.out.println(peerAddress.toString());
        }catch(Exception e){
            e.printStackTrace();
        }

        return address;
    }

    //Turns this packet into a confirmation packet by copying the old hash to content, as used by getConfirmationHash().
    public void makeConfirmationPacket() {
        for (int i = 0; i < 32; i++) {
            data[i + 96] = data[i + 992];
        }
        swapSenderAndRecipient();
        setInstruction("confirmation");
        computeHash();
        System.out.println("makeConfirmationPacket resulted in valid packet: " + isValidPacket());
    }
    private void swapSenderAndRecipient(){
        byte[] sender = Arrays.copyOfRange(data, 0, 32);
        setRecipient(getSender());
        setSender(sender);

    }

    public boolean isValidPacket() {
        if (data.length == 1024) {
            return getHash().equals(cryptoManager.getSHA256AsHexString(Arrays.copyOfRange(data, 0, 992)));
        }
        return false;
    }

    public void computeHash() {
        byte[] hash = cryptoManager.getSHA256AsByteArray(Arrays.copyOfRange(data, 0, 992));
        for (int i = 0; i < 32; i++) {
            data[i + 992] = hash[i];
        }
        System.out.println("Is valid packet: " + isValidPacket());
    }
}