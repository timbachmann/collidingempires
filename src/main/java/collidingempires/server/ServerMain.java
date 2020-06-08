package collidingempires.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main class for the Server-software of the Project.
 * It holds a static main Method and therefor can be executed directly
 * without the Main wrapper. It also can be Used directly as an Object.
 */
public class ServerMain {
    private static final Logger LOGGER =
            LogManager.getLogger(ServerMain.class.getName());
    /**
     * static main method that creates an ServerMain-Object.
     *
     * @param args arguments passed by the user.
     */
    public static void main(String[] args) {
        try {
            LOGGER.info("Try to Run Server On port" + args[0]);
            System.out.println("Server running on port " + args[0]);
            int connectionPort = Integer.parseInt(args[0]);
            if ((connectionPort > 65535) || (connectionPort < 1024)) {
                throw new IOException(connectionPort + " is an invalid port-number");
            }
            Server s = new Server(args[0]);
            s.start();
            Scanner sc = new Scanner(System.in);
            while (true) {
                if (sc.next().equalsIgnoreCase("quit")) {
                    s.shutdown();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
            LOGGER.error(e.getMessage());
            System.exit(1);
        }

    }
}
