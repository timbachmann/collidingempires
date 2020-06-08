package collidingempires.client;

import collidingempires.client.screens.ConnectionScreenController;
import collidingempires.client.screens.GameScreenController;
import collidingempires.client.screens.LobbyScreenController;
import collidingempires.client.screens.ServerScreenController;
import collidingempires.client.net.ServerInputThread;
import collidingempires.client.util.ControllerThread;
import collidingempires.client.util.GamesListItem;
import collidingempires.client.util.HighscoreListItem;
import collidingempires.client.util.LobbyListItem;
import collidingempires.client.util.MembersListItem;
import collidingempires.server.net.Packager;
import collidingempires.server.net.ServerProtocol;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Main Client class to manage the server connection and the GUI classes.
 */
public class ClientMain extends Application {

    public String ip;
    public String port;
    public Stage stage;
    public Socket socket;
    public int sound = 0;
    public String username;
    private String player1;
    private String player2;
    private String player3;
    private String player4;
    private InputStream in;
    private OutputStream out;
    public String requestedName;
    private final double scalingX = 0.0;
    private final double scalingY = 0.0;
    public String lobbyName = null;
    public MediaPlayer mediaPlayer;
    public boolean terminate = false;
    public boolean tutorialWatched = false;
    public boolean spectating = false;
    private static ClientMain instance;
    private double scalingFactor = 1.0;
    private BufferedWriter bufferedWriter;
    public boolean alreadyLeftServer = false;
    public boolean fullscreen = false;
    public AnchorPane root = new AnchorPane();
    private ServerInputThread serverInputThread;
    public GameScreenController gameScreenController;
    public LobbyScreenController lobbyScreenController;
    public ServerScreenController serverScreenController;
    public ConnectionScreenController connectionScreenController;
    public ObservableList<GamesListItem>
            games = FXCollections.observableArrayList();
    public ObservableList<LobbyListItem>
            lobbys = FXCollections.observableArrayList();
    public ObservableList<String>
            clients = FXCollections.observableArrayList();
    public ObservableList<MembersListItem>
            members = FXCollections.observableArrayList();
    public ObservableList<HighscoreListItem>
            leaderboard = FXCollections.observableArrayList();

    /**
     * Returns instance of ClientMain.
     *
     * @return Instance of ClientMain.
     */
    public static ClientMain getInstance() {
        return instance;
    }

    /**
     * Displays the Application and sets screen displayed when opening.
     *
     * @param primaryStage Application stage.
     */
    @Override
    public void start(Stage primaryStage) {
        instance = this;
        // Get Parameters from Main.java
        Parameters params = getParameters();
        String[] args = params.getRaw().toArray(new String[3]);

        // create scene depending on resolution
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        double sceneHeight = width / 16 * 9;
        double sceneWidth = width;
        scalingFactor = width / 1920;
        if (scalingFactor > 1.0) {
            scalingFactor = height / 1080;
            sceneHeight = height;
            sceneWidth = height / 9 * 16;
        }
        Scene scene = new Scene(root, sceneWidth * 0.8, sceneHeight * 0.8);
        scalingFactor *= 0.8;

        // set stage scene
        stage = primaryStage;
        stage.setTitle("Colliding Empires");
        stage.setScene(scene);

        loadSplashscreen(args);

        // show stage and specify exit behaviour
        stage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.F11));
        stage.setOnCloseRequest(event -> {
            event.consume();
            handleESC();
        });
        stage.getIcons().add(new Image("images/logoSquare.png"));
        stage.setResizable(false);
        stage.show();
    }

    /* ******************************General***************************** */

    /**
     * Method to handle an ESC Key press. Displays an
     * alert dialog asking if the user wants to exit the game.
     */
    public void handleESC() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Colliding Empires");
        ImageView logo = new ImageView(new Image("images/logo.png"));
        logo.setFitWidth(70);
        logo.setPreserveRatio(true);
        alert.setGraphic(logo);
        alert.setHeaderText("Do you really want to exit the game?");
        alert.initOwner(stage);
        alert.initStyle(StageStyle.UNDECORATED);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/layout/dialog.css").toExternalForm());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.orElse(null) == ButtonType.OK) {
            alreadyLeftServer = true;
            try {
                if (socket != null) {
                    send(new String[]{ServerProtocol.CWLS.name()});
                }
                terminate = true;
                terminate();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.close();
        }
    }

    /**
     * Loading a splashscreen.
     *
     * @param args Arguments from Application startup.
     */
    private void loadSplashscreen(String[] args) {
        StackPane pane = new StackPane();
        MediaPlayer player = new MediaPlayer(new Media(getClass()
                .getResource("/images/intro.mp4").toExternalForm()));
        MediaView mediaView = new MediaView(player);
        pane.getChildren().add(mediaView);
        root.getChildren().setAll(pane);
        if (fullscreen) {
            root.getTransforms()
                    .add(new Scale(scalingFactor * 1.25,
                            scalingFactor * 1.25, scalingX, scalingY));
        } else {
            root.getTransforms()
                    .add(new Scale(scalingFactor, scalingFactor, scalingX, scalingY));
        }
        stage.getScene().setFill(Color.BLACK);
        player.play();
        new MediaPlayer(new Media(getClass().getResource("/sound/glitch.mp3")
                .toExternalForm())).play();

        //Set layout depending on arguments given
        player.setOnEndOfMedia(() -> {
            ControllerThread controllerThread = new ControllerThread();
            Thread thread = new Thread(controllerThread);
            thread.start();
            if (!args[0].equals("")
                    && !args[1].equals("") && !args[2].equals("")) {
                connect(args[0], args[1]);
                requestedName = args[2];
            } else if (!args[0].equals("") && !args[1].equals("")) {
                setScene("layout_connection.fxml");
                connectionScreenController.ipField.setText(args[0]);
                connectionScreenController.portField.setText(args[1]);
                connectionScreenController.usernameField
                        .setText(System.getProperty("user.name"));
            } else {
                setScene("layout_welcome.fxml");
            }
            playMusic();
        });
    }

    /**
     * Method to connect to server with Socket.
     *
     * @param newIp   Ip address passed to new Socket.
     * @param newPort Port passed to new Socket.
     */
    public void connect(String newIp, String newPort) {
        ip = newIp;
        port = newPort;
        try {
            // new socket with given ip and port
            socket = new Socket(ip, Integer.parseInt(port));
            in = socket.getInputStream();
            out = socket.getOutputStream();

            // create server reading thread
            serverInputThread = new ServerInputThread(in);
            Thread inputThread = new Thread(serverInputThread);
            inputThread.start();

            //bufferedWriter to send commands to server
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(out, StandardCharsets.UTF_8));

            terminate = false;
        } catch (IOException e) {
            System.out.println("Couldn't connect to server!!");
            setScene("layout_welcome.fxml");
        }
    }

    /**
     * Sets new scene root for main stage.
     *
     * @param fileName Filename of displayed layout file.
     */
    public void setScene(String fileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource(("/layout/" + fileName)));
            root = loader.load();
            //Scaling
            if (fullscreen) {
                root.getTransforms()
                        .add(new Scale(scalingFactor * 1.25,
                                scalingFactor * 1.25, scalingX, scalingY));
            } else {
                root.getTransforms()
                        .add(new Scale(scalingFactor, scalingFactor, scalingX, scalingY));
            }
            stage.getScene().setFill(Color.web("2C2B2E"));
            if ("layout_server.fxml".equals(fileName)) {
                serverScreenController = loader.getController();
                serverScreenController.initialize();
                serverScreenController.setUsername();
            } else if ("layout_lobby.fxml".equals(fileName)) {
                lobbyScreenController = loader.getController();
            } else if ("layout_game.fxml".equals(fileName)) {
                gameScreenController = loader.getController();
            } else if ("layout_connection.fxml".equals(fileName)) {
                connectionScreenController = loader.getController();
            }
            //Switch root of scene instead of setting new scene
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays background music on repeat.
     */
    public void playMusic() {
        Media media = new Media(getClass()
                .getResource("/sound/track.mp3").toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setVolume(0.1);
        mediaPlayer.setAutoPlay(true);
    }

    /**
     * Method to send messages to server.
     *
     * @param message String containing the message to send.
     */
    public void send(String[] message) {
        try {
            System.out.println("Send: " + Packager.pack(message));
            bufferedWriter.write(Packager.pack(message));
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ******************************ServerView***************************** */

    /**
     * Method called when a new chat message is received.
     *
     * @param sender  String containing information about the
     *                sender of the message.
     * @param message String containing the received message.
     * @param context String containing private, server or lobby.
     */
    public void receiveChat(String context, String sender, String message) {
        switch (context) {
            case "server":
                System.out.println(sender + "@Server: " + message);
                if (serverScreenController != null) {
                    serverScreenController.getChatView()
                            .appendText(sender + "@Server: " + message);
                }
                /*if (lobbyScreenController != null) {
                    lobbyScreenController.getChatView()
                            .appendText(sender + "@Server: " + message);
                }
                if (gameScreenController != null) {
                    gameScreenController.getChatView()
                            .appendText(sender + "@Server: " + message);
                }*/
                break;
            case "private":
                System.out.println("Message from " + sender + ": " + message);
                if (serverScreenController != null) {
                    serverScreenController.getChatView().appendText(
                            "Private Message from " + sender + ": " + message);
                }
                if (lobbyScreenController != null) {
                    lobbyScreenController.getChatView().appendText(
                            "Private Message from " + sender + ": " + message);
                }
                if (gameScreenController != null) {
                    gameScreenController.getChatView().appendText(
                            "Private Message from " + sender + ": " + message);
                }
                break;
            case "lobby":
                System.out.println("LobbyMessage from "
                        + sender + ": " + message);
                if (lobbyScreenController != null) {
                    lobbyScreenController.getChatView().appendText(
                            sender + "@Lobby: " + message);
                }
                if (gameScreenController != null) {
                    gameScreenController.getChatView().appendText(
                            sender + "@Lobby: " + message);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Method called when the text message status is updated.
     *
     * @param msg Message sent from server.
     */
    public void textMessageStatusUpdated(String msg) {
        if (msg.equals("success")) {
            System.out.println("Message sent!");
            if (serverScreenController != null) {
                if (!serverScreenController.getChatView().tmpSendTo.equals(username)
                        && !serverScreenController.getChatView().tmpSendTo.equals("")) {
                    serverScreenController.getChatView().appendText(
                            ClientMain.getInstance().username
                                    + " to " + serverScreenController
                                    .getChatView().tmpSendTo + ": "
                                    + serverScreenController.getChatView().tmpMessage);
                }
            }
            if (lobbyScreenController != null) {
                if (!lobbyScreenController.getChatView().tmpSendTo.equals(username)
                        && !lobbyScreenController.getChatView().tmpSendTo.equals("")) {
                    lobbyScreenController.getChatView().appendText(
                            ClientMain.getInstance().username
                                    + " to " + lobbyScreenController
                                    .getChatView().tmpSendTo + ": "
                                    + lobbyScreenController.getChatView().tmpMessage);
                }
            }
            if (gameScreenController != null) {
                if (!gameScreenController.getChatView().tmpSendTo.equals(username)
                        && !gameScreenController.getChatView().tmpSendTo.equals("")) {
                    gameScreenController.getChatView().appendText(
                            ClientMain.getInstance().username
                                    + " to " + gameScreenController
                                    .getChatView().tmpSendTo + ": "
                                    + gameScreenController.getChatView().tmpMessage);
                }
            }
        } else if (msg.equals("fail")) {
            System.out.println("Message not sent!");
        }
    }

    /**
     * Method to send a request to the server changing the username.
     *
     * @param newName String containing the new name to send.
     */
    public void sendUsernameRequest(String newName) {
        send(new String[]{ServerProtocol.CWCN.name(), newName});
    }

    /**
     * Method called when username was updated successfully.
     *
     * @param newName String containing the new username.
     * @param oldName String containing the old username.
     */
    public void updatedUsername(String oldName, String newName) {
        if (serverScreenController != null) {
            if (oldName.equals(username)) {
                username = newName;
                serverScreenController.setUsername();
            } else {
                System.out.println(oldName + " changed name to: " + newName);
            }
            serverScreenController.clientList.getItems().remove(oldName);
            serverScreenController.clientList.getItems().add(newName);
        }
    }

    /**
     * Method called when a client left the server.
     *
     * @param nickname Name of the client.
     */
    public void clientLeftServer(String nickname) {
        if (serverScreenController != null) {
            System.out.println(nickname + " left the server.");
            serverScreenController.clientList.getItems().remove(nickname);
        }
    }

    /**
     * Method called when a client joined the server.
     *
     * @param nickname Name of the client.
     */
    public void newClientOnServer(String nickname) {
        if (serverScreenController != null) {
            System.out.println(nickname + " joined the server.");
            serverScreenController.clientList.getItems().add(nickname);
        }
    }

    /**
     * Method called when a the connection was successful.
     *
     * @param nickname Initial name of the client.
     */
    public void connectionSuccessful(String nickname) {
        username = nickname;
        setScene("layout_server.fxml");
        sendUsernameRequest(requestedName);
    }

    /**
     * Method to terminate the connection to the server.
     * @throws IOException if socket cannot be closed
     */
    public void terminate() throws IOException {
        ControllerThread.getInstance().running = false;
        if (socket != null) {
            ServerInputThread.isRunning = false;
            System.out.println("terminating ...");
            in.close();
            out.close();
            socket.close();
        }
    }

    /**
     * Method called when a lobby changed either the player count or its status.
     *
     * @param name           Name of Lobby.
     * @param trueWhenInGame True when the game of the lobby started.
     * @param playerCount    Number of players in Lobby.
     */
    public void aLobbyChangedStatus(String name, String trueWhenInGame,
                                    String playerCount) {
        if (serverScreenController != null) {
            if (trueWhenInGame.equals("true")) {
                GamesListItem itemNew = new GamesListItem(name,
                        GamesListItem.STATUS.ONGOING, "");
                for (GamesListItem item : serverScreenController
                        .playedGamesList.getItems()) {
                    if (item.getName().equals(name)) {
                        serverScreenController.playedGamesList.getItems().remove(item);
                        serverScreenController.playedGamesList.getItems().add(itemNew);
                        break;
                    }
                }
            } else {
                LobbyListItem itemNew = new LobbyListItem(name,
                        Integer.parseInt(playerCount));
                for (LobbyListItem item : serverScreenController
                        .lobbyList.getItems()) {
                    if (item.getName().equals(name)) {
                        serverScreenController.lobbyList.getItems().remove(item);
                        serverScreenController.lobbyList.getItems().add(itemNew);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Method called when a new Lobby was created.
     *
     * @param lobbyName  Name of the Lobby.
     * @param numPlayers the number of players in the lobby
     */
    public void newLobby(String lobbyName, String numPlayers) {
        LobbyListItem listItem = new LobbyListItem(lobbyName,
                Integer.parseInt(numPlayers));
        serverScreenController.lobbyList.getItems().add(listItem);
        GamesListItem gamesListItem = new GamesListItem(lobbyName,
                GamesListItem.STATUS.OPEN, "");
        serverScreenController.playedGamesList.getItems().add(gamesListItem);
        serverScreenController.lobbyList.getItems().sort(
                (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    }

    /**
     * Method called when the creation of a Lobby failed.
     */
    public void failedToJoinLobby() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Failed to enter Lobby!\n"
                + "Please try again or create a new one.");
        alert.initOwner(stage);
        alert.initStyle(StageStyle.UNDECORATED);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/layout/dialog.css").toExternalForm());
        alert.showAndWait();
    }

    /**
     * Method called when a game finished and can be displayed as finished.
     *
     * @param name   Name of the Lobby.
     * @param winner Name of the winner.
     */
    public void newFinishedGame(String name, String winner) {
        if (serverScreenController != null) {
            for (GamesListItem item : serverScreenController.playedGamesList.getItems()) {
                if (item.getName().equals(name)
                        && !item.getStatus().equals(GamesListItem.STATUS.FINISHED)) {
                    item.setStatus(GamesListItem.STATUS.OPEN);
                }
            }
            GamesListItem listItem = new GamesListItem(name,
                    GamesListItem.STATUS.FINISHED, winner);
            serverScreenController.playedGamesList.getItems().add(listItem);
        }
    }

    /**
     * Method called when a new Lobby was deleted.
     *
     * @param name Name of the Lobby.
     */
    public void lobbyDeleted(String name) {
        if (serverScreenController != null) {
            for (LobbyListItem item : serverScreenController
                    .lobbyList.getItems()) {
                if (item.getName().equals(name)) {
                    serverScreenController.lobbyList.getItems().remove(item);
                    break;
                }
            }
            for (GamesListItem item : serverScreenController
                    .playedGamesList.getItems()) {
                if (item.getName().equals(name)
                        && !item.getStatus().equals(GamesListItem.STATUS.FINISHED)) {
                    serverScreenController.playedGamesList.getItems()
                            .remove(item);
                    break;
                }
            }
        }
    }

    /**
     * Method called when a new list entry in highscore list was made.
     *
     * @param name  Name of the player.
     * @param score Players score.
     */
    public void newHighscoreListEntry(String name, int score) {
        if (serverScreenController != null) {
            HighscoreListItem highscoreListItem = new HighscoreListItem(name, score);
            serverScreenController.leaderboardList.getItems()
                    .removeIf(item -> item.getName().equals(name));
            serverScreenController.leaderboardList.getItems().add(highscoreListItem);
            serverScreenController.leaderboardList.getItems().sort(
                    (o1, o2) -> {
                        Integer i = o2.getTime();
                        Integer j = o1.getTime();
                        return i.compareTo(j);
                    });
            serverScreenController.leaderboardList.refresh();
        }
    }

    /* *******************************LobbyView***************************** */

    /**
     * Method called when a new client joined the Lobby.
     *
     * @param name Name of the client.
     */
    public void clientJoinedLobby(String name) {
        if (lobbyScreenController != null) {
            for (MembersListItem item : lobbyScreenController.memberList.getItems()) {
                if (item.getName().equals(name)) {
                    return;
                }
            }
            MembersListItem listItem = new MembersListItem(name,
                    MembersListItem.STATUS.NOTREADY);
            lobbyScreenController.memberList.getItems().add(listItem);
        }
    }

    /**
     * Method called when a client is ready to play.
     *
     * @param name Name of the client.
     */
    public void clientChangedStatus(String name) {
        if (lobbyScreenController != null) {
            for (MembersListItem item : lobbyScreenController
                    .memberList.getItems()) {
                if (item.getName().equals(name)) {
                    if (item.getStatus().equals(MembersListItem.STATUS.NOTREADY)) {
                        MembersListItem listItem = new MembersListItem(name,
                                MembersListItem.STATUS.READY);
                        lobbyScreenController.memberList.getItems().remove(item);
                        lobbyScreenController.memberList.getItems().add(listItem);
                        if (item.getName().equals(username)) {
                            lobbyScreenController.readyImage
                                    .setImage(new Image(
                                            "images/lobby_screen/btn_notReady.png"));
                        }
                    } else if (item.getStatus().equals(MembersListItem.STATUS.READY)) {
                        MembersListItem listItem1 = new MembersListItem(name,
                                MembersListItem.STATUS.NOTREADY);
                        lobbyScreenController.memberList.getItems().remove(item);
                        lobbyScreenController.memberList.getItems().add(listItem1);
                        if (item.getName().equals(username)) {
                            lobbyScreenController.readyImage
                                    .setImage(new Image(
                                            "images/lobby_screen/btn_ready.png"));
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Method called when a client left the Lobby.
     *
     * @param name Name of the client.
     */
    public void clientLeftLobby(String name) {
        if (lobbyScreenController != null) {
            for (MembersListItem item : lobbyScreenController
                    .memberList.getItems()) {
                if (item.getName().equals(name)) {
                    lobbyScreenController.memberList.getItems().remove(item);
                    break;
                }
            }
        }
    }

    /**
     * Method called when you left a Lobby.
     */
    public void youLeftLobby() {
        setScene("layout_server.fxml");
        lobbyName = null;
    }

    /**
     * Method called when you entered a new Lobby.
     *
     * @param name Name of the Lobby.
     */
    public void youAreInLobby(String name) {
        lobbyName = name;
        setScene("layout_lobby.fxml");
        MembersListItem listItem = new MembersListItem(username,
                MembersListItem.STATUS.NOTREADY);
        lobbyScreenController.memberList.getItems().add(listItem);
    }

    /* ********************************InGame******************************* */

    /**
     * Method called when a new object should be displayed on map.
     *
     * @param field  Field id (0 - 179).
     * @param lvl    Object level (1 - 4).
     * @param object Object name (knight/tree/farm/tower).
     */
    public void newObjectOnMap(String field, String object, String lvl) {
        gameScreenController.placeObject(Integer.parseInt(field), object + lvl);

    }

    /**
     * Method called when its a players turn.
     *
     * @param name Name of player.
     */
    public void newPlayerOnTurn(String name) {
        if (gameScreenController != null) {
            if (name.equals(player1)) {
                if (spectating) {
                    gameScreenController.playerTurnLabel.setText(name);
                    gameScreenController.playerTurnLabel.setTextFill(Color.RED);
                    gameScreenController.resetAll();
                } else {
                    gameScreenController.playerTurnLabel.setText("Your Turn");
                    gameScreenController.playerTurnLabel.setTextFill(Color.RED);
                    gameScreenController.enableButtons();
                    gameScreenController.enableClickForOwnKnights();
                }
            } else if (name.equals(player2)) {
                gameScreenController.playerTurnLabel.setText(name);
                gameScreenController.playerTurnLabel.setTextFill(Color.BLUE);
                gameScreenController.resetAll();
            } else if (name.equals(player3)) {
                gameScreenController.playerTurnLabel.setText(name);
                gameScreenController.playerTurnLabel.setTextFill(Color.GREEN);
                gameScreenController.resetAll();
            } else if (name.equals(player4)) {
                gameScreenController.playerTurnLabel.setText(name);
                gameScreenController.playerTurnLabel.setTextFill(Color.YELLOW);
                gameScreenController.resetAll();
            }
            gameScreenController.countdownLabel.setVisible(false);
            gameScreenController.countdownIndicator.setVisible(false);
        }
    }

    /**
     * Method called when a turn was requested.
     *
     * @param fields Possible field ids (String of 180 characters either 0 or 1).
     */
    public void possibleFieldsRequest(String fields) {
        char[] mapValues = fields.toCharArray();
        int id = 0;
        for (int value : mapValues) {
            if (value == 49) {
                gameScreenController.markTile(id);
            }
            id++;
        }
        gameScreenController.mapPane.addEventHandler(
                MouseEvent.MOUSE_CLICKED, mouseEvent -> {
                    if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                        gameScreenController.handleRightClick();
                    }
                });
    }

    /**
     * Method called when a game starts.
     */
    public void gameStart() {
        player1 = username;
        int i = 2;
        for (MembersListItem item : members) {
            if (!item.getName().equals(username)) {
                if (i == 2) {
                    player2 = item.getName();
                } else if (i == 3) {
                    player3 = item.getName();
                } else if (i == 4) {
                    player4 = item.getName();
                }
                i++;
            }
        }
        setScene("layout_game.fxml");
    }

    /**
     * Method called when a game ends.
     *
     * @param winner Game winner.
     */
    public void gameEnded(String winner) {
        if (spectating) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game finished.");
        if (winner.equals(username)) {
            alert.setHeaderText("Congratulations, you won the game!");
            ImageView trophy = new ImageView(
                    new Image("images/game_screen/trophy.png"));
            trophy.setFitWidth(100);
            trophy.setPreserveRatio(true);
            alert.setGraphic(trophy);
        } else {
            alert.setHeaderText("Unfortunately you didn't win the game."
                    + "\n" + winner + " won the game!");
            ImageView trophy = new ImageView(new Image("images/game_screen/sad.png"));
            trophy.setFitWidth(100);
            trophy.setPreserveRatio(true);
            alert.setGraphic(trophy);
        }
        alert.initOwner(stage);
        alert.initStyle(StageStyle.UNDECORATED);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/layout/dialog.css").toExternalForm());
        alert.showAndWait();
        setScene("layout_lobby.fxml");
        for (int i = 0; i < lobbyScreenController.memberList.getItems().size(); i++) {
            lobbyScreenController.memberList.getItems().get(i)
                    .setStatus(MembersListItem.STATUS.NOTREADY);
        }

    }

    /**
     * Method called when a time is left on your turn.
     *
     * @param time Time left on your turn.
     */
    public void timeLeft(String time) {
        if (time.equals("3")) {
            gameScreenController.countdownIndicator.setVisible(true);
            gameScreenController.countdownLabel.setVisible(true);
        }
        gameScreenController.timeLabel.setText(time + " sec");
        gameScreenController.countdownLabel.setText(time);
    }

    /**
     * Method called when a new map is available.
     *
     * @param mapValues Contains values for tile placement (0 - 179).
     */
    public void newMap(String mapValues) {
        if (gameScreenController != null) {
            gameScreenController.createMap(mapValues);
        }
    }

    /**
     * Method called when the coin balance changed.
     *
     * @param balance New coin balance.
     */
    public void coinBalanceChanged(String balance) {
        if (gameScreenController != null) {
            gameScreenController.moneyLabel.setText(balance);
        }
    }

    /**
     * Method called to set the balance change.
     *
     * @param balanceChange Coin balance change.
     */
    public void setBalanceChange(String balanceChange) {
        if (gameScreenController != null) {
            if (!balanceChange.startsWith("-")) {
                gameScreenController.updateMoneyLabel.setText("+ " + balanceChange);
            } else {
                gameScreenController.updateMoneyLabel.setText(balanceChange);
            }
        }
    }

    /**
     * Method called when a tile changes owner.
     *
     * @param mapValue Contains value for tile coloring (0 - 179).
     * @param name     Name of new owner.
     */
    public void fieldChangedOwner(String mapValue, String name) {
        if (gameScreenController != null) {
            if (name.equals(player1)) {
                gameScreenController.colorTile(Integer.parseInt(mapValue), "red");
            } else if (name.equals(player2)) {
                gameScreenController.colorTile(Integer.parseInt(mapValue), "blue");
            } else if (name.equals(player3)) {
                gameScreenController.colorTile(Integer.parseInt(mapValue), "green");
            } else if (name.equals(player4)) {
                gameScreenController.colorTile(Integer.parseInt(mapValue), "yellow");
            } else {
                gameScreenController.colorTile(Integer.parseInt(mapValue), "default");
            }
        }
    }

    /**
     * Method called when a object was deleted.
     *
     * @param field Coordinates (0 - 179).
     */
    public void objectRemoved(String field) {
        gameScreenController.removeObject(Integer.parseInt(field));
    }

    /**
     * Method called when a Player is spectating.
     * Defines players and sets scene to game.
     */
    public void youAreSpectating() {
        spectating = true;
        int i = 1;
        for (MembersListItem item : members) {
            if (!item.getName().equals(username)) {
                if (i == 1) {
                    player1 = item.getName();
                } else if (i == 2) {
                    player2 = item.getName();
                } else if (i == 3) {
                    player3 = item.getName();
                } else if (i == 4) {
                    player4 = item.getName();
                }
                i++;
            }
        }
        setScene("layout_game.fxml");
    }
}