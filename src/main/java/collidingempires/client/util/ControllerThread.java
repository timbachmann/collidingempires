package collidingempires.client.util;

import collidingempires.client.ClientMain;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Scale;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Thread to handle Controller Input and Keyboard Input.
 */
public class ControllerThread implements Runnable {

    private final ControllerManager controllers;
    private Robot robot;
    private KeyCode keyUp = KeyCode.W;
    private KeyCode keyDown = KeyCode.S;
    private KeyCode keyLeft = KeyCode.A;
    private KeyCode keyRight = KeyCode.D;
    private KeyCode keyExit = KeyCode.ESCAPE;
    private KeyCode keyClick = KeyCode.SPACE;
    private final ArrayList<String> keys = new ArrayList<>();
    public volatile boolean running;
    private static ControllerThread instance;
    private int x;
    private int y;

    /**
     * Returns instance of ControllerThread.
     *
     * @return Instance of ControllerThread.
     */
    public static ControllerThread getInstance() {
        return instance;
    }

    /**
     * Defines input actions.
     */
    public enum InputAction {
        A, B, STICK
    }

    /**
     * Initializes variables.
     */
    public ControllerThread() {
        instance = this;
        running = true;
        controllers = new ControllerManager();
        controllers.initSDLGamepad();
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        addKeys();
        ClientMain.getInstance().stage.getScene().addEventHandler(
                javafx.scene.input.KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    /**
     * Returns actions in Set of Input Actions.
     * Deciding whether an input action was performed or not.
     */
    Set<InputAction> actions() {
        ControllerState currState = controllers.getState(0);
        if (!currState.isConnected) {
            return Collections.emptySet();
        }
        Set<InputAction> actions = new HashSet<>();
        if (currState.leftStickMagnitude > 0.3) {
            actions.add(InputAction.STICK);
        }
        if (currState.a) {
            actions.add(InputAction.A);
        }
        if (currState.b) {
            actions.add(InputAction.B);
        }
        return actions;
    }

    /**
     * Simulates mouse moves and clicks if action was detected.
     */
    @Override
    public void run() {
        while (running) {
            ControllerState currState = controllers.getState(0);
            if (currState.isConnected) {
                System.out.println("Controller connected!");
                while (currState.isConnected && running) {
                    currState = controllers.getState(0);

                    if (actions().contains(InputAction.A)) {
                        Platform.runLater(() -> {
                            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        });
                        actions().remove(InputAction.A);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (actions().contains(InputAction.B)) {
                        Platform.runLater(() -> {
                            robot.keyPress(keyExit.getCode());
                            robot.keyRelease(keyExit.getCode());
                        });
                        actions().remove(InputAction.B);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (actions().contains(InputAction.STICK)) {
                        updateMousePos();
                        double angle = currState.leftStickAngle;
                        double magnitude = currState.leftStickMagnitude;

                        Platform.runLater(() -> {
                            double xi = Math.cos(Math.toRadians(angle));
                            double yi = Math.sin(Math.toRadians(angle));
                            robot.mouseMove((int) (x + xi * (magnitude * 10)),
                                    (int) (y - yi * (magnitude * 10)));
                        });
                        actions().remove(InputAction.STICK);
                    }

                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        controllers.quitSDLGamepad();
    }

    /**
     * Updates mouse position for mouse movement.
     */
    private void updateMousePos() {
        PointerInfo a = MouseInfo.getPointerInfo();
        Point b = a.getLocation();
        x = (int) b.getX();
        y = (int) b.getY();
    }

    /**
     * Updates list of used keys for navigation.
     */
    public void addKeys() {
        keys.clear();
        keys.add(keyUp.toString());
        keys.add(keyDown.toString());
        keys.add(keyLeft.toString());
        keys.add(keyRight.toString());
        keys.add(keyExit.toString());
        keys.add(keyClick.toString());
    }

    /**
     * Getter for keys.
     *
     * @return Key ArrayList.
     */
    public ArrayList<String> getKeys() {
        return keys;
    }

    /**
     * Method to handle all key presses.
     *
     * @param keyEvent keyEvent.
     */
    public void handleKeyPress(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode() == ControllerThread.getInstance().getKeyExit()
                && ClientMain.getInstance().lobbyName == null
                && !ClientMain.getInstance().alreadyLeftServer) {
            keyEvent.consume();
            ClientMain.getInstance().handleESC();
        } else if (keyEvent.getCode() == KeyCode.F11
                && !ClientMain.getInstance().fullscreen) {
            ClientMain.getInstance().stage.setFullScreen(true);
            ClientMain.getInstance().root.getTransforms()
                    .add(new Scale(1.25, 1.25, 0, 0));
            ClientMain.getInstance().fullscreen = true;
        } else if (keyEvent.getCode() == KeyCode.F11) {
            ClientMain.getInstance().root.getTransforms()
                    .add(new Scale(0.8, 0.8, 0, 0));
            ClientMain.getInstance().fullscreen = false;
        } else if (keyEvent.getCode() == keyUp) {
            updateMousePos();
            Platform.runLater(() -> robot.mouseMove(x, y - 10));
        } else if (keyEvent.getCode() == keyDown) {
            updateMousePos();
            Platform.runLater(() -> robot.mouseMove(x, y + 10));
        } else if (keyEvent.getCode() == keyLeft) {
            updateMousePos();
            Platform.runLater(() -> robot.mouseMove(x - 10, y));
        } else if (keyEvent.getCode() == keyRight) {
            updateMousePos();
            Platform.runLater(() -> robot.mouseMove(x + 10, y));
        } else if (keyEvent.getCode() == keyClick) {
            Platform.runLater(() -> {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            });
        }
    }

    /**
     * Setter for keyUp.
     * @param keyUp KeyCode to set.
     */
    public void setKeyUp(KeyCode keyUp) {
        this.keyUp = keyUp;
    }

    /**
     * Setter for keyDown.
     * @param keyDown KeyCode to set.
     */
    public void setKeyDown(KeyCode keyDown) {
        this.keyDown = keyDown;
    }

    /**
     * Setter for keyLeft.
     * @param keyLeft KeyCode to set.
     */
    public void setKeyLeft(KeyCode keyLeft) {
        this.keyLeft = keyLeft;
    }

    /**
     * Setter for keyRight.
     * @param keyRight KeyCode to set.
     */
    public void setKeyRight(KeyCode keyRight) {
        this.keyRight = keyRight;
    }

    /**
     * Setter for keyExit.
     * @param keyExit KeyCode to set.
     */
    public void setKeyExit(KeyCode keyExit) {
        this.keyExit = keyExit;
    }

    /**
     * Setter for keyClick.
     * @param keyClick KeyCode to set.
     */
    public void setKeyClick(KeyCode keyClick) {
        this.keyClick = keyClick;
    }

    /**
     * Getter for keyUp.
     *
     * @return keyUp.
     */
    public KeyCode getKeyUp() {
        return keyUp;
    }

    /**
     * Getter for keyDown.
     *
     * @return keyDown.
     */
    public KeyCode getKeyDown() {
        return keyDown;
    }

    /**
     * Getter for keyLeft.
     *
     * @return keyLeft.
     */
    public KeyCode getKeyLeft() {
        return keyLeft;
    }

    /**
     * Getter for keyRight.
     *
     * @return keyRight.
     */
    public KeyCode getKeyRight() {
        return keyRight;
    }

    /**
     * Getter for keyExit.
     *
     * @return keyExit.
     */
    public KeyCode getKeyExit() {
        return keyExit;
    }

    /**
     * Getter for keyClick.
     *
     * @return keyClick.
     */
    public KeyCode getKeyClick() {
        return keyClick;
    }
}
