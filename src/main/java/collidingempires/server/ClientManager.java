package collidingempires.server;

import collidingempires.client.net.ClientProtocol;
import collidingempires.server.util.ScoreHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.prometheus.client.Gauge;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Client Manager holds all Executor-threads and has functions,
 * that are used by them to communicate between each other.
 */
public class ClientManager {
    private static final Gauge CONN_CLIENTS = Gauge.build().name("connected_clients")
            .help("The Number of clients connected to the Server!").register();
    private HashMap<String, Executor> clientsByNicknames;
    private HashMap<String, Integer> scoreList;
    private int numClients;
    private static Logger logger =
            LogManager.getLogger(ClientManager.class.getName());

    /**
     * Constructor.
     * Creates a new HashMap for the {@link Executor} Threads and initialise
     * the number of clients counter.
     */
    public ClientManager() {
        this.numClients = 0;
        this.clientsByNicknames = new HashMap<>();
        initScoreList();
    }

    /**
     * Reads existing score via {@link ScoreHandler}.
     * If there is an error
     * or no data the map is just initialised empty.
     */
    private void initScoreList() {
        logger.info("Init Scores");
        this.scoreList = ScoreHandler.getScore();
        if (scoreList == null) {
            logger.info("null - initial Scores empty");
            scoreList = new HashMap<>();
        }
    }

    /**
     * Getter for the scoreList.
     * @return the current ScoreList
     */
    public HashMap<String, Integer> getScores() {
        return scoreList;
    }

    /**
     * Adds or modify entry.
     * broadcasts change and new list is dumped.
     *
     * @param name  name of player
     * @param score the new score of the player
     */
    public void addScoreEntry(String name, Integer score) {
        scoreList.put(name, score);
        broadcastServer(new String[]{ClientProtocol.CHSE.toString(),
                name, String.valueOf(score)});
        ScoreHandler.dumpScore(this.scoreList);
    }

    /**
     * Get The Highscore for a player.
     *
     * @param name Name of the Player
     * @return The score of the Player or default value 0
     */
    public Integer getScore(String name) {
        return scoreList.getOrDefault(name, 0);
    }

    /**
     * Returns the the number of clients connected to the Server.
     *
     * @return the variable num_clients
     */
    public int getNumClients() {
        return this.numClients;
    }

    /**
     * Setter for the nuClients field.
     *
     * @param num new number of clients
     */
    public void setNumClients(final int num) {
        this.numClients = num;
        CONN_CLIENTS.set(num);
    }

    /**
     * Inserts a new client to the Manager.
     *
     * @param nickname       unique nickname of client
     * @param clientExecutor the Executor-thread of the client-connection
     */
    public void put(String nickname, Executor clientExecutor) {
        logger.info("client " + nickname + " added to Manager");
        broadcastServer(new String[]{
                ClientProtocol.NCON.name(), nickname});
        this.clientsByNicknames.put(nickname, clientExecutor);
        this.numClients += 1;
        CONN_CLIENTS.inc();
        clientExecutor.getClientReference().send(new String[]{
                ClientProtocol.MISV.name(), nickname});
        clientExecutor.initialUpdate();
        clientExecutor.start();
    }

    /**
     * Returns the Object-Reference of a client-executor Thread.
     *
     * @param nickname The Nickname of the client you want the reference from.
     * @return null: no client with the given nickname, else: the Objectreference;
     */
    public Executor get(String nickname) {
        if (this.nicknameExists(nickname)) {
            return clientsByNicknames.get(nickname);
        } else {
            return null;
        }
    }

    /**
     * Sends a given Sequence to all connected clients.
     *
     * @param toSend the Stringarray containing the order, that should be packed and sent.
     */
    public void broadcastServer(final String[] toSend) {
        for (String s : getNicknames()) {
            Executor t = this.get(s);
            t.getClientReference().send(toSend);
        }
    }

    /**
     * Closes a connection to a client and informs all clients.
     *
     * @param nickname Nickname of the client to be deleted
     */
    public void closeConnection(final String nickname) {
        if (this.nicknameExists(nickname)) {
            Executor toTerminate = this.get(nickname);
            try {
                logger.info("Try to close Connection properly for: " + nickname);
                toTerminate.leaveLobby();
                this.clientsByNicknames.remove(nickname);
                this.numClients -= 1;
                broadcastServer(new String[]{ClientProtocol.CLSV.name(),
                        nickname});
                logger.info("Try to terminate Executor for: " + nickname);
                toTerminate.terminate();
                logger.info("closed connection to client: " + nickname);
                System.out.println("closed connection to client: "
                        + nickname);
            } catch (IOException e) {
                logger.error(e.getMessage());
                System.out.println(e.getMessage());
            } finally {
                // decrease stats for prometheus
                CONN_CLIENTS.dec();
            }
        }
    }

    /**
     * Returns a Set of all Nicknames.
     *
     * @return all Nicknames
     */
    public Set<String> getNicknames() {
        return clientsByNicknames.keySet();
    }

    /**
     * Returns whether the nickname already exists or not.
     *
     * @param lookup the nickname to check existance for
     * @return true: nickname already exists, false: nickname doesnt exist
     */
    public boolean nicknameExists(final String lookup) {
        return clientsByNicknames.containsKey(lookup);
    }

    /**
     * Changes the Nickname of the client and informs all clients on the server.
     * Refresh the key of the HashMap and the attribute of the client itself.
     * If the requested new Nickname already exists, a Number is appended
     * until its unique.
     *
     * @param oldNickname Nickname of the client who's nickname to change
     * @param newNickname The new Nickname you want to Change to
     */
    public void changeNickname(final String oldNickname, String newNickname) {
        if (nicknameExists(oldNickname)) {
            logger.info("Change Nickname of " + oldNickname
                    + " to " + newNickname);
            int tail = 0;
            while (nicknameExists(newNickname)) {
                newNickname = newNickname + ++tail;
            }
            Executor e = clientsByNicknames.remove(oldNickname); //change HashMap-key
            clientsByNicknames.put(newNickname, e);
            //change Nickname in Object itself
            get(newNickname).getClientReference().setNickname(newNickname);
            logger.info("Nickname changed from " + oldNickname
                    + "to " + newNickname);
            broadcastServer(new String[]{ClientProtocol.CCNN.name(),
                    oldNickname, newNickname});
        }
    }
}
