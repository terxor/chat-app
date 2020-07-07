package client.gui;

import javax.swing.*;

public class StartupGui {

    private String username;
    private boolean ready;

    public StartupGui() {
        ready = false;
    }

    public String getUsername() {
        while (!ready) showInputDialog();
        return username;
    }

    private void showInputDialog() {
        JPanel panel = new JPanel();
        JTextField textField = new JTextField(25);
        panel.add(new JLabel("Enter username:"));
        panel.add(textField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                GuiUtil.APPLICATION_TITLE,
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            String string = textField.getText();
            if (string.length() >= 3) {
                username = string;
                ready = true;
            } else {
                GuiUtil.showAlert("Username should have atleast 3 characters");
            }
        } else {
            System.exit(0);
        }
    }


}
