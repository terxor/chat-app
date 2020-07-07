package client.core;

import server.core.Message;
import server.core.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class MessageSender implements Runnable {
    private final User user;
    private final ObjectOutputStream outputStream;

    public MessageSender(User user) {
        this.user = user;
        this.outputStream = user.getOutputStream();
    }

    @Override
    public void run() {
        while (user.isConnected()) {
            if (user.hasMessageToSend()) {
                sendMessage(user.getMessageToSend());
            }
        }
    }

    private void sendMessage(Message message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
