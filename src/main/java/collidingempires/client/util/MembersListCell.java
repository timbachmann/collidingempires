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
 * Class representing a member list cell.
 */
public class MembersListCell extends ListCell<MembersListItem> {

    @FXML
    Label nameLabel;
    @FXML
    Pane cellPane;
    @FXML
    Label statusLabel;
    FXMLLoader fxmlLoader;

    /**
     * Updates the view of a cell.
     *
     * @param item  Item update is performed on.
     * @param empty True if empty.
     */
    @Override
    protected void updateItem(MembersListItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass()
                        .getResource("/layout/membersListCell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            nameLabel.setFont(Font.loadFont(getClass()
                    .getResourceAsStream("/font/build.ttf"), 28));
            statusLabel.setFont(Font.loadFont(getClass()
                    .getResourceAsStream("/font/build.ttf"), 28));
            nameLabel.setTextFill(Color.WHITE);
            statusLabel.setTextFill(Color.WHITE);
            nameLabel.setText(item.getName());
            statusLabel.setText(item.getStatus().toString());
            setText(null);
            setGraphic(cellPane);
        }
    }
}
