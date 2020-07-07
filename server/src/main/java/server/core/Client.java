package server.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Represents a client at server side
 * Has methods for sending & receiving messages
 */
public class Client {
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Socket socket;
    private String name;

    Client (Socket socket, ObjectOutputStream outputStream, ObjectInputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.socket = socket;
    }

    public void sendMessage (Message m) {
        try {
            outputStream.writeObject(m);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This function is blocking, use carefully
    // Returns null if there is the object read is not a message (or class not found)
    public Message getMessage() throws IOException {
        try {
            Object object = inputStream.readObject();
            return (object instanceof Message) ? (Message)object : null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
