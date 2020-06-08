package collidingempires.client.screens;

import collidingempires.client.ClientMain;
import collidingempires.server.net.ServerProtocol;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;

/**
 * Class managing the chat windows.
 */
public class ChatView {

    private final TextArea chatDisplay;
    private final TextField chatInput;
    public String tmpMessage;
    public String tmpSendTo = "";


    /**
     * Constructor to initialize Variables and setting Fonts.
     * @param chatDisplay TextArea used.
     * @param chatInput TextField used.
     */
    public ChatView(TextArea chatDisplay, TextField chatInput) {
        this.chatDisplay = chatDisplay;
        this.chatInput = chatInput;
        this.chatDisplay.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 28));
        this.chatInput.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 28));
    }

    /**
     * Called to respond to entered text or commands.
     * Sends commands to server.
     */
    public void handleInput() {
        String line = chatInput.getText();
        if (!line.isEmpty()) {
            if (line.startsWith("@") && line.contains(":")) {
                String[] message = line.split(":", 2);
                ClientMain.getInstance().send(new String[]{ServerProtocol.CWST.name(),
                        message[0], message[1]});
                tmpSendTo = message[0].substring(1);
                tmpMessage = message[1];
            } else if (ClientMain.getInstance().lobbyName == null) {
                ClientMain.getInstance().send(new String[]{ServerProtocol.CWST.name(),
                        "server", line});
            } else {
                ClientMain.getInstance().send(new String[]{ServerProtocol.CWST.name(),
                        "lobby", line});
            }
        }
        chatInput.clear();
    }

    /**
     * Displays text in TextArea textDisplay.
     * Called when a chat is received from server.
     *
     * @param text text to display.
     */
    public void appendText(String text) {
        if (chatDisplay.getText().trim().isEmpty()) {
            chatDisplay.appendText(text);
        } else {
            chatDisplay.appendText("\n" + text);
        }
    }
}
