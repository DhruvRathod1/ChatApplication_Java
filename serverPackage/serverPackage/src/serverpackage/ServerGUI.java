package serverPackage;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ServerGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextArea chatArea;
    private JTextField chatField;
    private JButton sendButton, closeButton;

    private ServerSocket server;
    private Socket socket;
    private BufferedReader br;
    private PrintWriter out;
    private Connection connection;
    private PreparedStatement statement;

    public ServerGUI() {
        super("Chat Server");

        // Initialize components
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        chatField = new JTextField(20);

        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String content = chatField.getText();
                out.println(content);
                out.flush();

                if (content.equals("exit")) {
                    chatArea.append("Server terminated the chat\n");
                    closeConnection();
                } else {
                    chatArea.append("Server: " + content + "\n");
                    logChat("Server", content);
                }

                chatField.setText("");
            }
        });

        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                closeConnection();
            }
        });

        JPanel panel = new JPanel();
        panel.add(chatField);
        panel.add(sendButton);
        panel.add(closeButton);

        // Add components to the frame
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(panel, BorderLayout.SOUTH);

        // Start the server
        try {
            server = new ServerSocket(8888);
            chatArea.append("Server is ready to accept connection..\n");
            chatArea.append("Waiting....\n");
            socket = server.accept();

            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

            startReading();

            // Connect to MySQL database
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat", "root", "");
             statement = connection.prepareStatement("INSERT INTO chat_log1 (sender, message, timestamp) VALUES (?, ?, ?)");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set frame properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setVisible(true);
    }

    public void startReading() {
        // thread-read data Multithreading

        Runnable r1 = () -> {
            chatArea.append("Reader started..\n");
            try {
                while (!socket.isClosed()) {

                    String msg = br.readLine();
                    if (msg.equals("exit")) {
                        chatArea.append("Client terminated the chat\n");
                        closeConnection();
                        break;
                    }

                    chatArea.append("Client: " + msg + "\n");
                    logChat("Client", msg);
                }
            } catch (Exception e) {
                chatArea.append("Server terminated the chat\n");
                e.printStackTrace();
            }

        };

        new Thread(r1).start();

    }

 public void logChat(String sender, String message) {
    try {
        // Get the current timestamp
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // Insert the chat message into the database
        statement.setString(1, sender);
        statement.setString(2, message);
        statement.setTimestamp(3, timestamp);
        statement.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    public void closeConnection() {
        try {
            socket.close();
            server.close();
            br.close();
            out.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServerGUI();
    }
}