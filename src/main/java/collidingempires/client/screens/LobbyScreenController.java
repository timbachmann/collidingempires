package collidingempires.client.screens;

import collidingempires.client.ClientMain;
import collidingempires.client.util.ControllerThread;
import collidingempires.client.util.MembersListCell;
import collidingempires.client.util.MembersListItem;
import collidingempires.server.net.ServerProtocol;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;

/**
 * Class managing the Nodes of layout_lobby.fxml
 */
public class LobbyScreenController {

    public ListView<MembersListItem> memberList;
    public Label lobbyNameLabel;
    public Label ipLabel;
    public Label portLabel;
    public TextArea chatDisplay;
    public TextField chatInput;
    public Button btnBack;
    public Button btnReady;
    private ChatView chatView;
    public ImageView readyImage;
    public ImageView soundToggle;
    public GridPane grid;
    private AudioClip click;

    /**
     * Sets the initial listItems and fonts for ui elements.
     */
    @FXML
    public void initialize() {
        setDropShadow(btnBack);
        setDropShadow(btnReady);
        if (ClientMain.getInstance().sound % 2 != 0) {
            soundToggle.setImage(new Image("images/server_screen/volumeOff.png"));
        } else {
            soundToggle.setImage(new Image("images/server_screen/volumeOn.png"));
        }
        click = new AudioClip(getClass()
                .getResource("/sound/click.mp3").toExternalForm());
        ipLabel.setText(ClientMain.getInstance().ip);
        portLabel.setText(ClientMain.getInstance().port);
        lobbyNameLabel.setText(ClientMain.getInstance().lobbyName);
        memberList.setItems(ClientMain.getInstance().members);
        memberList.setCellFactory(listView -> new MembersListCell());
        chatView = new ChatView(chatDisplay, chatInput);
        chatInput.setOnAction(event -> chatView.handleInput());
        lobbyNameLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 64));
        ipLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 36));
        portLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 36));
        grid.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == ControllerThread.getInstance().getKeyClick()) {
                event.consume();
                ControllerThread.getInstance().handleKeyPress(event);
            }
        });
    }

    /**
     * Called when back in lobbyView was clicked.
     * Sends leave lobby request to server.
     */
    @FXML
    public void onBack() {
        click.play();
        System.out.println("Left Lobby.");
        ClientMain.getInstance().send(new String[]{ServerProtocol.CWLL.name()});
        ClientMain.getInstance().members.clear();
    }

    /**
     * Called when ready to play was clicked.
     * Sends toggle ready request to server.
     */
    @FXML
    public void onReadyClicked() {
        click.play();
        System.out.println("Clicked Ready.");
        ClientMain.getInstance().send(new String[]{ServerProtocol.CWTR.name()});
    }

    /**
     * Getter for the chat view.
     *
     * @return chatView
     */
    public ChatView getChatView() {
        return chatView;
    }

    /**
     * Set DropShadow Effect for given Button.
     *
     * @param button Button you want to apply the effect.
     */
    public void setDropShadow(Button button) {
        button.setOnMousePressed(event -> button.setEffect(new DropShadow()));
        button.setOnMouseReleased(event -> button.setEffect(null));
    }

    /**
     * Stops or plays music depending on current mediaplayer state.
     */
    public void toggleSound() {
        if (ClientMain.getInstance().sound % 2 == 0) {
            ClientMain.getInstance().mediaPlayer.stop();
            soundToggle.setImage(new Image("images/server_screen/volumeOff.png"));
        } else {
            ClientMain.getInstance().mediaPlayer.play();
            soundToggle.setImage(new Image("images/server_screen/volumeOn.png"));
        }
        ClientMain.getInstance().sound++;
    }
}
