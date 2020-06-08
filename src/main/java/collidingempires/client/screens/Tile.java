package collidingempires.client.screens;

import collidingempires.client.ClientMain;
import collidingempires.server.net.ServerProtocol;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class representing a tile layout on map.
 */
public class Tile extends StackPane {
    public final int id;
    public final ImageView hex = new ImageView();
    public final ImageView object = new ImageView();
    public String currentColor;
    public String currentObject;
    private String level;
    private Timeline timeLine;
    private boolean animationRunning = false;

    /**
     * Constructor to set properties of a Tile.
     *
     * @param id Tile id
     */
    public Tile(int id) {
        this.id = id;
        currentColor = "default";
        prefHeight(87);
        prefWidth(100);
        hex.setImage(new Image("images/game_screen/tileDefault.png"));
        hex.prefHeight(87);
        hex.prefWidth(100);
        getChildren().add(hex);
        object.setMouseTransparent(true);
    }

    /**
     * Method called when tile was clicked.
     * Sends request to server depending on previous actions.
     *
     * @param gsc        GameScreenController reference.
     * @param mouseEvent mouseEvent.
     */
    public void onClick(MouseEvent mouseEvent, GameScreenController gsc) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            gsc.unmarkTiles();
            if (!gsc.itemToPlace.equals("")) {
                gsc.clickInstructions.setVisible(false);
                ClientMain.getInstance().stage.getScene().setCursor(Cursor.DEFAULT);
                ClientMain.getInstance()
                        .send(new String[]{ServerProtocol.CWPI.name(),
                                gsc.itemToPlace, String.valueOf(id)});
                gsc.itemToPlace = "";
            } else if (gsc.itemToMove.equals("")) {
                gsc.clickInstructions.setVisible(true);
                gsc.itemToMove = String.valueOf(id);
                ClientMain.getInstance()
                        .send(new String[]{ServerProtocol.CWRM.name(),
                                String.valueOf(id)});
                ClientMain.getInstance().stage.getScene().setCursor(
                        new ImageCursor(new Image("images/game_screen/knight"
                                + level + "Red" + ".png")));
            } else {
                gsc.clickInstructions.setVisible(false);
                ClientMain.getInstance().stage.getScene().setCursor(Cursor.DEFAULT);
                if (!gsc.itemToMove.equals("")) {
                    ClientMain.getInstance()
                            .send(new String[]{ServerProtocol.CWTM.name(),
                                    gsc.itemToMove, String.valueOf(id)});
                    gsc.alreadyMoved.add(id);
                }
                gsc.itemToMove = "";
            }
        }
    }

    /**
     * Method to change a tile's color.
     *
     * @param color New color (red/green/blue/yellow).
     */
    public void setColor(String color) {
        switch (color) {
            case "red":
                hex.setImage(new Image("images/game_screen/tileRed.png"));
                break;
            case "blue":
                hex.setImage(new Image("images/game_screen/tileBlue.png"));
                break;
            case "green":
                hex.setImage(new Image("images/game_screen/tileGreen.png"));
                break;
            case "yellow":
                hex.setImage(new Image("images/game_screen/tileYellow.png"));
                break;
            case "default":
                hex.setImage(new Image("images/game_screen/tileDefault.png"));
                break;
            default:
                break;
        }
        currentColor = color;
    }

    /**
     * Sets the image of the object imageView in tile layout and displays it.
     *
     * @param objectName Name of new Object ((knight/tree/farm/tower) + Level(1 - 4)
     *                   + Color(Red/Green/Blue/Yellow)).
     */
    public void setObject(String objectName) {
        if (objectName.contains("knight")) {
            level = objectName.substring(6, 7);
            currentObject = "knight";
            if (objectName.contains("Red")) {
                if (animationRunning) {
                    timeLine.stop();
                    animationRunning = false;
                }
            }
        } else if (objectName.contains("tower")) {
            level = "1";
            currentObject = "tower";
        } else if (objectName.contains("farm")) {
            level = "1";
            currentObject = "farm";
        } else if (objectName.contains("tree")) {
            level = "1";
            currentObject = "tree";
        }
        object.setImage(new Image("images/game_screen/" + objectName + ".png"));
        object.setPreserveRatio(true);
        object.setFitWidth(100);
        if (!getChildren().contains(object)) {
            getChildren().add(object);
        }
        object.setVisible(true);
    }

    /**
     * Start character animation.
     */
    public void animateKnight() {
        if (!animationRunning) {
            System.out.println(level);
            List<Image> images = new ArrayList<>();
            for (int i = 1; i < 7; i++) {
                images.add(new Image("images/game_screen/animation/knight"
                        + level + "Red_" + i + ".png"));
            }
            timeLine = new Timeline();
            Collection<KeyFrame> frames = timeLine.getKeyFrames();
            Duration frameGap = Duration.millis(100);
            Duration frameTime = Duration.ZERO;
            for (Image img : images) {
                frameTime = frameTime.add(frameGap);
                frames.add(new KeyFrame(frameTime, e -> object.setImage(img)));
            }
            timeLine.setCycleCount(Timeline.INDEFINITE);
            timeLine.play();
            animationRunning = true;
        }
    }

    /**
     * Getter for timeline.
     *
     * @return timeLine.
     */
    public Timeline getTimeLine() {
        return timeLine;
    }

    /**
     * Getter for animationRunning.
     *
     * @return animationRunning.
     */
    public boolean isAnimationRunning() {
        return animationRunning;
    }

    /**
     * Setter for animationRunning.
     *
     * @param animationRunning New running state.
     */
    public void setAnimationRunning(boolean animationRunning) {
        this.animationRunning = animationRunning;
    }

    /**
     * Getter for level.
     * @return Current level.
     */
    public String getLevel() {
        return level;
    }
}
