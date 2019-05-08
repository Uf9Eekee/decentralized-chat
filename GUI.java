import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.*;

public class GUI extends JFrame implements ActionListener {
    private JPanel mainPanel = new JPanel();
    private JTextField inputArea = new JTextField("test");
    private JTextArea chatArea = new JTextArea(40, 10);
    private JSeparator separator = new JSeparator();
    private JScrollPane chatScrollPane = new JScrollPane();

    private PipeBundle pipeBundle1;
    private PipeBundle pipeBundle2;
    private PipeBundle pipeBundle3;

    private PipedOutputStream pipeOut1;

    private MessageListener messageListener;



    public GUI(PipeBundle pipeBundle1, PipeBundle pipeBundle2, PipeBundle pipeBundle3){
        super("DecentralizedChat");

        this.pipeBundle1 = pipeBundle1;
        this.pipeBundle2 = pipeBundle2;
        this.pipeBundle3 = pipeBundle3;

        pipeOut1 = (PipedOutputStream) pipeBundle1.getOutputPipe();

        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(separator, BorderLayout.PAGE_END);
        //mainPanel.add(chatArea, BorderLayout.PAGE_START);
        chatScrollPane.setViewportView(chatArea);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(inputArea, BorderLayout.PAGE_END);
        inputArea.addActionListener(this);
        add(mainPanel);
        setSize(800, 610);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        initialize();
    }

    public void actionPerformed(ActionEvent a){
        if(a.getSource() == inputArea) {
            try {
                byte[] input = new byte[1024];
                byte[] temp = inputArea.getText().getBytes("UTF-8");
                for (int i = 0; i < temp.length; i++) {
                    input[i] = temp[i];
                }
                inputArea.setText("");
                pipeOut1.write(input, 0, input.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private void initialize(){
        messageListener = new MessageListener(chatArea, pipeBundle1, pipeBundle2, pipeBundle3);
        messageListener.doInBackground();
        }

}


class MessageListener extends SwingWorker<Boolean, Void> {
    private JTextArea chatArea;

    private PipedOutputStream pipeOut1;
    private PipedInputStream pipeIn2;
    private PipedInputStream pipeIn3;

    public MessageListener(JTextArea chatArea, PipeBundle pipeBundle1, PipeBundle pipeBundle2, PipeBundle pipeBundle3){

        pipeOut1 = (PipedOutputStream) pipeBundle1.getOutputPipe();
        pipeIn2 = (PipedInputStream) pipeBundle2.getInputPipe();
        pipeIn3 = (PipedInputStream) pipeBundle3.getInputPipe();

        this.chatArea = chatArea;
    }

    @Override
    public Boolean doInBackground(){

        try {
            while(true) {
                byte[] messageArray = new byte[1024];
                pipeIn3.read(messageArray, 0, 1024);
                String message = new String(messageArray, "UTF-8");

                System.out.println(message);
                SwingUtilities.invokeLater(new MessageWriter(message, chatArea));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
class MessageWriter implements Runnable{
    private String message;
    private JTextArea chatArea;
    public MessageWriter(String s, JTextArea chatArea){
        message = s;
        this.chatArea = chatArea;
    }
    public void run(){
        chatArea.append(message);
        chatArea.append("\n");
    }


}