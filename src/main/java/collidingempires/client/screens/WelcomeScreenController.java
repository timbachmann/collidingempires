package collidingempires.client.screens;

import collidingempires.client.ClientMain;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.media.AudioClip;

import java.io.IOException;

/**
 * Class managing the Nodes of layout_welcome.fxml
 */
public class WelcomeScreenController {

    public Button btnConnectToServer;
    public Button btnQuit;
    private AudioClip click;

    /**
     * Sets DropShadows for all Buttons.
     */
    @FXML
    public void initialize() {
        setDropShadow(btnConnectToServer);
        setDropShadow(btnQuit);
        click = new AudioClip(getClass()
                .getResource("/sound/click.mp3").toExternalForm());
        click.setVolume(0.2);
    }

    /**
     * Called when the connect button was pressed.
     * Sets scene for main stage to layout_connection.fxml.
     */
    @FXML
    public void onClickConnect() {
        click.play();
        ClientMain.getInstance().setScene("layout_connection.fxml");
    }

    /**
     * Called when the quit button was pressed.
     * Terminates application.
     */
    @FXML
    public void onQuit() {
        click.play();
        try {
            ClientMain.getInstance().terminate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClientMain.getInstance().stage.close();
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
}

