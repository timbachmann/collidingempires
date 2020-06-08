package collidingempires.client.net;

import collidingempires.client.ClientMain;
import collidingempires.server.net.Packager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Thread to handle the server messages.
 */
public class ServerInputThread implements Runnable {
    InputStream in;
    public static volatile boolean isRunning;

    /**
     * Constructor to set the InputStream.
     * @param in Input Stream to read from server.
     */
    public ServerInputThread(InputStream in) {
        this.in = in;
        isRunning = true;
    }

    /**
     * Method to listen for messages from server,
     * validate them and send them to the protocolSwitch method
     * for further processing.
     */
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        while (isRunning) {
            try {
                String message = bufferedReader.readLine();
                if (message != null) {
                    String[] serverMessage = Packager.unpack(message, "client");
                    System.out.println("Receive: " + Packager.pack(serverMessage));
                    protocolSwitch(serverMessage);
                } else {
                    throw new IOException("Connection Lost!");
                }

            } catch (IOException e) {
                System.out.println("Connection was terminated!");
                if (!ClientMain.getInstance().terminate) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("Connection to server lost. "
                                + "Please connect again.");
                        alert.initOwner(ClientMain.getInstance().stage);
                        alert.initStyle(StageStyle.UNDECORATED);
                        DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.getStylesheets().add(
                                getClass().getResource("/layout/dialog.css")
                                        .toExternalForm());
                        alert.showAndWait();
                        ClientMain.getInstance().setScene("layout_connection.fxml");
                    });
                }
                return;
            } catch (NullPointerException e) {
                System.out.println("Connection was terminated!");
                if (!ClientMain.getInstance().terminate) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("Connection to server lost. "
                                + "Please connect again.");
                        alert.initOwner(ClientMain.getInstance().stage);
                        alert.initStyle(StageStyle.UNDECORATED);
                        DialogPane dialogPane = alert.getDialogPane();
                        dialogPane.getStylesheets().add(
                                getClass().getResource("/layout/dialog.css")
                                        .toExternalForm());
                        alert.showAndWait();
                        ClientMain.getInstance().setScene("layout_connection.fxml");
                    });
                }
            }
        }
    }

    /**
     * Contains the switch deciding what to to according
     * to the received message and its arguments.
     *
     * @param serverMessage String[] containing the protocol message and its arguments.
     */
    private void protocolSwitch(String[] serverMessage) {
        ClientProtocol sequence = ClientProtocol.valueOf(serverMessage[0]);
        Platform.runLater(() -> {
            switch (sequence) {
                case CCNN:
                    ClientMain.getInstance().updatedUsername(serverMessage[1],
                            serverMessage[2]);
                    break;
                case TXTR:
                    ClientMain.getInstance().receiveChat(serverMessage[1],
                            serverMessage[2], serverMessage[3]);
                    break;
                case TCMS:
                    ClientMain.getInstance().textMessageStatusUpdated(serverMessage[1]);
                    break;
                case CLSV:
                    ClientMain.getInstance().clientLeftServer(serverMessage[1]);
                    break;
                case NCON:
                    ClientMain.getInstance().newClientOnServer(serverMessage[1]);
                    break;
                case MISV:
                    ClientMain.getInstance().connectionSuccessful(serverMessage[1]);
                    break;
                case NLOS:
                    ClientMain.getInstance().newLobby(serverMessage[1],
                            serverMessage[2]);
                    break;
                case ACJL:
                    ClientMain.getInstance().clientJoinedLobby(serverMessage[1]);
                    break;
                case ACCS:
                    ClientMain.getInstance().clientChangedStatus(serverMessage[1]);
                    break;
                case ACLL:
                    ClientMain.getInstance().clientLeftLobby(serverMessage[1]);
                    break;
                case ALCS:
                    ClientMain.getInstance().aLobbyChangedStatus(serverMessage[1],
                            serverMessage[2], serverMessage[3]);
                    break;
                case ALDS:
                    ClientMain.getInstance().lobbyDeleted(serverMessage[1]);
                    break;
                case CHSE:
                    ClientMain.getInstance().newHighscoreListEntry(serverMessage[1],
                            Integer.parseInt(serverMessage[2]));
                    break;
                case FCOW:
                    ClientMain.getInstance().fieldChangedOwner(serverMessage[1],
                            serverMessage[2]);
                    break;
                case FTJL:
                    ClientMain.getInstance().failedToJoinLobby();
                    break;
                case GEND:
                    ClientMain.getInstance().gameEnded(serverMessage[1]);
                    break;
                case LTOT:
                    ClientMain.getInstance().timeLeft(serverMessage[1]);
                    break;
                case NEMA:
                    ClientMain.getInstance().newMap(serverMessage[1]);
                    break;
                case NFGA:
                    ClientMain.getInstance().newFinishedGame(serverMessage[1],
                            serverMessage[2]);
                    break;
                case NOOM:
                    ClientMain.getInstance().newObjectOnMap(serverMessage[1],
                            serverMessage[2], serverMessage[3]);
                    break;
                case NPOT:
                    ClientMain.getInstance().newPlayerOnTurn(serverMessage[1]);
                    break;
                case PFOR:
                    ClientMain.getInstance().possibleFieldsRequest(serverMessage[1]);
                    break;
                case TGST:
                    ClientMain.getInstance().gameStart();
                    break;
                case YAIL:
                    ClientMain.getInstance().youAreInLobby(serverMessage[1]);
                    break;
                case YCBC:
                    ClientMain.getInstance().coinBalanceChanged(serverMessage[1]);
                    break;
                case YLTL:
                    ClientMain.getInstance().youLeftLobby();
                    break;
                case BCNR:
                    ClientMain.getInstance().setBalanceChange(serverMessage[1]);
                    break;
                case OOMR:
                    ClientMain.getInstance().objectRemoved(serverMessage[1]);
                    break;
                case YASP:
                    ClientMain.getInstance().youAreSpectating();
                    break;
                default:
                    break;
            }
        });

    }
}
