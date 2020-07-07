package client.gui;

import javax.swing.*;

public class GuiUtil {
    public static final String APPLICATION_TITLE = "Simple-Chat-Client";

    public static void showAlert(String message) {
        JOptionPane.showMessageDialog(
                null,
                message
        );
    }
}
