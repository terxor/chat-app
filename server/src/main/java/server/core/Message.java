package server.core;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType messageType;
    private final String senderName;
    private final String messageBody;

    public Message (
            MessageType messageType,
            String senderName,
            String messageBody
    ) {
        this.messageType = messageType;
        this.senderName = senderName;
        this.messageBody = messageBody;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageBody() {
        return messageBody;
    }
}
