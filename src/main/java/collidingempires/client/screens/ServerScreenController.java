package collidingempires.client.screens;

import collidingempires.client.ClientMain;
import collidingempires.client.util.ControllerThread;
import collidingempires.client.util.GamesListCell;
import collidingempires.client.util.GamesListItem;
import collidingempires.client.util.HighscoreListCell;
import collidingempires.client.util.HighscoreListItem;
import collidingempires.client.util.LobbyListCell;
import collidingempires.client.util.LobbyListItem;
import collidingempires.server.net.ServerProtocol;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * Class managing the Nodes of layout_server.fxml
 */
public class ServerScreenController {

    public Label ipLabel;
    public Label playedListLabelName;
    public Label playedListLabelWinner;
    public Label playedListLabelStatus;
    public Label scoreListLabelTotal;
    public Label scoreListLabelName;
    public TextArea credits;
    public Label nameLabel;
    public Button btnUp;
    public Button btnDown;
    public Button btnLeft;
    public Button btnRight;
    public Button btnExit;
    public Button btnClick;
    public TabPane tabPane;
    private AudioClip click;
    private ChatView chatView;
    public ImageView editName;
    public Button createLobby;
    public TextField chatInput;
    public TextArea chatDisplay;
    public ImageView soundToggle;
    public TextField newLobbyNameField;
    private int buttonClicked = 0;
    public ListView<String> clientList = new ListView<>();
    public ListView<LobbyListItem> lobbyList = new ListView<>();
    public ListView<GamesListItem> playedGamesList = new ListView<>();
    public ListView<HighscoreListItem> leaderboardList = new ListView<>();

    /**
     * Sets the initial listItems and fonts for ui elements.
     */
    @FXML
    public void initialize() {
        if (ClientMain.getInstance().sound % 2 == 0) {
            soundToggle.setImage(new Image("images/server_screen/volumeOff.png"));
        } else {
            soundToggle.setImage(new Image("images/server_screen/volumeOn.png"));
        }
        ClientMain.getInstance().sound++;
        click = new AudioClip(getClass()
                .getResource("/sound/click.mp3").toExternalForm());
        setDropShadow(createLobby);
        chatView = new ChatView(chatDisplay, chatInput);
        chatInput.setOnAction(event -> chatView.handleInput());
        lobbyList.setItems(ClientMain.getInstance().lobbys);
        lobbyList.setCellFactory(listView -> new LobbyListCell());
        leaderboardList.setItems(ClientMain.getInstance().leaderboard);
        playedGamesList.setItems(ClientMain.getInstance().games);
        playedGamesList.setCellFactory(listView -> new GamesListCell());
        leaderboardList.setCellFactory(listView -> new HighscoreListCell());
        clientList.setItems(ClientMain.getInstance().clients);
        ipLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 48));
        nameLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 48));
        playedListLabelName.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 36));
        playedListLabelStatus.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 36));
        playedListLabelWinner.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 36));
        scoreListLabelName.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 36));
        scoreListLabelTotal.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 36));
        Label placeholder = new Label("Empty");
        placeholder.setTextFill(Color.web("#ffffff"));
        lobbyList.setPlaceholder(new StackPane(placeholder));
        Label placeholder1 = new Label("Empty");
        placeholder1.setTextFill(Color.web("#ffffff"));
        leaderboardList.setPlaceholder(new StackPane(placeholder1));
        Label placeholder2 = new Label("Empty");
        placeholder2.setTextFill(Color.web("#ffffff"));
        playedGamesList.setPlaceholder(new StackPane(placeholder2));
        clientList.setPlaceholder(new Label("Empty"));
        ClientMain.getInstance().stage.getScene().addEventHandler(
                KeyEvent.KEY_PRESSED, this::handleCustomKey);
        btnUp.setText(ControllerThread.getInstance().getKeyUp().name());
        btnDown.setText(ControllerThread.getInstance().getKeyDown().name());
        btnLeft.setText(ControllerThread.getInstance().getKeyLeft().name());
        btnRight.setText(ControllerThread.getInstance().getKeyRight().name());
        btnExit.setText(ControllerThread.getInstance().getKeyExit().name());
        btnClick.setText(ControllerThread.getInstance().getKeyClick().name());
        tabPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.UP
                    || event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.LEFT
                    || event.getCode() == KeyCode.RIGHT) {
                event.consume();
                handleCustomKey(event);
                ControllerThread.getInstance().handleKeyPress(event);
            }
        });
        credits.setText(
                "Icons:\n\thttps://material.io/resources/icons/\nGraphics:\n\t"
                        + "https://loudeyes.itch.io/battle"
                        + "-royale-fps-game-ui-asset-pack\n\t"
                        + "https://www.hiclipart.com/"
                        + "free-transparent-background-png-clipart-itlzw\n\t"
                        + "https://www.hiclipart.com/"
                        + "free-transparent-background-png-clipart-dxkaq\n\t"
                        + "https://craftpix.net/"
                        + "freebies/free-tropical-medieval-city-2d-tileset/\n\t"
                        + "https://craftpix.net/freebies/free-castle-2d-game-assets/\n\t"
                        + "https://craftpix.net/freebies/free-2d-rpg-desert-tileset/\n\t"
                        + "https://kenney.nl\n"
                        + "Sounds:\n\t"
                        + "http://soundbible.com/857-Swords-Clashing.html\n\t"
                        + "https://gamesounds.xyz/\n\t"
                        + "http://soundbible.com/\n"
                        + "Music:\n\t"
                        + "http://www.bensound.com");
    }

    /**
     * Set ip_Label to display ip, port and username of connection to server.
     */
    public void setUsername() {
        ipLabel.setText(" Name: " + ClientMain.getInstance().username);
        nameLabel.setText(" Name: " + ClientMain.getInstance().username);
    }

    /**
     * Called when an item on the lobby list was clicked.
     * Sends join request to server.
     */
    public void onLobbyListClick() {
        click.play();
        if (!lobbyList.getItems().isEmpty()
                && lobbyList.getSelectionModel().getSelectedItem() != null) {
            ClientMain.getInstance()
                    .send(new String[]{ServerProtocol.CWJL.name(),
                            lobbyList.getSelectionModel()
                                    .getSelectedItem().getName()});
        }
    }

    /**
     * Called when an item on the games list was clicked.
     * Sends spectate request to server.
     */
    public void onGamesListClick() {
        click.play();
        if (!playedGamesList.getItems().isEmpty() && playedGamesList.getSelectionModel()
                .getSelectedItem().getStatus().equals(GamesListItem.STATUS.ONGOING)) {
            ClientMain.getInstance()
                    .send(new String[]{ServerProtocol.CWTS.name(),
                            playedGamesList.getSelectionModel()
                                    .getSelectedItem().getName()});
        }
    }

    /**
     * Called when create new lobby was clicked.
     * Sends create request to server.
     */
    public void createClicked() {
        click.play();
        if (!newLobbyNameField.getText().equals("")) {
            ClientMain.getInstance().send(new String[]{ServerProtocol.CWCL.name(),
                    newLobbyNameField.getText()});
            newLobbyNameField.clear();
        }
        tabPane.requestFocus();
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
     * Handles click on edit username.
     * Opens input dialog and sends request to server if confirmed.
     */
    public void editNameClicked() {
        click.play();
        TextInputDialog dialog = new TextInputDialog("Tran");
        dialog.setTitle("Change username");
        dialog.setHeaderText("Enter your name:");
        dialog.getEditor().setText(System.getProperty("user.name"));
        dialog.initOwner(ClientMain.getInstance().stage);
        dialog.initStyle(StageStyle.UNDECORATED);
        ImageView logo = new ImageView(new Image("images/server_screen/edit.png"));
        logo.setFitWidth(30);
        logo.setPreserveRatio(true);
        dialog.setGraphic(logo);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/layout/dialog.css").toExternalForm());
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.equals(ClientMain.getInstance().username)) {
                ClientMain.getInstance().sendUsernameRequest(name);
            }
        });
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

    /**
     * Initiate keymap process for up navigation.
     */
    public void btnUp() {
        buttonClicked = 1;
        disableButtons();
    }

    /**
     * Initiate keymap process for down navigation.
     */
    public void btnDown() {
        buttonClicked = 2;
        disableButtons();
    }

    /**
     * Initiate keymap process for left navigation.
     */
    public void btnLeft() {
        buttonClicked = 3;
        disableButtons();
    }

    /**
     * Initiate keymap process for right navigation.
     */
    public void btnRight() {
        buttonClicked = 4;
        disableButtons();
    }

    /**
     * Initiate keymap process for exit navigation.
     */
    public void btnExit() {
        buttonClicked = 5;
        disableButtons();
    }

    /**
     * Initiate keymap process for clicks.
     */
    public void btnClick() {
        buttonClicked = 6;
        disableButtons();
    }

    /**
     * Disables all mapping buttons.
     */
    private void disableButtons() {
        btnUp.setDisable(true);
        btnDown.setDisable(true);
        btnLeft.setDisable(true);
        btnRight.setDisable(true);
        btnExit.setDisable(true);
        btnClick.setDisable(true);
    }

    /**
     * Finishes keymap process by assigning new key.
     *
     * @param keyEvent Current key pressed.
     */
    private void handleCustomKey(KeyEvent keyEvent) {
        switch (buttonClicked) {
            case 1:
                if (!ControllerThread.getInstance()
                        .getKeys().contains(keyEvent.getCode().toString())) {
                    ControllerThread.getInstance().setKeyUp(keyEvent.getCode());
                    btnUp.setText(ControllerThread.getInstance().getKeyUp().name());
                    ControllerThread.getInstance().addKeys();
                }
                break;
            case 2:
                if (!ControllerThread.getInstance()
                        .getKeys().contains(keyEvent.getCode().toString())) {
                    ControllerThread.getInstance().setKeyDown(keyEvent.getCode());
                    btnDown.setText(
                            ControllerThread.getInstance().getKeyDown().toString());
                    ControllerThread.getInstance().addKeys();
                }
                break;
            case 3:
                if (!ControllerThread.getInstance()
                        .getKeys().contains(keyEvent.getCode().toString())) {
                    ControllerThread.getInstance().setKeyLeft(keyEvent.getCode());
                    btnLeft.setText(
                            ControllerThread.getInstance().getKeyLeft().toString());
                    ControllerThread.getInstance().addKeys();
                }
                break;
            case 4:
                if (!ControllerThread.getInstance()
                        .getKeys().contains(keyEvent.getCode().toString())) {
                    ControllerThread.getInstance().setKeyRight(keyEvent.getCode());
                    btnRight.setText(
                            ControllerThread.getInstance().getKeyRight().toString());
                    ControllerThread.getInstance().addKeys();
                }
                break;
            case 5:
                if (!ControllerThread.getInstance()
                        .getKeys().contains(keyEvent.getCode().toString())) {
                    ControllerThread.getInstance().setKeyExit(keyEvent.getCode());
                    btnExit.setText(
                            ControllerThread.getInstance().getKeyExit().toString());
                    ControllerThread.getInstance().addKeys();
                }
                break;
            case 6:
                if (!ControllerThread.getInstance()
                        .getKeys().contains(keyEvent.getCode().toString())) {
                    ControllerThread.getInstance().setKeyClick(keyEvent.getCode());
                    btnClick.setText(
                            ControllerThread.getInstance().getKeyClick().toString());
                    ControllerThread.getInstance().addKeys();
                }
                break;
            default:
                break;
        }
        btnUp.setDisable(false);
        btnDown.setDisable(false);
        btnLeft.setDisable(false);
        btnRight.setDisable(false);
        btnExit.setDisable(false);
        btnClick.setDisable(false);
        buttonClicked = 0;
    }
}
