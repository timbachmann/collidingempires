package collidingempires.server;

import java.util.HashMap;
import java.util.Set;

import collidingempires.client.net.ClientProtocol;
import collidingempires.server.ingame.Game;

/**
 * Represents a lobby on the Server.
 */
public class Lobby {
    private LobbyManager lobbyReference;
    private ClientManager clientReference;
    private String lobbyName;
    private HashMap<String, Boolean> players;
    private HashMap<String, Boolean> spectators;
    private int playerAmount;
    private int spectatorAmount;
    private boolean inGame;
    private Game game;

    /**
     * Constructor, sets LobbyID and initialize the playersarray.
     *
     * @param name     Lobbyname to set.
     * @param manager  Lobbymanager of the Server
     * @param cManager ClientManager
     */
    public Lobby(final String name, final LobbyManager manager,
                 final ClientManager cManager) {
        this.playerAmount = 0;
        this.players = new HashMap<>(4);
        this.spectators = new HashMap<>(4);
        this.lobbyName = name;
        this.inGame = false;
        this.lobbyReference = manager;
        this.clientReference = cManager;
    }

    /**
     * Getter for the lobbyName-Field.
     *
     * @return the current Lobbyname;
     */
    public String getLobbyName() {
        return lobbyName;
    }

    /**
     * Checks whether a player is in this Lobby by its nickname.
     *
     * @param nickname the nickname of the player to look for
     * @return player in lobby or not(true/false)
     */
    public boolean inLobby(final String nickname) {
        return players.containsKey(nickname) || spectators.containsKey(nickname);
    }

    /**
     * Check if player is spectator.
     *
     * @param nickname Name of player.
     * @return True if spectator.
     */
    public boolean isSpectator(final String nickname) {
        return spectators.containsKey(nickname);
    }

    /**
     * Returns the current number of Players in the Lobby.
     *
     * @return Number of Players
     */
    public int numPlayers() {
        return this.playerAmount;
    }

    /**
     * Adds a Players nickname to the players array.
     * Make sure to proof the size of lobby with isFull first.
     *
     * @param nickname the nickname of the player to add.
     */
    public void addPlayer(final String nickname) {
        playerAmount++;
        this.players.put(nickname, false);
    }

    /**
     * Adds a Client to the spectators of the Lobby and
     * increases the amount.
     * Make sure to check to be inGame before adding.
     *
     * @param nickname name of the client to be added.
     */
    public void addSpectator(final String nickname) {
        if (!this.spectators.containsKey(nickname)) {
            spectatorAmount++;
            this.spectators.put(nickname, false);
        }
    }

    /**
     * Removes a Player from the players list if it exists.
     * and removes it from the game if one was active.
     *
     * @param nickname the nickname of the Player to be removed.
     */
    public void removePlayer(final String nickname) {
        if (inLobby(nickname) && players.containsKey(nickname)) {
            players.remove(nickname);
            playerAmount--;
            if (isInGame()) {
                if (game.getPlayerActive().getNick().equals(nickname)) {
                    clientReference.get(nickname).finishTurn(lobbyName);
                }
                game.playerLeft(game.getPlayer(nickname).getNick(),
                        lobbyReference, lobbyName);
                if (game.isWon() != null) {
                    finishedGame();
                }
            }
        }
    }

    /**
     * Removes a client with the given nickname from the
     * spectators list and reduces the spectatorAmount.
     *
     * @param nickname Nickname of the client to be removed
     */
    public void removeSpectator(final String nickname) {
        if (inLobby(nickname) && spectators.containsKey(nickname)) {
            spectators.remove(nickname);
            spectatorAmount--;
        }
    }

    /**
     * Returns the list of players in the lobby.
     *
     * @return the nicknames of all players
     * in the lobby as a Set.
     */
    public Set<String> getPlayers() {
        return this.players.keySet();
    }

    /**
     * Returns a Set of the clientNames of all clients
     * that are spectators in the Lobby.
     *
     * @return the set of names
     */
    public Set<String> getSpectators() {
        return this.spectators.keySet();
    }

    /**
     * Return whether the Lobby is full or not.
     *
     * @return is the lobby full?
     */
    public boolean isFull() {
        return playerAmount > 3;
    }

    /**
     * Changes the game-ready status of a player in the Lobby.
     *
     * @param playerNickname Player whose status should be switched
     */
    public void toggleReady(final String playerNickname) {
        boolean newStatus = !players.get(playerNickname);
        this.players.put(playerNickname, newStatus);
    }

    /**
     * Starts the Game for this Lobby.
     * Is called in the Executor in Order to Start the Game.
     * It calls the game-update for every player and updates its
     * income and balance.
     */
    public void startGame() {
        this.inGame = true;
        this.game = new Game(getPlayers().toArray(new String[0]));
        Set<String> players = getPlayers();
        for (String player: players) {
            Executor ex = clientReference.get(player);
            ex.gameUpdate();
            ex.updateGold(game.getPlayer(player));
            ex.updateIncome(game.getPlayer(player));
        }
    }

    /**
     * Returns whether the Lobby is in a Game or not.
     *
     * @return is in Game?
     */
    public boolean isInGame() {
        return this.inGame;
    }

    /**
     * Returns whether all players in a lobby are ready to play.
     *
     * @return all players ready?
     */
    public boolean allReady() {
        return !players.containsValue(false);
    }

    /**
     * Returns all ready players in the Lobby.
     *
     * @param nickname nickname as key
     * @return Array of Strings of ready Players
     */
    public boolean getPlayerStatus(final String nickname) {
        return players.get(nickname);
    }

    /**
     * Returns an object reference of the Game.
     *
     * @return The Game Object.
     */
    public Game getGame() {
        return this.game;
    }

    /**
     * Called when a Game finished or was aborted.
     * Adds a finished Game to the list or not depending on
     * whether the Game was finished properly or not.
     * Also updates the Highscorelist
     */
    public void finishedGame() {
        String winner = getGame().isWon();
        clientReference.get(winner).turnFinished = true;
        lobbyReference.addFinishedGame(lobbyName, winner);
        lobbyReference.broadcastLobby(lobbyName, new String[]{
                ClientProtocol.GEND.toString(), winner});
        for (String spectator : getSpectators()) {
            removeSpectator(spectator);
            clientReference.get(spectator).getClientReference()
                    .send(new String[]{ClientProtocol.YLTL.name()});
        }
        clientReference.addScoreEntry(winner,
                clientReference.getScore(winner) + 1);
        this.inGame = false;
        this.game = null;
        for (String player : getPlayers()) {
            toggleReady(player);
        }
        for (String player : getPlayers()) {
            lobbyReference.initialLobbyUpdate(lobbyName, player);
        }
    }
}
