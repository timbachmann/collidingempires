package collidingempires.client.util;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;

/**
 * Class representing a lobby list cell.
 */
public class LobbyListCell extends ListCell<LobbyListItem> {

    @FXML
    Label nameLabel;
    @FXML
    Label playerLabel;
    @FXML
    Pane cellPane;
    FXMLLoader fxmlLoader;

    /**
     * Updates the view of a cell.
     *
     * @param item  Item update is performed on.
     * @param empty True if empty.
     */
    @Override
    protected void updateItem(LobbyListItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass()
                        .getResource("/layout/lobbyListCell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            nameLabel.setFont(Font.loadFont(getClass()
                    .getResourceAsStream("/font/build.ttf"), 48));
            nameLabel.setTextFill(Color.WHITE);
            playerLabel.setTextFill(Color.WHITE);
            nameLabel.setText(item.getName());
            playerLabel.setFont(Font.loadFont(getClass()
                    .getResourceAsStream("/font/build.ttf"), 48));
            playerLabel.setText(item.getPlayerCount() + "/4");
            if (item.getPlayerCount() <= 1) {
                playerLabel.setTextFill(Color.RED);
            } else {
                playerLabel.setTextFill(Color.GREEN);
            }
            setText(null);
            setGraphic(cellPane);
        }
    }
}
