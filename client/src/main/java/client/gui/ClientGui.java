package client.gui;

import client.core.User;
import server.core.Message;
import server.core.MessageType;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class ClientGui {
    private final User user;

    /* GUI Components */
    private JFrame mainFrame;

    private JMenu menu;
    private JMenuBar menuBar;
    private JMenuItem connectMenuItem;
    private JMenuItem disconnectMenuItem;

    private JTextPane textPane;
    private StyledDocument document;
    private SimpleAttributeSet attributeSetBold = new SimpleAttributeSet();
    private SimpleAttributeSet attributeSetItalic = new SimpleAttributeSet();
    private SimpleAttributeSet styleLeftChat = new SimpleAttributeSet();
    private SimpleAttributeSet styleJoinedChat = new SimpleAttributeSet();

    private JScrollPane scrollPane;
    private JTextField sendTextField;
    private JButton sendButton;
    private JPanel lowerPanel;
    private JLabel nameLabel;

    /* To show received messages on text pane */
    Thread messageReaderThread;

    public ClientGui(User user) {
        this.user = user;

        /* Main frame setup */
        mainFrame = new JFrame(GuiUtil.APPLICATION_TITLE);
        mainFrame.setSize(600, 400);
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                user.disconnect();
                System.exit(0);
            }
        });

        /* Lower panel setup */
        lowerPanel = new JPanel();
        sendTextField = new JTextField(30);
        sendButton = new JButton("Send");
        nameLabel = new JLabel("I am: " + user.getName());
        lowerPanel.add(nameLabel);
        lowerPanel.add(sendTextField);
        lowerPanel.add(sendButton);
        sendButton.addActionListener(new SendListener());
        sendTextField.addKeyListener(new SendKeyListener());

        /* Menu bar setup */
        menuBar = new JMenuBar();
        menu = new JMenu("Options");
        connectMenuItem = new JMenuItem("Connect");
        disconnectMenuItem = new JMenuItem("Disconnect");
        connectMenuItem.addActionListener(new ConnectListener());
        disconnectMenuItem.addActionListener(new DisconnectListener());
        menuBar.add(menu);
        menu.add(connectMenuItem);
        menu.add(disconnectMenuItem);

        /* Text pane settings */
        textPane = new JTextPane();
        textPane.setEditable(false);
        document = textPane.getStyledDocument();
        StyleConstants.setBold(attributeSetBold, true);
        StyleConstants.setItalic(attributeSetItalic, true);

        Color green = new Color(0, 255, 8);
        StyleConstants.setItalic(styleJoinedChat, true);
        StyleConstants.setForeground(styleJoinedChat, green);

        Color red = new Color(255, 0, 0);
        StyleConstants.setItalic(styleLeftChat, true);
        StyleConstants.setForeground(styleLeftChat, red);

        DefaultCaret defaultCaret = (DefaultCaret)textPane.getCaret();
        defaultCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        scrollPane = new JScrollPane(textPane);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        );

        /* Position the components into main frame */
        mainFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        mainFrame.getContentPane().add(menuBar, BorderLayout.NORTH);
        mainFrame.getContentPane().add(lowerPanel, BorderLayout.SOUTH);
        mainFrame.setVisible(true);

        setDisconnectedState();

        /* Thread setup */
        messageReaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (user.isConnected() && user.hasUnreadMessage()) {
                        Message message = user.getUnreadMessage();
                        try {
                            /* First insert newline */
                            document.insertString(document.getLength(), "\n", null);

                            switch (message.getMessageType()) {
                                case TEXT: {
                                    document.insertString(
                                            document.getLength(),
                                            message.getSenderName() + " : ",
                                            attributeSetBold
                                    );
                                    document.insertString(
                                            document.getLength(),
                                            message.getMessageBody(),
                                            null
                                    );
                                    break;
                                }
                                case CLIENT_CONNECTED: {
                                    document.insertString(
                                            document.getLength(),
                                            message.getSenderName() + " just connected",
                                            styleJoinedChat
                                    );
                                    break;
                                }
                                case CLIENT_DISCONNECTED: {
                                    document.insertString(
                                            document.getLength(),
                                            message.getSenderName() + " disconnected",
                                            styleLeftChat
                                    );
                                    break;
                                }
                            }
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        messageReaderThread.start();
    }

    private void setDisconnectedState() {
        connectMenuItem.setEnabled(true);
        disconnectMenuItem.setEnabled(false);
        sendButton.setEnabled(false);
        sendTextField.setEnabled(false);
        textPane.setText("Connect to a server to start chat");
    }

    private void setConnectedState() {
        connectMenuItem.setEnabled(false);
        disconnectMenuItem.setEnabled(true);
        sendButton.setEnabled(true);
        sendTextField.setEnabled(true);
        textPane.setText("");
    }

    class ConnectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showConnectDialog();
        }
    }

    class DisconnectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            user.disconnect();
            setDisconnectedState();
        }
    }

    class SendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessage();
        }
    }

    class SendKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            // Nothing for now
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // Nothing for now
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                sendMessage();
            }
        }
    }

    private void sendMessage() {
        user.addMessageToSend(new Message(
                MessageType.TEXT,
                user.getName(),
                sendTextField.getText()
        ));
        sendTextField.setText("");
        sendTextField.requestFocus();
    }

    private void showConnectDialog() {
        JTextField addressTextField = new JTextField(20);
        JTextField portTextField = new JTextField(20);
        GridLayout grid = new GridLayout(2, 2);

        JPanel panel = new JPanel(grid);
        panel.add(new JLabel("Server address"));
        panel.add(addressTextField);
        panel.add(new JLabel("Server port"));
        panel.add(portTextField);

        int response = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Enter connection parameters",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (response == JOptionPane.OK_OPTION) {
            int port;
            try {
                port = Integer.parseInt(portTextField.getText());
            } catch (NumberFormatException e) {
                GuiUtil.showAlert("Port must be an integer");
                showConnectDialog();
                return;
            }

            user.connect(addressTextField.getText(), port);
            if (!user.isConnected()) {
                GuiUtil.showAlert("Could not connect to server");
                showConnectDialog();
            } else {
                setConnectedState();
            }
        }
    }
}
