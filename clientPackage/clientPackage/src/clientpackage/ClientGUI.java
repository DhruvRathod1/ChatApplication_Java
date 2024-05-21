package clientPackage;

import java.net.*;
import java.io.*;

import javax.swing.*;

public class ClientGUI extends JFrame {

    private Socket socket;
    private BufferedReader br;
    private PrintWriter out;

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton closeButton;

    public ClientGUI() {
        try {
            System.out.println("Sending request to server");
            socket = new Socket("127.0.0.1", 8888);
            System.out.println("Connection done.");
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

            // Set up the GUI
            setTitle("Client");
            setSize(400, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            chatArea = new JTextArea();
            chatArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(chatArea);
            add(scrollPane);

            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
            inputField = new JTextField();
            inputPanel.add(inputField);
            sendButton = new JButton("Send");
            sendButton.addActionListener(e -> {
                sendMessage(inputField.getText());
                inputField.setText("");
            });
            inputPanel.add(sendButton);

            closeButton = new JButton("Close");
            closeButton.addActionListener(e -> {
                try {
                    socket.close();
                    dispose();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            inputPanel.add(closeButton);

            add(inputPanel, "South");

            setVisible(true);

            startReading();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startReading() {
        // thread-read data Multithreading
        Runnable r1 = () -> {
            System.out.println("Reader started..");
            try {
                while (!socket.isClosed()) {

                    String msg = br.readLine();
                    if (msg.equals("exit")) {
                        chatArea.append("Server terminated the chat\n");
                        socket.close();
                        break;
                    }
                    chatArea.append("Server : " + msg + "\n");
                }
            } catch (Exception e) {
                //e.printStackTrace();
                chatArea.append("Connection is Closed..Please Reconnect!\n");
            }

        };

        new Thread(r1).start();

    }

    public void sendMessage(String message) {
        out.println(message);
        out.flush();
        if (message.equals("exit")) {
            chatArea.append("Server terminated the chat\n");
            try {
                socket.close();
                dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        chatArea.append("Client : " + message + "\n");
    }

    public static void main(String[] args) {
        System.out.println("this is client...");
        new ClientGUI();
    }
}