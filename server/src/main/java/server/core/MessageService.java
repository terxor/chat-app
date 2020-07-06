package server.core;

import java.io.IOException;
import java.util.List;

public class MessageService implements Runnable{

    private final Client client;
    private final List<Client> clientList;

    MessageService (Client client, List<Client> clientList) {
        this.client = client;
        this.clientList = clientList;
    }

    @Override
    public void run() {
        while (client.isConnected()) {
            try {
                Message message = client.getMessage();
                sendToAll(message);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        System.err.println("Someone dropped off");
    }

    private void sendToAll (Message message) {
        clientList.forEach(client1 -> client1.sendMessage(message));
    }
}
