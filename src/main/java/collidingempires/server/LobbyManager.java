package collidingempires.server;

import collidingempires.client.net.ClientProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.prometheus.client.Gauge;

/**
 * LobbyManager. Yet an empty shell!
 * will later Manage all Lobbys on the Server.
 */
public class LobbyManager {
    private HashMap<String, Lobby> lobbies;
    private ClientManager clientsReference;
    private List<FinishedGame> finishedGames;
    private static final Gauge NUM_LOBBIES = Gauge.build().name("number_of_lobbies")
            .help("The Number of existing lobbies on the Server").register();
    private static final Gauge NUM_FINISHED_GAMES = Gauge.build()
            .name("number_of_finishedGames")
            .help("Number of finished Games on the Server").register();

    /**
     * Inner class, representing the format, a finished game
     * is saved as. Holds Information about the lobby and the winner.
     */
    protected class FinishedGame {
        String lobby;
        String winner;

        /**
         * Constructor.
         * saves the given data into the fields.
         * @param lobby lobby that finished a game
         * @param winner the winner of that game
         */
        FinishedGame(String lobby, String winner) {
            this.lobby = lobby;
            this.winner = winner;
        }
    }

    /**
     * Constructor.
     * Creates a new ArrayList for the Lobbys to be saved into.
     *
     * @param ref reference of the clientManger
     */
    public LobbyManager(final ClientManager ref) {
        this.clientsReference = ref;
        this.lobbies = new HashMap<>();
        this.finishedGames = new ArrayList<>();
    }

    /**
     * Creates a Lobby with a unique name;
     *
     * @param name the name of the Lobby
     */
    public void createLobby(String name) {
        int tail = 0;
        while (lobbies.containsKey(name)) {
            name = name + ++tail;
        }
        lobbies.put(name, new Lobby(name, this, clientsReference));
        clientsReference.broadcastServer(new String[]{
                ClientProtocol.NLOS.toString(), name, "0"});
        NUM_LOBBIES.inc();
    }

    /**
     * Checks whether a client is already in a Lobby.
     *
     * @param nickname the nickname to look for
     * @return is in a lobby or not
     */
    public String alreadyInLobby(final String nickname) {
        for (Lobby l : lobbies.values()) {
            if (l.inLobby(nickname)) {
                return l.getLobbyName();
            }
        }
        return null;
    }

    /**
     * Add a finished Game to the list.
     *
     * @param lobbyName lobby of the game
     * @param winner    the winner
     */
    public void addFinishedGame(String lobbyName, String winner) {
        finishedGames.add(new FinishedGame(lobbyName, winner));
        clientsReference.broadcastServer(new String[]{
                ClientProtocol.NFGA.toString(), lobbyName, winner});
        NUM_FINISHED_GAMES.inc();
    }

    /**
     * Getter for finishedGames.
     *
     * @return finished games
     */
    public List<FinishedGame> getFinishedGames() {
        return this.finishedGames;
    }

    /**
     * Adds a client to a Lobby. Therefor checks whether the
     * Lobby exists and the client isn't in a Lobby yet.
     *
     * @param nickname  nickname of the client to add
     * @param lobbyname the Lobby to get into
     */
    public void joinLobby(final String nickname, final String lobbyname) {
        if (alreadyInLobby(nickname) == null && lobbyExists(lobbyname)
                && !lobbies.get(lobbyname).isFull()
                && !lobbies.get(lobbyname).isInGame()) {
            // Broadcast Server that there is a new Player in the Lobby=new Player-number
            clientsReference.broadcastServer(new String[]{
                    ClientProtocol.ALCS.toString(), lobbyname,
                    "", String.valueOf(
                    lobbies.get(lobbyname).numPlayers() + 1)});
            // Broadcast Lobby that a new Player joined
            broadcastLobby(lobbyname, new String[]{
                    ClientProtocol.ACJL.toString(), nickname});
            lobbies.get(lobbyname).addPlayer(nickname);
            // Inform Client that it joined successful
            clientsReference.get(nickname).getClientReference().send(new String[]{
                    ClientProtocol.YAIL.toString(), lobbyname});
            // initial update for joined client
            initialLobbyUpdate(lobbyname, nickname);
        } else if (clientsReference.nicknameExists(nickname)) {
            String[] message = new String[]{ClientProtocol.FTJL.toString()};
            clientsReference.get(nickname).getClientReference().send(message);
        }
    }

    /**
     * Called to add a user to a lobby in spectator-mode.
     * If the lobby is in a game, the client is added to the spectators and
     * gets an initial update of the current game state.
     * @param nickname Nickname of the client to add to spectators
     * @param lobbyName the lobby to join in as a spectator
     */
    public void joinSpectating(final String nickname, final String lobbyName) {
        if (alreadyInLobby(nickname) == null && lobbyExists(lobbyName)) {
            lobbies.get(lobbyName).addSpectator(nickname);
            clientsReference.get(nickname).getClientReference().send(new String[]{
                    ClientProtocol.YAIL.toString(), lobbyName});
            // initial update for joined client
            initialLobbyUpdate(lobbyName, nickname);
            // Inform Client that it spectates
            clientsReference.get(nickname).getClientReference().send(new String[]{
                    ClientProtocol.YASP.toString()});
        } else if (clientsReference.nicknameExists(nickname)) {
            String[] message = new String[]{ClientProtocol.FTJL.toString()};
            clientsReference.get(nickname).getClientReference().send(message);
        }
    }

    /**
     * Updates the joining client with all current players in the lobby that he joined in
     * and their status.
     *
     * @param lobby    The lobby to update and get data from
     * @param nickname nickname of the player that joined
     */
    public void initialLobbyUpdate(final String lobby, final String nickname) {
        for (String player : lobbies.get(lobby).getPlayers()) {
            if (!player.equals(nickname)) {
                clientsReference.get(nickname).getClientReference().send(
                        new String[]{ClientProtocol.ACJL.toString(), player}
                );
                if (lobbies.get(lobby).getPlayerStatus(player)) {
                    clientsReference.get(nickname).getClientReference().send(
                            new String[]{ClientProtocol.ACCS.toString(), player}
                    );
                }
            }
        }
    }

    /**
     * Removes a Player from a Lobby properly.
     * Manages the deletion of the Lobby when the Lobby would be empty.
     *
     * @param nickname  nickname of the client to remove from the Lobby
     * @param lobbyname The Lobby to remove from
     */
    public void leaveLobby(final String nickname, final String lobbyname) {
        // lobby exists and client in it
        if (lobbyExists(lobbyname) && lobbies.get(lobbyname).inLobby(nickname)
                && !lobbies.get(lobbyname).isSpectator(nickname)) {
            clientsReference.broadcastServer(new String[]{
                    ClientProtocol.ALCS.toString(), lobbyname,
                    "", String.valueOf(
                    lobbies.get(lobbyname).numPlayers() - 1)});
            // if lobby is not empty after the player leaves
            if (!(lobbies.get(lobbyname).numPlayers() - 1 == 0)) {
                lobbies.get(lobbyname).removePlayer(nickname);
                broadcastLobby(lobbyname, new String[]{
                        ClientProtocol.ACLL.toString(), nickname});
            } else {
                // lobby would be empty -> delete
                lobbies.get(lobbyname).removePlayer(nickname);
                deleteLobby(lobbyname);
            }
            clientsReference.get(nickname).getClientReference().send(
                    new String[]{ClientProtocol.YLTL.toString()});
        } else if (lobbyExists(lobbyname)
                && lobbies.get(lobbyname).isSpectator(nickname)) {
            lobbies.get(lobbyname).removeSpectator(nickname);
            broadcastLobby(lobbyname, new String[]{
                    ClientProtocol.ACLL.toString(), nickname});
            clientsReference.get(nickname).getClientReference().send(
                    new String[]{ClientProtocol.YLTL.toString()});
        }
    }

    /**
     * Checks whether a lobby exists.
     *
     * @param lobbyName The Name of the Lobby to check for
     * @return contains lobbyname as a key or not = lobby exists?
     */
    public boolean lobbyExists(final String lobbyName) {
        return lobbies.containsKey(lobbyName);
    }

    /**
     * Send a given command to all Members of the Lobby with the given LobbyID.
     *
     * @param lobbyName the id if the Lobby to broadcast in.
     * @param toSend    the Stringarray tht will be packed and sent as a command.
     */
    public void broadcastLobby(final String lobbyName, final String[] toSend) {
        if (lobbyExists(lobbyName)) {
            for (String clientName : lobbies.get(lobbyName).getPlayers()) {
                clientsReference.get(clientName).getClientReference().send(toSend);
            }
            for (String clientName : lobbies.get(lobbyName).getSpectators()) {
                clientsReference.get(clientName).getClientReference().send(toSend);
            }
        }
    }

    /**
     * Returns a set of all lobby-names.
     *
     * @return set of lobby-names
     */
    public Set<String> getLobbyNames() {
        return lobbies.keySet();
    }

    /**
     * Returns the Lobby-object connected to the given name.
     * Returns null when there is no Lobby with the given Name.
     *
     * @param lobbyName the lobby to get
     * @return Lobby-object
     */
    public Lobby getLobbyReference(final String lobbyName) {
        return lobbies.get(lobbyName);
    }

    /**
     * Deletes a lobby-object from the lobby-list.
     * Just called when the last player leaves!!
     *
     * @param lobbyName name of the lobby to delete
     */
    public void deleteLobby(final String lobbyName) {
        System.out.println("in delete lobby!!!");
        if (lobbyExists(lobbyName)) {
            System.out.println("lobby exists!!!");
            if (getLobbyReference(lobbyName).numPlayers() == 0) {
                System.out.println("num players zero, delete!!!");
                lobbies.remove(lobbyName);
                clientsReference.broadcastServer(new String[]{
                        ClientProtocol.ALDS.toString(), lobbyName});
                NUM_LOBBIES.dec();
            }
        }
    }
}
