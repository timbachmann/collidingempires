package collidingempires.client.screens;

import collidingempires.client.ClientMain;
import collidingempires.client.util.ControllerThread;
import collidingempires.server.net.ServerProtocol;
import javafx.animation.FadeTransition;
import javafx.animation.PathTransition;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class managing the Nodes of layout_game.fxml
 */
public class GameScreenController {

    public GridPane mainPane;
    public TextArea chatDisplay;
    public TextField chatInput;
    public Button btnLeave;
    public AnchorPane mapPane;
    private ChatView chatView;
    public Label moneyLabel;
    public Label updateMoneyLabel;
    public Label timeLabel;
    public Label playerTurnLabel;
    public Button btnNext;
    public HBox shopKnight;
    public HBox shopTower;
    public HBox shopFarm;
    public Label shopLabelKnight;
    public Label shopLabelTower;
    public Label shopLabelFarm;
    public String itemToPlace = "";
    public String itemToMove = "";
    private double[] oldKnightPos = null;
    public ArrayList<Integer> alreadyMoved = new ArrayList<>();
    private ImageView tutorial;
    public AnchorPane anchorPane;
    public ImageView soundToggle;
    public ImageView clickInstructions;
    public ProgressIndicator countdownIndicator;
    public Label countdownLabel;
    public Label spectatingLabel;
    private AudioClip click;
    private AudioClip buy;
    private AudioClip placeKnight;
    private AudioClip fight;

    /**
     * Sets the initial listItems and fonts for ui elements.
     */
    @FXML
    public void initialize() {
        shopFarm.setOnMouseClicked(Event::consume);
        shopKnight.setOnMouseClicked(Event::consume);
        shopTower.setOnMouseClicked(Event::consume);
        btnNext.setDisable(true);
        setDropShadow(btnLeave);
        setDropShadow(btnNext);
        setBorderOnClick(shopFarm);
        setBorderOnClick(shopKnight);
        setBorderOnClick(shopTower);
        if (ClientMain.getInstance().sound % 2 != 0) {
            soundToggle.setImage(new Image("images/server_screen/volumeOff.png"));
        } else {
            soundToggle.setImage(new Image("images/server_screen/volumeOn.png"));
        }
        click = new AudioClip(getClass()
                .getResource("/sound/click.mp3").toExternalForm());
        buy = new AudioClip(getClass()
                .getResource("/sound/buyItem.wav").toExternalForm());
        placeKnight = new AudioClip(getClass()
                .getResource("/sound/placeKnight.wav").toExternalForm());
        fight = new AudioClip(getClass()
                .getResource("/sound/fight.mp3").toExternalForm());
        chatView = new ChatView(chatDisplay, chatInput);
        chatInput.setOnAction(event -> chatView.handleInput());
        timeLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/builtTitling.ttf"), 36));
        moneyLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/builtTitling.ttf"), 36));
        updateMoneyLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/builtTitling.ttf"), 36));
        playerTurnLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/build.ttf"), 36));
        shopLabelFarm.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/builtTitling.ttf"), 36));
        shopLabelKnight.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/builtTitling.ttf"), 36));
        shopLabelTower.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/builtTitling.ttf"), 36));
        shopLabelKnight.setTextFill(Color.WHITE);
        shopLabelTower.setTextFill(Color.WHITE);
        shopLabelFarm.setTextFill(Color.WHITE);
        tutorial = new ImageView(new Image("images/game_screen/tutorial.png"));
        tutorial.setFitHeight(1080);
        tutorial.setFitWidth(1920);
        tutorial.setOnMouseClicked(event -> hideTutorial());
        countdownLabel.setFont(Font.loadFont(getClass()
                .getResourceAsStream("/font/builtTitling.ttf"), 54));
        if (ClientMain.getInstance().spectating) {
            moneyLabel.setVisible(false);
            updateMoneyLabel.setVisible(false);
            spectatingLabel.setVisible(true);
        } else {
            moneyLabel.setVisible(true);
            updateMoneyLabel.setVisible(true);
            spectatingLabel.setVisible(false);
        }
        mainPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == ControllerThread.getInstance().getKeyClick()) {
                event.consume();
                ControllerThread.getInstance().handleKeyPress(event);
            }
        });
        anchorPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == ControllerThread.getInstance().getKeyClick()) {
                event.consume();
                ControllerThread.getInstance().handleKeyPress(event);
            }
        });
    }

    /**
     * Called when back in gameView was clicked.
     * Sends leave lobby request to server if OK.
     */
    public void onBack() {
        click.play();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Do you really want to return to server and exit lobby?");
        alert.initOwner(ClientMain.getInstance().stage);
        alert.initStyle(StageStyle.UNDECORATED);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/layout/dialog.css").toExternalForm());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.orElse(null) == ButtonType.OK) {
            System.out.println("Left Game and Lobby.");
            ClientMain.getInstance().send(new String[]{ServerProtocol.CWLL.name()});
            ClientMain.getInstance().members.clear();
        }
    }

    /**
     * Creates Tile Layouts on mapPane to represent the map.
     *
     * @param mapValues Fields playable (0 - 179).
     */
    public void createMap(String mapValues) {
        List<Integer> emptyFields = new ArrayList<>();
        char[] mapArray = mapValues.toCharArray();
        int id = 0;
        double size = 50;
        double v = Math.sqrt(3) / 2.0;
        for (double y = 30; y < 890; y += size * Math.sqrt(3)) {
            for (double x = 0, dy = y; x < 1300; x += (3.0 / 2.0) * size) {
                if (mapArray[id] == '1') {
                    Tile hex = new Tile(id);
                    hex.setLayoutX(x);
                    hex.setLayoutY(dy);
                    mapPane.getChildren().add(hex);
                } else {
                    StackPane pane = new StackPane();
                    pane.setLayoutX(x);
                    pane.setLayoutY(dy);
                    emptyFields.add(id);
                    mapPane.getChildren().add(pane);
                }
                dy = dy == y ? dy + size * v : y;
                id++;
            }
        }
        if (!ClientMain.getInstance().tutorialWatched
                && !ClientMain.getInstance().spectating) {
            anchorPane.getChildren().add(tutorial);
        }

        for (Integer i : emptyFields) {
            if (getNeighborCount(i) == 6) {
                ImageView hex = new ImageView(
                        new Image("images/game_screen/tileWater.png"));
                hex.prefHeight(87);
                hex.prefWidth(100);
                ((StackPane) mapPane.getChildren().get(i)).getChildren().add(hex);
            }
        }
    }

    public int getNeighborCount(int mapValue) {
        List<Integer> neighbor = new ArrayList<>();
        if (mapValue % 2 == 1) {
            neighbor.add(mapValue - 18);
            neighbor.add(mapValue - 1);
            neighbor.add(mapValue + 1);
            neighbor.add(mapValue + 17);
            neighbor.add(mapValue + 18);
            neighbor.add(mapValue + 19);
        } else {
            neighbor.add(mapValue - 18);
            neighbor.add(mapValue - 19);
            neighbor.add(mapValue - 17);
            neighbor.add(mapValue - 1);
            neighbor.add(mapValue + 18);
            neighbor.add(mapValue + 1);
        }
        int neighbors = 0;
        for (Integer i : neighbor) {
            if (i >= 0 && i <= 179) {
                if (mapPane.getChildren().get(i) instanceof Tile) {
                    neighbors++;
                }
            }
        }
        return neighbors;
    }

    /**
     * Called when right mouse button is clicked.
     * Cancels placement and resets view to respond to new actions.
     */
    public void handleRightClick() {
        itemToPlace = "";
        itemToMove = "";
        unmarkTiles();
        enableButtons();
        clickInstructions.setVisible(false);
        ClientMain.getInstance().stage.getScene().setCursor(Cursor.DEFAULT);
        enableClickForOwnKnights();
    }

    /**
     * Sets new color of given tile.
     *
     * @param color    New Color (red, green, blue or yellow).
     * @param mapValue Id of field  (0 - 179).
     */
    public void colorTile(int mapValue, String color) {
        Tile tile = (Tile) mapPane.getChildren().get(mapValue);
        if (!tile.currentColor.equals("default") && !tile.currentColor.equals("red")
                && !color.equals("default")) {
            fight.play();
        }
        tile.setColor(color);
    }

    /**
     * Shows new object of given tile.
     *
     * @param object   New object ((knight/tree/farm/tower) + Level(1 - 4))
     * @param mapValue Id of field  (0 - 179).
     */
    public void placeObject(int mapValue, String object) {
        Tile tile = (Tile) mapPane.getChildren().get(mapValue);
        String color = object.equals("tree1") ? "" : tile.currentColor.substring(0, 1)
                .toUpperCase() + tile.currentColor.substring(1);
        if (oldKnightPos != null && mapValue == oldKnightPos[2]) {
            oldKnightPos = null;
        }
        if (oldKnightPos != null && object.contains("knight")) {
            Tile oldTile = (Tile) mapPane.getChildren().get((int) oldKnightPos[2]);
            ImageView tempKnight = new ImageView(new Image("images/game_screen/knight"
                    + oldTile.getLevel()
                    + oldTile.currentColor.substring(0, 1).toUpperCase()
                    + oldTile.currentColor.substring(1) + ".png"));
            tempKnight.setLayoutX(50);
            tempKnight.setLayoutY(44);
            tempKnight.setPreserveRatio(true);
            tempKnight.setFitWidth(100);
            mapPane.getChildren().add(tempKnight);
            oldTile.object.setVisible(false);
            final Path path = new Path();
            path.getElements().add(new MoveTo(oldKnightPos[0], oldKnightPos[1]));
            path.getElements().add(new LineTo(tile.getLayoutX(), tile.getLayoutY()));
            path.setOpacity(0.0);

            mapPane.getChildren().add(path);
            final PathTransition pathTransition = new PathTransition();

            pathTransition.setDuration(Duration.seconds(0.8));
            pathTransition.setPath(path);
            pathTransition.setNode(tempKnight);
            pathTransition.setCycleCount(1);
            pathTransition.play();
            pathTransition.setOnFinished(event -> {
                tile.setObject(object + color);
                mapPane.getChildren().remove(tempKnight);
                mapPane.getChildren().remove(path);
                oldKnightPos = null;
            });
        } else {
            tile.setObject(object + color);
        }
        if (tile.currentColor.equals("red") && !ClientMain.getInstance().spectating) {
            enableClickForOwnKnights();
            if (object.equals("farm1")) {
                shopLabelFarm.setText(
                        Integer.parseInt(shopLabelFarm.getText()) + 2 + "");
            }
        }
        if (object.contains("knight")) {
            if (!placeKnight.isPlaying()) {
                placeKnight.play();
            }
        }
    }

    /**
     * Removes the object of given tile.
     *
     * @param mapValue Id of field  (0 - 179).
     */
    public void removeObject(int mapValue) {
        Tile tile = (Tile) mapPane.getChildren().get(mapValue);
        if (tile.currentColor.equals("red") && tile.currentObject.equals("farm")) {
            shopLabelFarm.setText(Integer.parseInt(shopLabelFarm.getText()) - 2 + "");
        } else if (tile.currentObject != null && tile.currentObject.contains("knight")) {
            oldKnightPos = new double[]{
                    tile.getLayoutX(), tile.getLayoutY(), mapValue};

        }
        tile.object.setVisible(false);
        tile.currentObject = "";
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
    private void setDropShadow(Button button) {
        button.setOnMousePressed(event -> button.setEffect(new DropShadow()));
        button.setOnMouseReleased(event -> button.setEffect(null));
    }

    /**
     * Handles click on next button.
     * Requesting server to end turn.
     */
    public void onNext() {
        click.play();
        resetAll();
        ClientMain.getInstance().send(new String[]{ServerProtocol.CWFT.name()});
    }

    /**
     * Handles click on knight in shop.
     * Requesting possible fields to place from server.
     */
    public void shopKnightClicked() {
        if (!moneyLabel.getText().startsWith("-")
                && Integer.parseInt(moneyLabel.getText()) >= 10) {
            buy.play();
            clickInstructions.setVisible(true);
            unmarkTiles();
            itemToPlace = "knight";
            ClientMain.getInstance()
                    .send(new String[]{ServerProtocol.CWRB.name(), "knight"});
            ClientMain.getInstance().stage.getScene().setCursor(
                    new ImageCursor(new Image("images/game_screen/knight1Red.png")));
        } else {
            flashLabel(moneyLabel);
        }
    }

    /**
     * Handles click on tower in shop.
     * Requesting possible fields to place from server.
     */
    public void shopTowerClicked() {
        if (!moneyLabel.getText().startsWith("-")
                && Integer.parseInt(moneyLabel.getText()) >= 15) {
            buy.play();
            unmarkTiles();
            itemToPlace = "tower";
            clickInstructions.setVisible(true);
            ClientMain.getInstance()
                    .send(new String[]{ServerProtocol.CWRB.name(), "tower"});
            ClientMain.getInstance().stage.getScene().setCursor(
                    new ImageCursor(new Image("images/game_screen/tower1Red.png")));
        } else {
            flashLabel(moneyLabel);
        }
    }

    /**
     * Handles click on farm in shop.
     * Requesting possible fields to place from server.
     */
    public void shopFarmClicked() {
        if (!moneyLabel.getText().startsWith("-")
                && Integer.parseInt(moneyLabel.getText()) >= Integer.parseInt(
                shopLabelFarm.getText())) {
            buy.play();
            clickInstructions.setVisible(true);
            unmarkTiles();
            itemToPlace = "farm";
            ClientMain.getInstance()
                    .send(new String[]{ServerProtocol.CWRB.name(), "farm"});
            ClientMain.getInstance().stage.getScene().setCursor(
                    new ImageCursor(new Image("images/game_screen/farm1Red.png")));
        } else {
            flashLabel(moneyLabel);
        }
    }

    /**
     * Hides tutorial image view and prevents from showing again.
     */
    private void hideTutorial() {
        ClientMain.getInstance().tutorialWatched = true;
        anchorPane.getChildren().remove(tutorial);
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
     * Set Border Effect for given Hbox.
     *
     * @param shopItem Shop item you want to apply the effect.
     */
    private void setBorderOnClick(HBox shopItem) {
        Border border = new Border(new BorderStroke(Color.WHITE,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
        shopItem.setOnMouseEntered(event -> shopItem.setBorder(border));
        shopItem.setOnMouseExited(event -> shopItem.setBorder(Border.EMPTY));
        Border borderClick = new Border(new BorderStroke(Color.YELLOW,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
        shopItem.setOnMousePressed(event -> shopItem.setBorder(borderClick));
        shopItem.setOnMouseReleased(event -> shopItem.setBorder(Border.EMPTY));
    }

    /**
     * Marks all red fields with knights and handles clicking.
     */
    public void enableClickForOwnKnights() {
        for (int i = 0; i < 180; i++) {
            if (mapPane.getChildren().get(i) instanceof Tile) {
                Tile tile = (Tile) mapPane.getChildren().get(i);
                if (tile.currentObject != null && tile.currentObject.equals("knight")
                        && tile.currentColor.equals("red")) {
                    if (!alreadyMoved.contains(i)) {
                        markTile(tile.id);
                        tile.animateKnight();
                    }
                }
            }
        }
    }

    /**
     * Marks specific tile on map.
     *
     * @param mapValue Id of field  (0 - 179).
     */
    public void markTile(int mapValue) {
        Tile tile = (Tile) mapPane.getChildren().get(mapValue);
        if (tile.currentColor != null) {
            String color = tile.currentColor.substring(0, 1)
                    .toUpperCase() + tile.currentColor.substring(1);
            tile.hex.setImage(new Image("images/game_screen/tile" + color + "M.png"));
            tile.hex.setOnMouseClicked(event -> tile.onClick(event, this));
        }
    }

    /**
     * Unmarks all tiles and prevents from registering clicks.
     */
    public void unmarkTiles() {
        for (int i = 0; i < 180; i++) {
            if (mapPane.getChildren().get(i) instanceof Tile) {
                Tile tile = (Tile) mapPane.getChildren().get(i);
                if (tile.currentColor != null) {
                    String color = tile.currentColor.substring(0, 1)
                            .toUpperCase() + tile.currentColor.substring(1);
                    tile.hex.setImage(new Image(
                            "images/game_screen/tile" + color + ".png"));
                    tile.hex.setOnMouseClicked(Event::consume);
                    if (tile.isAnimationRunning()) {
                        tile.getTimeLine().stop();
                        tile.setAnimationRunning(false);
                    }
                }
            }
        }
    }

    /**
     * Resets all variables modified during turn.
     * Disables all buttons while it's not the players turn.
     */
    public void resetAll() {
        shopFarm.setOnMouseClicked(Event::consume);
        shopKnight.setOnMouseClicked(Event::consume);
        shopTower.setOnMouseClicked(Event::consume);
        btnNext.setDisable(true);
        unmarkTiles();
        oldKnightPos = null;
        clickInstructions.setVisible(false);
        alreadyMoved.clear();
        itemToMove = "";
        itemToPlace = "";
        ClientMain.getInstance().stage.getScene().setCursor(Cursor.DEFAULT);
        ClientMain.getInstance().stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED,
                keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.F2
                            || keyEvent.getCode() == KeyCode.F3) {
                        keyEvent.consume();
                    }
                });
    }

    /**
     * Enables Shop and next buttons.
     * Called when it's the players turn.
     */
    public void enableButtons() {
        oldKnightPos = null;
        shopFarm.setOnMouseClicked(event -> shopFarmClicked());
        shopKnight.setOnMouseClicked(event -> shopKnightClicked());
        shopTower.setOnMouseClicked(event -> shopTowerClicked());
        btnNext.setDisable(false);
        ClientMain.getInstance().stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED,
                keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.F2) {
                        ClientMain.getInstance().send(
                                new String[]{ServerProtocol.CWTC.name(), "0"});
                        System.out.println("Cheat activated!");
                    } else if (keyEvent.getCode() == KeyCode.F3) {
                        ClientMain.getInstance().send(
                                new String[]{ServerProtocol.CWTC.name(), "1"});
                        System.out.println("Cheat activated!");
                    }
                });
    }


    /**
     * Flash opacity of a Label.
     *
     * @param label Label the effect should be applied.
     */
    public void flashLabel(Label label) {
        FadeTransition fadeTransition = new FadeTransition(
                Duration.seconds(0.1), label);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(10);
        fadeTransition.play();
    }
}