package server;

import server.core.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(5005);
        server.start();
    }
}
