package client.core;

import server.core.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

public class MessageListener implements Runnable {

    private final List<Message> messages;
    private final ObjectInputStream inputStream;
    private final User user;

    public MessageListener (User user) {
        this.user = user;
        this.messages = user.getMessages();
        this.inputStream = user.getInputStream();
    }

    @Override
    public void run() {
        while (user.isConnected()) {
            try {
                Object object = inputStream.readObject();
                if (object instanceof Message) {
                    Message message = (Message)object;
                    System.out.println(message.getSenderName() + " says: " + message.getMessageBody());
                    messages.add(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.err.println("Server sent unknown object");
            }
        }
        System.err.println("Disconnected from server");
    }
}
