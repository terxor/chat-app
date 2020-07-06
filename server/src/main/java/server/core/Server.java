package server.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
    private ServerSocket serverSocket;
    private final List<Client> clients;
    private List<Thread> threads;

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        clients = new ArrayList<>();
        threads = new ArrayList<>();
    }

    public void start() {
        while (!serverSocket.isClosed()) {
            try {
                int aliveCount = threads.stream().mapToInt(thread -> thread.isAlive() ? 1 : 0).sum();
                System.err.println("Alive threads:" + aliveCount);

                System.err.println("Listening for incoming connections...");
                Socket socket = serverSocket.accept();
                System.err.println("Accepted incoming connection: " + socket.getInetAddress());

                /* Construct ObjectOutputStream before ObjectInputStream */
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                Client newClient = new Client(socket, outputStream, inputStream);
                Thread thread = new Thread(new MessageService(newClient, clients));

                synchronized (clients) {
                    List<Client> activeClients = clients.stream().filter(Client::isConnected).collect(Collectors.toList());
                    clients.clear();
                    clients.addAll(activeClients);
                    clients.add(newClient);
                }

                threads = threads.stream().filter(Thread::isAlive).collect(Collectors.toList());
                threads.add(thread);
                thread.start();

                newClient.sendMessage(new Message(MessageType.TEXT, "SERVER", "Welcome"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
