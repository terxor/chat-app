package server.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
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

                /* First know the identity of client */
                String newClientName = getNewClientName(newClient);

                if (newClientName == null) {
                    socket.close();
                } else {
                    newClient.setName(newClientName);
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

                    Message message = new Message(MessageType.CLIENT_CONNECTED, newClientName, "");
                    clients.forEach(client -> client.sendMessage(message));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Client must send the initialization message under 1000 ms or the connection will be closed */
    private String getNewClientName(Client client) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Message message = client.getMessage();
                if (message.getMessageType() != MessageType.CLIENT_CONNECTED) return null;
                return message.getSenderName();
            }
        });
        executorService.shutdown();

        try {
            return future.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }
}
