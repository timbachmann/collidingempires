package collidingempires.client.util;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;

/**
 * Class representing a game list cell.
 */
public class GamesListCell extends ListCell<GamesListItem> {

    @FXML
    ImageView spectate;
    @FXML
    Label gamesNameLabel;
    @FXML
    Label gamesWinnerLabel;
    @FXML
    Pane cellPane;
    @FXML
    Label gamesStatusLabel;
    FXMLLoader fxmlLoader;


    /**
     * Updates the view of a cell.
     *
     * @param item  Item update is performed on.
     * @param empty True if empty.
     */
    @Override
    protected void updateItem(GamesListItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass()
                        .getResource("/layout/gamesListCell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            gamesNameLabel.setFont(Font.loadFont(getClass()
                    .getResourceAsStream("/font/build.ttf"), 48));
            gamesWinnerLabel.setFont(Font.loadFont(getClass()
                    .getResourceAsStream("/font/build.ttf"), 48));
            gamesStatusLabel.setFont(Font.loadFont(getClass()
                    .getResourceAsStream("/font/build.ttf"), 48));
            gamesNameLabel.setTextFill(Color.WHITE);
            gamesWinnerLabel.setTextFill(Color.WHITE);
            gamesStatusLabel.setTextFill(Color.WHITE);
            gamesNameLabel.setText(item.getName());
            gamesStatusLabel.setText(item.getStatus().toString());
            gamesWinnerLabel.setText(item.getWinner());
            if (item.getStatus().equals(GamesListItem.STATUS.OPEN)) {
                gamesStatusLabel.setTextFill(Color.YELLOW);
                spectate.setVisible(false);
            } else if (item.getStatus().equals(GamesListItem.STATUS.ONGOING)) {
                spectate.setVisible(true);
                gamesStatusLabel.setTextFill(Color.ORANGE);
            } else if (item.getStatus().equals(GamesListItem.STATUS.FINISHED)) {
                gamesStatusLabel.setTextFill(Color.GREEN);
                spectate.setVisible(false);
            }
            setText(null);
            setGraphic(cellPane);
        }
    }
}
