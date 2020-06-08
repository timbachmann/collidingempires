package collidingempires.server;

import collidingempires.server.ingame.Building;
import collidingempires.server.ingame.Container;
import collidingempires.server.ingame.Field;
import collidingempires.server.ingame.Game;
import collidingempires.server.ingame.Player;
import collidingempires.server.ingame.Unit;
import collidingempires.server.net.ClientOnServer;
import collidingempires.server.net.Packager;
import collidingempires.server.net.ServerProtocol;
import collidingempires.client.net.ClientProtocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.prometheus.client.Summary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * An Executor Manages one Client on the Server, it
 * executes its Orders and serves as an API to Access it.
 * As a Thread it Reads from the {@link ClientOnServer} object of
 * the client and executes/validates incoming orders.
 */
public class Executor extends Thread {

    private LobbyManager clientLobbies;
    private ClientOnServer client;
    private ClientManager clientsReference;
    private Random random = new Random();
    private Thread timer;
    public volatile boolean turnFinished = false;
    private volatile boolean running;
    private static final Logger LOGGER =
            LogManager.getLogger(Executor.class.getName());
    private static final Summary REQUEST_LATENCY = Summary.build()
            .name("request_latency")
            .help("The Time the server needs to process the request")
            .register();

    /**
     * Constructor. Assigns a client to the Executor-object and sets the
     * Reference of the {@link ClientManager} of the Server.
     *
     * @param clientToExecute The Client, that the Executor should hold and manage.
     * @param clients         Reference of the server's {@link ClientManager}
     * @param lobbies         the Lobbymanager of the Server
     */
    public Executor(ClientOnServer clientToExecute, ClientManager clients,
                    LobbyManager lobbies) {
        this.client = clientToExecute;
        this.clientsReference = clients;
        this.clientLobbies = lobbies;
        this.running = true;
    }

    /**
     * Inner class to time the rounds.
     * Automatically calls finishturn() after time is over.
     */
    private class Timer implements Runnable {
        private int time;

        /**
         * Constructor of the Timer, takes start-time.
         *
         * @param startTime time in seconds to time.
         */
        Timer(final int startTime) {
            this.time = startTime;
        }

        /**
         * will run in Thread.
         * Counts from start-time to zero in seconds and broadcast this to
         * all clients in the game.
         */
        public void run() {
            for (int i = time; i >= 0; i--) {
                if (turnFinished) {
                    return;
                } else {
                    clientLobbies.broadcastLobby(lobbyName(), new String[]{
                            ClientProtocol.LTOT.toString(), String.valueOf(i)});
                    try {
                        int duration = 1000;
                        Thread.sleep(duration);
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage() + "\n-->finish turn!");
                        finishTurn(lobbyName());
                    }
                }
            }
            if (!turnFinished && game() != null) {
                finishTurn(lobbyName());
            }
        }
    }

    /**
     * Start The Timer Thread for this executor.
     */
    public void startTimer() {
        turnFinished = false;
        this.timer = new Thread(new Timer(50));
        this.timer.start();
    }

    /**
     * Returns current Lobby of the client.
     * null when not in a Lobby.
     *
     * @return lobby or null if not in a lobby.
     */
    public String lobbyName() {
        return clientLobbies.alreadyInLobby(clientName());
    }

    /**
     * Returns a Reference to the current Lobby of the Client.
     *
     * @return The Lobby Object.
     */
    public Lobby lobby() {
        return clientLobbies.getLobbyReference(lobbyName());
    }

    /**
     * Returns the current Gameobject.
     *
     * @return Gameobject or null.
     */
    public Game game() {
        if (lobby() != null) {
            return lobby().getGame();
        } else {
            return null;
        }
    }


    /**
     * returns nickname of the Client.
     *
     * @return nickname
     */
    public String clientName() {
        return this.client.getNickname();
    }

    /**
     * Will be executed in a Thread: constantly executes the next order.
     * Runs as long as the terminate() is called or an IOException occurs.
     */
    public void run() {
        System.out.println(clientName() + " is running");
        LOGGER.info("Executor of Client: "
                + clientName() + " is running");
        while (this.running) {
            try {
                this.executeNext();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                clientsReference.closeConnection(clientName());
            }
        }
    }

    /**
     * Sets the running-flag to false. This leads to a stop of the Thread
     * after the current execution. This Method is called by The clientManager to
     * end a connection.
     *
     * @throws IOException thrown when getClientReference().stop() throws it
     */
    public void terminate() throws IOException {
        this.turnFinished = true;
        this.running = false;
        turnFinished = true;
        this.getClientReference().stop();
    }

    /**
     * Takes the next order from the InputBuffer of the Socket of the Client
     * and tries to execute it by calling execute(). The time is observed by prometheus.
     *
     * @throws IOException the orders.take() waits till there is something to get.
     */
    public void executeNext() throws IOException {
        String[] order = client.receive();
        if (order != null) {
            Summary.Timer reqTimer = REQUEST_LATENCY.startTimer();
            this.execute(order);
            reqTimer.observeDuration();
        } else {
            LOGGER.trace("invalid order!");
        }
    }

    /**
     * Returns an ArrayList with all clients using this executor.
     *
     * @return the clients field
     */
    public ClientOnServer getClientReference() {
        return this.client;
    }

    /**
     * contains the switch case that calls functionality depending
     * on the order and its arguments. This Method gets the sequence
     * with the right amount of arguments. This is made sure by
     * the {@link ClientOnServer}'s receive Method that is called by
     * the executeNext method.
     *
     * @param order the sequence that should be executed and validated
     */
    public void execute(final String[] order) {
        ServerProtocol sequence = ServerProtocol.valueOf(order[0]);
        LOGGER.info(clientName() + " execute: " + Packager.pack(order));
        switch (sequence) {
            case CWJL:
                clientLobbies.joinLobby(clientName(), order[1]);
                break;
            case CWCL:
                clientLobbies.createLobby(order[1]);
                break;
            case CWLS:
                LOGGER.info("Client Wants to Leave Server");
                clientsReference.closeConnection(clientName());
                break;
            case CWST:
                LOGGER.info("Client " + clientName() + " wants to send text!");
                sendText(order);
                break;
            case CWCN:
                LOGGER.info("Client " + clientName() + ": wants to change nickname!");
                clientsReference.changeNickname(clientName(),
                        order[1]);
                break;
            case CWTR:
                LOGGER.info("Client " + clientName() + ": toggle Ready!");
                toggleReadyStatus();
                break;
            case CWLL:
                LOGGER.info("Client " + clientName() + ": wants to leave lobby!");
                leaveLobby();
                break;
            case CWFT:
                LOGGER.info("Client " + clientName() + ": wants to finish turn!");
                if (lobby() != null && lobby().isInGame()
                        && lobby().getGame().getPlayerActive()
                        .getNick().equals(clientName())) {
                    finishTurn(lobbyName());
                }
                break;
            case CWRB:
                LOGGER.info("Client " + clientName() + ": requests fields to build on!");
                requestFields(order[1]);
                break;
            case CWRM:
                LOGGER.info("Client " + clientName() + ": requests fields to move on !");
                requestFieldsMove(order[1]);
                break;
            case CWPI:
                LOGGER.info("Client " + clientName() + ": wants to place Item !");
                placeItem(order[1], order[2]);
                break;
            case CWTM:
                LOGGER.info("Client " + clientName() + ": wants to move a knight!");
                moveKnight(order[1], order[2]);
                break;
            case CWTC:
                //add 100 gold to active player
                LOGGER.info("Client " + clientName() + ": wants to cheat!");
                cheat(order[1]);
                break;
            case CWTS:
                LOGGER.info("Client " + clientName() + ": wants to spectate!");
                spectate(order[1]);
                break;
            default:
                System.out.println("Invalid Order!");
                LOGGER.error(Packager.pack(order) + " is an invalid order");
        }
    }

    /**
     * Joins a Lobby to spectate.
     * Checks if you are in a lobby since you cant spectate if you are.
     * @param lobbyName The name of the lobby to join to spectate.
     */
    private void spectate(String lobbyName) {
        if (lobby() == null) {
            clientLobbies.joinSpectating(clientName(), lobbyName);
            gameUpdate();
            client.send(new String[]{ClientProtocol.NPOT.name(),
                    game().getPlayerActive().getNick()});
        }
    }

    /**
     * Adds 100 Gold, removes all knights as punishment.
     * Checks whether the player is on turn and game is running.
     */
    private void cheat(String cheatCode) {
        if (cheatCode.equals("0")) {
            // Lobby exists and Game is running
            if (lobby() != null && lobby().isInGame()) {
                Player player = game().getPlayerActive();
                // I am on turn
                if (player.getNick().equals(this.clientName())) {
                    player.setGold(player.getGold() + 100);
                    //player loses all his knights
                    for (Field t : player.getOwned()) {
                        String id = t.getId();
                        if (t.getContent() instanceof Unit) {
                            clientLobbies.broadcastLobby(lobby().getLobbyName(),
                                    new String[]{ClientProtocol.OOMR.toString(), id});
                            game().getWorldObject().removeContent(id);
                        }
                    }
                    updateGold(player);
                    updateIncome(player);
                    // finish turn for active player
                    finishTurn(lobbyName());
                    // inform the lobby
                    clientLobbies.broadcastLobby(lobbyName(),
                            new String[]{ClientProtocol.TXTR.toString(),
                                    "lobby", "***BIGBROTHER***", clientName()
                                    + " cheated!!"});
                }
            }
        } else if (cheatCode.equals("1")) {
            if (lobby() != null && lobby().isInGame()) {
                Player player = game().getPlayerActive();
                boolean noFarm = true;
                if (player.getNick().equals(this.clientName())) {
                    for (Player p: game().getPlayers()) {
                        if (!p.getNick().equals(this.clientName())) {
                            for (Field t : p.getOwned()) {
                                String id = t.getId();
                                if (t.getContent() instanceof Building
                                        && ((Building) t.getContent())
                                            .getType().equals("farm")) {
                                    clientLobbies.broadcastLobby(lobby().getLobbyName(),
                                            new String[]{ClientProtocol.OOMR.toString(),
                                                    id});
                                    game().getWorldObject().removeContent(id);
                                    noFarm = false;
                                    break;
                                }
                            }
                            updateIncome(p);
                        }
                    }
                    if (!noFarm) {
                        player.setGold(player.getGold() - 50);
                        updateGold(player);
                        finishTurn(lobbyName());
                        clientLobbies.broadcastLobby(lobbyName(),
                                new String[]{ClientProtocol.TXTR.toString(),
                                        "lobby", "***BIGBROTHER***", clientName()
                                        + " cheated!!"});
                    }
                }
            }
        }
    }

    /**
     * Takes the valid CWTS Sequence and sends the text according
     * to the given arguments of the sequence.
     *
     * @param order the sequence to execute
     */
    private void sendText(final String[] order) {
        // send message to all clients on Server
        if (order[1].equals("server")) {
            clientsReference.broadcastServer(new String[]{
                    ClientProtocol.TXTR.name(),
                    order[1], clientName(), order[2]});
            client.send(new String[]{ClientProtocol.TCMS.name(),
                    "success"});
            LOGGER.trace("Text sent to all clients on the server");
            // send message to clients in the same Lobby
        } else if (order[1].equals("lobby")
                && clientLobbies.alreadyInLobby(clientName()) != null) {
            clientLobbies.broadcastLobby(clientLobbies.alreadyInLobby(clientName()),
                    new String[]{
                            ClientProtocol.TXTR.name(),
                            order[1], clientName(), order[2]});
            client.send(new String[]{ClientProtocol.TCMS.name(),
                    "success"});
            LOGGER.trace("Text sent to Lobby");
            // send message to specific user
        } else if (order[1].startsWith("@")) {
            String[] split = order[1].split("@");
            if (clientsReference.nicknameExists(split[1])) {
                clientsReference.get(split[1]).getClientReference().send(
                        new String[]{ClientProtocol.TXTR.name(),
                                "private", clientName(), order[2]});
                client.send(new String[]{ClientProtocol.TCMS.name(),
                        "success"});
                LOGGER.trace("Text sent to " + split[1]);
            } else {
                client.send(new String[]{ClientProtocol.TCMS.name(),
                        "fail"});
                LOGGER.info("Failed to send Text to " + split[1]);
            }
        } else {
            client.send(new String[]{ClientProtocol.TCMS.name(), "fail"});
            LOGGER.error("First argument is not valid");
        }
    }

    /**
     * All connected clients, all lobbies, all finished Games,
     * all Highscorelistentries.
     */
    public void initialUpdate() {
        LOGGER.info(clientName() + "- initial update!");
        for (String s : clientsReference.getNicknames()) {
            if (!clientName().equals(s)) {
                LOGGER.info("Initial update names: " + s);
                getClientReference().send(new String[]{ClientProtocol.NCON.toString(),
                        s});
            }
        }
        for (String s : clientLobbies.getLobbyNames()) {
            LOGGER.info("Initial update Lobbys: " + s);
            Lobby lobby = clientLobbies.getLobbyReference(s);
            getClientReference().send(new String[]{ClientProtocol.NLOS.toString(),
                    s, String.valueOf(lobby.numPlayers())});
            if (lobby.isInGame()) {
                getClientReference().send(new String[]{ClientProtocol.ALCS.toString(),
                        s, String.valueOf(true), ""});
            }
        }
        // send all finished games
        for (LobbyManager.FinishedGame f : clientLobbies.getFinishedGames()) {
            getClientReference().send(new String[]{ClientProtocol.NFGA.toString(),
                    f.lobby, f.winner});
        }
        // send all high score entries
        HashMap<String, Integer> scores = clientsReference.getScores();
        for (String name : scores.keySet()) {
            getClientReference().send(new String[]{ClientProtocol.CHSE.toString(),
                    name, String.valueOf(scores.get(name))});
        }
    }

    /**
     * Switches the Game-Ready-Status of a Client in a Lobby.
     * Ignores the Order when client is not in a lobby.
     * Starts the Game when all clients are ready and when there are 2,3 or 4
     * players in the Lobby.
     */
    private void toggleReadyStatus() {
        String myLobby = lobbyName();
        if (myLobby != null && !clientLobbies.getLobbyReference(myLobby).isInGame()) {
            clientLobbies.getLobbyReference(myLobby).toggleReady(clientName());
            clientLobbies.broadcastLobby(myLobby, new String[]{
                    ClientProtocol.ACCS.toString(), clientName()});
            Lobby lobby = clientLobbies.getLobbyReference(myLobby);
            if (lobby.allReady() && lobby.numPlayers() >= 2) {
                clientLobbies.broadcastLobby(myLobby, new String[]{
                        ClientProtocol.TGST.toString()});
                lobby.startGame();
                // Broadcast who's first
                String firstPlayer = game().getPlayerActive().getNick();
                clientLobbies.broadcastLobby(lobbyName(),
                        new String[]{ClientProtocol.NPOT.toString(), firstPlayer});
                clientsReference.get(firstPlayer).startTimer();
                // Broadcast the Server that the Lobby is inGame
                clientsReference.broadcastServer(new String[]{
                        ClientProtocol.ALCS.toString(), myLobby,
                        "true", ""});
            }
        }
    }

    /**
     * Updates the client about the Gamestatus.
     * All Fields are set to the right, Units and trees are placed.
     * Expects an empty map!
     */
    public void gameUpdate() {
        client.send(new String[]{ClientProtocol.NEMA.toString(), game().getWorld()});
        for (String name : lobby().getPlayers()) {
            String[] owned = game().getOwned(name);
            // set fields and place objects
            for (String id : owned) {
                client.send(new String[]{
                        ClientProtocol.FCOW.toString(), id, name});
                if (game().getWorldObject()
                        .getField(id).getContent() != null) {
                    Container container = game().getWorldObject()
                            .getField(id).getContent();
                    if (container instanceof Unit) {
                        client.send(new String[]{
                                ClientProtocol.NOOM.toString(), id, "knight",
                                String.valueOf(container.getLevel())});
                    } else if (container instanceof Building) {
                        if (((Building) container).getType().equals("farm")) {
                            client.send(new String[]{
                                    ClientProtocol.NOOM.toString(),
                                    id, "farm", "1"});
                        } else if (((Building) container).getType()
                                .equals("tower")) {
                            client.send(new String[]{
                                    ClientProtocol.NOOM.toString(),
                                    id, "tower", "1"});
                        }
                    }
                }
            }

            // Set trees
            for (String id : game().getTrees()) {
                // set trees
                client.send(new String[]{ClientProtocol.NOOM.toString(),
                                id, "tree", "1"});
            }
        }
    }

    /**
     * Inform all Players who's next, update Game logic via nextTurn method,
     * Sets the new Balance of the new active Player.
     *
     * @param lobby Name of the lobby to finish turn for
     */
    public void finishTurn(String lobby) {
        // if its your turn
        Game game = clientLobbies.getLobbyReference(lobby).getGame();
        placeRandomTree(game);
        turnFinished = true;
        if (game.getNextPlayer().getOwned().isEmpty()) {
            game.nextTurn();
        }
        if (game.getNextPlayer() != null && game.isWon() == null) {
            Player nextPlayer = game.getNextPlayer();
            game.nextTurn();
            int nextGold = nextPlayer.getGold();
            if (nextGold < 0) {
                for (Field t : nextPlayer.getOwned()) {
                    String id = t.getId();
                    if (t.getContent() instanceof Unit) {
                        clientLobbies.broadcastLobby(lobby,
                                new String[]{ClientProtocol.OOMR.toString(), id});
                        game().getWorldObject().removeContent(id);
                    }
                }
                updateIncome(nextPlayer);
            }
            clientLobbies.broadcastLobby(lobby, new String[]{
                    ClientProtocol.NPOT.toString(), nextPlayer.getNick()});
            updateGold(nextPlayer);
            resetMoved(nextPlayer);
            // just start new timer if game is not over
            clientsReference.get(nextPlayer.getNick()).startTimer();
        }
    }

    /**
     * called to leave the current Lobby if client is in one.
     * Also Updates the Game logic ex finishes Turn when.
     */
    public void leaveLobby() {
        // if in lobby
        if (lobby() != null) {
            clientLobbies.leaveLobby(clientName(), lobbyName());
        }
    }

    /**
     * Queries the Gamelogic for possible fields to place a building or a knight,
     * and sends the possible fields as a map of booleans.
     *
     * @param objectToBuy tower/knight
     */
    public void requestFields(String objectToBuy) {
        if (game().getPlayerActive().getNick().equals(clientName())) {
            switch (objectToBuy) {
                case "tower":
                    getClientReference().send(new String[]{ClientProtocol.PFOR.toString(),
                            (game().possibleFieldsPlace(new Building("tower")))});
                    break;
                case "knight":
                    getClientReference().send(new String[]{ClientProtocol.PFOR.toString(),
                            (game().possibleFieldsPlace(new Unit()))});
                    break;
                case "farm":
                    getClientReference().send(new String[]{ClientProtocol.PFOR.toString(),
                            (game().possibleFieldsPlace(new Building("farm")))});
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Returns possible fields to move on for a unit on field with id.
     *
     * @param id id of the field.
     */
    public void requestFieldsMove(String id) {
        if (game().getContent(id) != null && game().isOwned(id, clientName())
                && game().getPlayerActive().getNick().equals(clientName())) {
            getClientReference().send(new String[]{ClientProtocol.PFOR.toString(),
                    game().possibleFieldsMove(id)});
        }
    }

    /**
     * Place an Unit or a building on a field if possible and updates all players in
     * the Lobby.
     * Updates a knight when a knight is already on the field and its not max Level.
     *
     * @param what  what to build: tower/farm/knight
     * @param where where: fieldId
     */
    public void placeItem(String what, String where) {
        if (game().isOwned(where, clientName())
                && game().getPlayerActive().getNick().equals(clientName())) {
            switch (what) {
                case "tower":
                    // enough money, field empty?
                    if (game().getContent(where) == null
                            && Building.getTOWERCOST() <= game()
                            .getPlayerActive().getGold()) {
                        game().buyContainer(where, new Building("tower"));
                        getClientReference().send(
                                new String[]{ClientProtocol.YCBC.toString(),
                                        String.valueOf(game().getPlayerActive()
                                                .getGold())});
                        clientLobbies.broadcastLobby(lobbyName(),
                                new String[]{ClientProtocol.NOOM.toString(), where,
                                        "tower", "1"});
                    }
                    break;
                case "farm":
                    // enough money, field empty?
                    int farmCost =
                            Building.getFARMCOST()
                                    + game().getPlayerActive().getFarms() * 2;
                    if (game().getContent(where) == null
                            && farmCost <= game().getPlayerActive().getGold()) {
                        game().buyContainer(where, new Building("farm"));
                        getClientReference().send(
                                new String[]{ClientProtocol.YCBC.toString(),
                                        String.valueOf(game().getPlayerActive()
                                                .getGold())});
                        getClientReference().send(
                                new String[]{ClientProtocol.BCNR.toString(),
                                        String.valueOf(
                                                game().getPlayerActive()
                                                        .getGoldPerRound())});
                        clientLobbies.broadcastLobby(lobbyName(),
                                new String[]{ClientProtocol.NOOM.toString(), where,
                                        "farm", "1"});
                    }
                    break;
                case "knight":
                    Container content = game().getContent(where);
                    if (content == null) {
                        if (Unit.cost() <= game().getPlayerActive().getGold()) {
                            game().buyContainer(where, new Unit());
                            getClientReference().send(
                                    new String[]{ClientProtocol.YCBC.toString(),
                                            String.valueOf(game().getPlayerActive()
                                                    .getGold())});
                            getClientReference().send(
                                    new String[]{ClientProtocol.BCNR.toString(),
                                            String.valueOf(
                                                    game().getPlayerActive()
                                                            .getGoldPerRound())});
                            clientLobbies.broadcastLobby(lobbyName(),
                                    new String[]{ClientProtocol.NOOM.toString(), where,
                                            "knight", "1"});
                        }
                    } else if ((content instanceof Unit)
                            && (content.getLevel() < Unit.getMaxLevel())) {
                        if (content.getCost() <= game().getPlayerActive().getGold()) {
                            game().buyContainer(where, new Unit());
                            getClientReference().send(
                                    new String[]{ClientProtocol.YCBC.toString(),
                                            String.valueOf(game()
                                                    .getPlayerActive().getGold())});
                            getClientReference().send(
                                    new String[]{ClientProtocol.BCNR.toString(),
                                            String.valueOf(game().getPlayerActive()
                                                    .getGoldPerRound())});
                            clientLobbies.broadcastLobby(lobbyName(),
                                    new String[]{ClientProtocol.OOMR.toString(), where});
                            clientLobbies.broadcastLobby(lobbyName(),
                                    new String[]{ClientProtocol.NOOM.toString(), where,
                                            "knight",
                                            String.valueOf(content.getLevel())});
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Checks if move is possible and executes the move in the gamelogic and informs
     * all clients.
     * ALso checks if the Game is won after this move.
     *
     * @param fromId ID of the field where the knight to move is.
     * @param toID   ID of the field to move to
     */
    public void moveKnight(String fromId, String toID) {
        // if its your turn and you own the field
        if (game().getPlayerActive().getNick().equals(clientName()) && game()
                .isOwned(fromId, clientName())) {
            // if there is a knight on the field to move and the move is possible
            if (game().getContent(fromId) instanceof Unit && game()
                    .isPossibleMove(fromId, toID)) {
                // if unit hasnt been moved already
                if (!((Unit) game().getContent(fromId)).isMoved()) {
                    // if there is something on the field to move on remove it
                    if (game().getContent(toID) != null) {
                        clientLobbies.broadcastLobby(lobbyName(),
                                new String[]{ClientProtocol.OOMR.toString(), toID});
                    }
                    // remove knight from old position
                    clientLobbies.broadcastLobby(lobbyName(),
                            new String[]{ClientProtocol.OOMR.toString(), fromId});

                    // place new knight and change owner of field
                    clientLobbies.broadcastLobby(lobbyName(),
                            new String[]{ClientProtocol.FCOW.toString(), toID,
                                    clientName()});
                    clientLobbies.broadcastLobby(lobbyName(),
                            new String[]{ClientProtocol.NOOM.toString(), toID,
                                    "knight",
                                    String.valueOf(game().getContent(fromId)
                                            .getLevel())});

                    String playerOwned =
                            game().getWorldObject().getField(toID).getPlayer();
                    Player activePlayer = game().getPlayerActive();
                    game().moveUnit(toID, fromId);

                    //adjust balanceChange of active Player
                    updateIncome(activePlayer);

                    // if field to move on was owned the balance of this player is changed
                    if (!playerOwned.equals("")) {
                        updateGold(game().getPlayer(playerOwned));
                        updateIncome(game().getPlayer(playerOwned));
                    }
                    // send game is won message if the move won the game
                    if (game().isWon() != null) {
                        lobby().finishedGame();
                    }
                }
            }
        }
    }

    //*********************** Helpers *********************************

    /**
     * Place random tree on Game board.
     * Just places tree when board is not full.
     * @param game game to place tree in
     */
    private void placeRandomTree(Game game) {
        if (!game.getWorldObject().isFull()) {
            int randomId;
            do {
                randomId = random.nextInt(179);
            } while (game.getWorld().toCharArray()[randomId] != '1'
                    || game.getWorldObject().getMap().get(
                    String.valueOf(randomId)).getContent() != null);
            game.getWorldObject().addTree(String.valueOf(randomId));
            String nick = game.getWorldObject()
                    .getField(Integer.toString(randomId)).getPlayer();
            if (!(nick.equals(""))) {
                clientsReference.get(nick).getClientReference().send(
                        new String[]{ClientProtocol.BCNR.toString(),
                                String.valueOf(game.getPlayer(nick).getGoldPerRound())});
            }

            clientLobbies.broadcastLobby(lobbyName(),
                    new String[]{ClientProtocol.NOOM.toString(),
                            String.valueOf(randomId), "tree", "1"});
        }
    }

    /**
     * Update Gold Balance of a Player.
     * @param player player to change balance of
     */
    public void updateGold(Player player) {
        clientsReference.get(player.getNick()).getClientReference().send(
                new String[]{ClientProtocol.YCBC.toString(),
                        String.valueOf(player.getGold())});
    }

    /**
     * Updates Income per Round of a player.
     * @param player player to change income of
     */
    public void updateIncome(Player player) {
        clientsReference.get(player.getNick()).getClientReference().send(
                new String[]{ClientProtocol.BCNR.toString(),
                        String.valueOf(player.getGoldPerRound())});
    }

    /**
     * Reset the moved-property of all units of a player.
     * @param player the player whom's units to reset
     */
    private void resetMoved(Player player) {
        for (Field f : player.getOwned()) {
            if (f.getContent() != null && f.getContent() instanceof Unit) {
                Unit unit = (Unit) f.getContent();
                unit.setMoved(false);
            }
        }
    }
}
