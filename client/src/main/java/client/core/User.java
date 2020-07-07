package client.core;

import server.core.Message;
import server.core.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class User {
    private final String name;
    private Socket socket;

    private final BlockingQueue<Message> outgoingMessageQueue;
    private final BlockingQueue<Message> incomingMessageQueue;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public User(String name) {
        this.name = name;
        socket = null;

        incomingMessageQueue = new ArrayBlockingQueue<>(1000);
        outgoingMessageQueue = new ArrayBlockingQueue<>(1000);
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

            this.addMessageToSend(new Message(MessageType.CLIENT_CONNECTED, this.getName(), ""));
        } catch (IOException e) {
            socket = null;
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null;
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

    public void disconnect() {
        if (socket != null) {
            try {
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasMessageToSend() {
        return !outgoingMessageQueue.isEmpty();
    }

    public Message getMessageToSend() {
        try {
            return outgoingMessageQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addMessageToSend(Message message) {
        outgoingMessageQueue.add(message);
    }

    public boolean hasUnreadMessage() {
        return !incomingMessageQueue.isEmpty();
    }

    public Message getUnreadMessage() {
        try {
            return incomingMessageQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addUnreadMessage(Message message) {
        incomingMessageQueue.add(message);
    }
}
