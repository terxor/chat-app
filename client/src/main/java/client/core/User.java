package client.core;

import server.core.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class User {
    private final String name;
    private Socket socket;
    private final List<Message> messages;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public User(String name) {
        this.name = name;
        socket = null;
        messages = new ArrayList<>();
    }

    public void connect(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);

            /* Construct ObjectOutputStream before ObjectInputStream */
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            MessageSender messageSender = new MessageSender(this);
            MessageListener messageListener = new MessageListener(this);

            Thread messageSenderThread = new Thread(messageSender);
            Thread messageListenerThread = new Thread(messageListener);

            messageListenerThread.start();
            messageSenderThread.start();

        } catch (IOException e) {
            socket = null;
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }
}
