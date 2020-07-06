package client;

import client.core.User;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your name, server address, port");
        String name = scanner.next();
        String address = scanner.next();
        int port = scanner.nextInt();

        User user = new User(name);
        user.connect(address, port);
    }
}
