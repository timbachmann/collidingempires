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
 * Class representing a game list cell.
 */
public class HighscoreListCell extends ListCell<HighscoreListItem> {

    @FXML
    Label highscoreNameLabel;
    @FXML
    Label highscoreTimeLabel;
    @FXML
    Pane cellPane;
    FXMLLoader fxmlLoader;

    /**
     * Updates the view of a cell.
     * @param item Item update is performed on.
     * @param empty True if empty.
     */
    @Override
    protected void updateItem(HighscoreListItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass()
                        .getResource("/layout/highscoreListCell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            highscoreNameLabel.setFont(Font.loadFont(getClass()
                    .getResourceAsStream("/font/build.ttf"), 48));
            highscoreTimeLabel.setFont(Font.loadFont(getClass()
                    .getResourceAsStream("/font/build.ttf"), 48));
            highscoreNameLabel.setTextFill(Color.WHITE);
            highscoreTimeLabel.setTextFill(Color.WHITE);
            highscoreNameLabel.setText(item.getName());
            highscoreTimeLabel.setText(item.getTime() + "");
            setText(null);
            setGraphic(cellPane);
        }
    }
}
