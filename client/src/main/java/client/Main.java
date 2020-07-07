package client;

import client.core.User;
import client.gui.ClientGui;
import client.gui.StartupGui;

public class Main {
    public static void main(String[] args) {
        /*
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your name, server address, port");
        String name = scanner.next();
        String address = scanner.next();
        int port = scanner.nextInt();

        User user = new User(name);
        user.connect(address, port);
         */
        StartupGui startupGUI = new StartupGui();
        String username = startupGUI.getUsername();

        User user = new User(username);
        ClientGui clientGUI = new ClientGui(user);
    }
}
