package collidingempires.client.screens;

import collidingempires.client.ClientMain;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;

/**
 * Class managing the Nodes of layout_connection.fxml.
 */
public class ConnectionScreenController {

    public TextField ipField;
    public TextField portField;
    public TextField usernameField;
    public Button btnBack;
    public Button btnConnect;
    private AudioClip click;

    /**
     * Called when layout is first displayed.
     * Sets ClickListeners for TextFields and Buttons.
     * Changes the text of TextFields to avoid some characters from typing in.
     */
    @FXML
    public void initialize() {
        click = new AudioClip(getClass()
                .getResource("/sound/click.mp3").toExternalForm());
        click.setVolume(0.2);
        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\^_/*,#")) {
                usernameField.setText(newValue.replaceAll("[\\^_/*,#]", ""));
            }
        });
        ipField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\^_*,#")) {
                ipField.setText(newValue.replaceAll("[\\^_*,#]", ""));
            }
        });
        setDropShadow(btnBack);
        setDropShadow(btnConnect);
        setDefaultBorder(ipField);
        setDefaultBorder(portField);
        setDefaultBorder(usernameField);
    }

    /**
     * Called when back button is pressed.
     */
    @FXML
    public void onBack() {
        click.play();
        ClientMain.getInstance().setScene("layout_welcome.fxml");
    }

    /**
     * Called when connect button is pressed.
     * Gets text from TextFields.
     * If a TextField is empty, a red border is set.
     * If not, the ServerInputThread is started with provided details.
     */
    @FXML
    public void onClickConnect() {
        click.play();
        String ip = ipField.getText();
        String port = portField.getText();
        String name = usernameField.getText();

        BorderWidths borderWidths = new BorderWidths(3);
        Border newBorder = new Border(new BorderStroke(Color.RED,
                BorderStrokeStyle.SOLID, null, borderWidths));

        if (ip.trim().isEmpty()) {
            ipField.setBorder(newBorder);
        } else if (port.trim().isEmpty()) {
            portField.setBorder(newBorder);
        } else if (name.trim().isEmpty()) {
            usernameField.setBorder(newBorder);
        } else {
            ClientMain.getInstance().connect(ip, port);
            ClientMain.getInstance().requestedName = name;
        }
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
     * Resets Border of TextField when clicked on it.
     *
     * @param textField TextField you want to apply the border.
     */
    public void setDefaultBorder(TextField textField) {
        textField.setOnMouseClicked(event -> textField.setBorder(Border.EMPTY));
    }
}
