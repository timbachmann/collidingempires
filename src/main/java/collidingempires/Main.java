package collidingempires;

import collidingempires.client.ClientMain;
import collidingempires.server.ServerMain;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Main Wrapper of "Colliding Empires".
 * It contains the main-method, that launches the requested software-part.
 * This class is meant to be the "Main-Class" of the Project
 * "Colliding Empires" and the executable jar-archive.
 */
public class Main {
    private static final Logger LOGGER =
            LogManager.getLogger(Main.class.getName());

    /**
     * This is a simple main method to launch the client-
     * or server software of "Colliding Empires".
     * It just checks for the right number of arguments and pass
     * them to the {@link ClientMain}- or {@link ServerMain} - class
     * @param args the commandline-arguments passed by the user.
     */
    public static void main(final String[] args) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "server":
                    if (args.length == 2) {
                        // start server on port args[1]
                        ServerMain.main(new String[]{args[1]});
                    }
                    break;
                case "client":
                    if (args.length == 1) {
                        //start client GUI with the start-screen
                        Application.launch(ClientMain.class, "", "", "");
                    } else if (args.length <= 3) {
                        //start client-GUI and connect directly to a server
                        String[] split = args[1].split(":");
                        if (split.length == 2) {
                            String[] arguments = new String[3];
                            arguments[0] = split[0]; // hostname
                            arguments[1] = split[1];
                            // extract username(could be empty)
                            arguments[2] = (args.length == 3) ? args[2] : "";
                            Application.launch(ClientMain.class, arguments[0],
                                    arguments[1], arguments[2]);
                        } else {
                            LOGGER.error("The given Arguments are not"
                                    + " in the required pattern");
                        }
                    } else {
                        LOGGER.info("Wrong number of arguments entered");
                        System.out.println("Wrong number of arguments! For "
                                + "option -client-, you "
                                + "need to give additional 0 or 2 arguments");
                    }
                    break;
                default:
                    LOGGER.info("Unknown first argument");
                    System.out.println("Unknown first argument: " + args[0]
                            + ". Use -client- or -server- !");
            }
        } else {
            LOGGER.info("No argument was given");
            System.out.println("You need to give at least one argument!");
        }
    }
}
