package collidingempires.server.ingame;


import collidingempires.client.net.ClientProtocol;
import collidingempires.server.LobbyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class represents the Game itself and uses all other inGame classes.
 */
public class Game {
    private final List<Player> players;

    private final Map world;
    private Player playerActive;
    private Player nextPlayer;
    private int turnCounter;
    private Random random = new Random();
    private static final Logger LOGGER = LogManager.getLogger(Game.class.getName());

    /**
     * Creates a new Game, declares the first player, initializes turnCounter.
     *
     * @param players Array of player names (max 4).
     */
    public Game(String[] players) {
        this.world = new Map();
        this.players = generatePlayerList(players);

        playerActive = this.players.get(0);
        nextPlayer = this.players.get(1);
        turnCounter = 0;
    }

    /**
     * Creates a new Game with a given Map Layout,
     * declares first Player, initializes turnCounter.
     *
     * @param players list of up to 4 players.
     * @param map Map String 180 Characters (0 or 1).
     */
    public Game(String[] players, String map) {
        this.world = new Map(map);
        this.players = generatePlayerList(players);

        playerActive = this.players.get(0);
        nextPlayer = this.players.get(1);
        turnCounter = 0;
    }

    /**
     * Generates a Playerlist and assigns the starting Fields to the players.
     * @param nicknames Array of nicknames of the Players (max 4)
     * @return A List of Player Objects
     */
    private List<Player> generatePlayerList(String[] nicknames) {
        //String[] ids = {"41", "49", "157", "146"};
        String[] ids = {"40", "50", "129", "158"};
        for (int i = 0; i < ids.length; i++) {
            int j = Integer.parseInt(ids[i]);
            while (true) {
                ids[i] = String.valueOf(j);
                if (world.existingNeighborsList(ids[i]).size() == 6) {
                    break;
                } else if (i == 0) {
                    j += j % 2 == 0 ? 1 : 19;
                } else if (i == 1) {
                    j += j % 2 == 0 ? -1 : 17;
                } else if (i == 2) {
                    j += j % 2 == 0 ? 1 : -17;
                } else {
                    j += j % 2 == 0 ? -1 : -19;
                }
            }
        }
        int i = 0;
        List<String> allStartFields = new ArrayList<>();
        List<Player> playerList = new ArrayList<>();
        for (String s : nicknames) {
            List<Field> start = new ArrayList<>();
            start.add(world.getField(ids[i]));
            allStartFields.add(ids[i]);
            for (String p : world.existingNeighborsList(ids[i])) {
                start.add(world.getField(p));
                allStartFields.add(p);
            }
            world.addObject(ids[i], new Building("tower"));
            playerList.add(new Player(s, start));
            playerList.get(playerList.size() - 1).setStartingField(ids[i]);
            i++;
        }
        // Generate trees
        for (int j = 0; j < 180; j++) {
            if (random.nextBoolean() && !allStartFields.contains(String.valueOf(j))
                    && world.getExist().toCharArray()[j] == '1') {
                world.addTree(String.valueOf(j));
            }
        }
        return playerList;
    }

    /**
     * Ends the turn for the active player and starts the turn for
     * the next player. Adds 1 to the turn counter.
     */
    public void nextTurn() {
        if (players.size() < 2) {
            nextPlayer = playerActive;
            return;
        }
        for (Field f : playerActive.getOwned()) {
            if (f.getContent() != null && f.getContent() instanceof Unit) {
                ((Unit) f.getContent()).setMoved(false);
            }
        }
        if (turnCounter >= players.size() - 1) {
            nextPlayer.setGold(nextPlayer.getGoldPerRound() + nextPlayer.getGold());
        }

        playerActive = nextPlayer;
        nextPlayer = players.get(
                (players.indexOf(nextPlayer) + 1) % players.size());
        turnCounter++;
    }

    /**
     * Getter for the players field.
     *
     * @return A list of the current players in the Game.
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Getter for the playerActive field.
     *
     * @return Player whose turn it is.
     */
    public Player getPlayerActive() {
        return playerActive;
    }

    /**
     * Getter for the nextPlayer field.
     *
     * @return Player who's up next.
     */
    public Player getNextPlayer() {
        return nextPlayer;
    }

    /**
     * Getter for the Starttower ID.
     *
     * @param player Whose Starttower ID you want.
     * @return ID for the field of the Starttower.
     */
    public String getStartTower(String player) {
        return getPlayer(player).getStartingField();
    }

    /**
     * Returns all trees on Map.
     *
     * @return List of ids.
     */
    public ArrayList<String> getTrees() {
        return world.getTrees();
    }

    /**
     * Getter for the world field.
     *
     * @return The Map of the Game in its current state.
     */
    public Map getWorldObject() {
        return world;
    }

    /**
     * Getter for the turnCounter field.
     *
     * @return The number of the current turn.
     */
    public int getTurnCounter() {
        return turnCounter;
    }

    /**
     * Getter for the world field.
     *
     * @return The Map of the Game in its current state.
     */
    public String getWorld() {
        return world.getExist();
    }

    /**
     * Tells which fields are owned by a given player.
     *
     * @param player The player nickname whose fields you want to know.
     * @return The field IDs of the owned fields as a String array.
     */
    public String[] getOwned(String player) {
        String[] out = new String[getPlayer(player).getOwned().size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = getPlayer(player).getOwned().get(i).getId();
        }
        return out;
    }

    /**
     * Returns the content of a specific field
     *
     * @param id The ID of the field (0 - 179)
     * @return The content of the field.
     */
    public Container getContent(String id) {
        return world.getField(id).getContent();
    }

    /**
     * Tells whether the Game is won, and who won if it's the case.
     *
     * @return The winner if there is one, or null otherwise.
     */
    public String isWon() {
        List<String> still = new ArrayList<>();
        for (Player p : players) {
            if (!p.getOwned().isEmpty()) {
                still.add(p.getNick());
            }
        }
        if (still.size() > 1) {
            return null;
        }
        return still.get(0);
    }

    /**
     * Tells if a Field is owned by a specific player.
     *
     * @param id     The ID of the Field (0 - 179)
     * @param player The player nickname to compare to.
     * @return True if the player owns the Field, false otherwise.
     */
    public boolean isOwned(String id, String player) {
        if (!world.getMap().containsKey(id)) {
            return false;
        }
        boolean out = false;
        if (world.getField(id).getPlayer() != null && world.getField(id)
                .getPlayer().equals(player)) {
            out = true;
        }
        return out;
    }

    /**
     * Tells how much gold a player has at the moment.
     *
     * @param player The nickname of the player whose gold balance you want to know.
     * @return The gold balance of the player.
     */
    public String getBalance(String player) {
        String out = String.valueOf(0);
        for (Player value : players) {
            if (value.getNick().equals(player)) {
                out = String.valueOf(value.getGold());
            }
        }
        return out;
    }

    /**
     * Tells how much gold the player will get in the next round.
     *
     * @param player The nickname of the player whose gold generation you want to know.
     * @return The gold generation per round of the player.
     */
    public String getBalanceChange(String player) {
        String out = String.valueOf(0);
        for (Player value : players) {
            if (value.getNick().equals(player)) {
                out = String.valueOf(value.getGoldPerRound());
            }
        }
        return out;
    }

    /**
     * Tells on which fields it is possible to place a unit or building
     *
     * @param container The unit or building which you want to place.
     * @return The Map as a 2d Array with the attribute playable true if placement is
     * possible
     * and false if it is not possible.
     */
    public String possibleFieldsPlace(Container container) {

        String out;
        List<Integer> possibleIds = new ArrayList<>();
        List<Field> owned = getPlayerActive().getOwned();
        if (container instanceof Unit) {
            for (Field field : owned) {
                if (!(field.getContent() instanceof Building) && field.getLevel() < 4) {
                    possibleIds.add(Integer.parseInt(field.getId()));
                }
            }
        } else {
            for (Field field : owned) {
                if (field.getContent() == null) {
                    possibleIds.add(Integer.parseInt(field.getId()));
                }
            }
        }
        out = listToString(possibleIds);
        return out;
    }

    /**
     * Converts a List of Integers(representing Ids) to a String to send to Client.
     * @param list List of Integers where the Integers are Ids (range 0 - 179)
     * @return a String representing the map
     */

    public String listToString(List<Integer> list) {
        StringBuilder out = new StringBuilder(180);
        for (int i = 0; i < 180; i++) {
            if (list.contains(i)) {
                out.append("1");
            } else {
                out.append("0");
            }
        }
        return out.toString();
    }

    /**
     * Checks which fields are possible to move to.
     *
     * @param id id of the field to move from (0 - 179)
     * @return String variant of int[][] where possible fields have value 1
     */
    public String possibleFieldsMove(String id) {
        List<Integer> possible;
        String out;
        possible = world.getPossibleIds(id);
        out = listToString(possible);
        return out;
    }

    /**
     * Checks if a Move of the knight on the field from to the field to
     * is possible.
     *
     * @param from Id of initial field (0 - 179)
     * @param to   Id of destination (0 -179)
     * @return ? is move possible
     */
    public boolean isPossibleMove(String from, String to) {
        return (world.getPossibleIds(from).contains(Integer.parseInt(to)));
    }

    /**
     * Get Player Object for given Nickname.
     *
     * @param nick nickname of player
     * @return null or playerobject
     */
    public Player getPlayer(String nick) {
        for (Player p : players) {
            if (p.getNick().equals(nick)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Removes all units and buildings of the leaving player
     * and removes ownership of the fields.
     *
     * @param lobby  lobby of current game.
     * @param lobbys Manager of Lobbies.
     * @param player Nickname of the leaving player.
     */
    public void playerLeft(String player, LobbyManager lobbys, String lobby) {
        List<Field> toRemove = getPlayer(player).getOwned();
        String id;
        for (Field f : toRemove) {
            id = f.getId();
            lobbys.broadcastLobby(lobby, new String[]{
                    ClientProtocol.FCOW.toString(), id, "default"});
            if (getContent(id) != null) {
                lobbys.broadcastLobby(lobby,
                        new String[]{ClientProtocol.OOMR.toString(), id});
            }
            world.resetField(id);
        }

        players.remove(getPlayer(player));
        nextPlayer = players.get(
                (players.indexOf(playerActive) + 1) % players.size());
    }

    /**
     * If possible adds a new unit or building to the specified Field
     * by the active player.
     *
     * @param id        The field ID of the chosen field. (0 - 179)
     * @param container Object that the active player wants to buy. (Unit or Building)
     */
    public void buyContainer(String id, Container container) {
        if (!world.getMap().containsKey(id)) {
            return;
        }
        String possible = possibleFieldsPlace(container);
        if (possible.charAt(Integer.parseInt(id)) == '1') {
            world.addObject(id, container);
            if (container instanceof Building
                    && ((Building) container).getType().equals("farm")) {
                playerActive.setGold(playerActive.getGold()
                        - (container.getCost() + playerActive.getFarms() * 2));
                playerActive.incrementFarms();
            } else {
                playerActive.setGold(playerActive.getGold() - container.getCost());
            }
        }
    }

    /**
     * Moves a unit to the specified field if possible.
     *
     * @param toId Id of destination (0 - 179)
     * @param fromId Id of initial field (0 - 179)
     */
    public void moveUnit(String toId, String fromId) {

        //Check if Id are valid and initial field has a Unit
        if (!world.getMap().containsKey(toId)
                || !world.getMap().containsKey(fromId)) {
            return;
        } else if (world.getField(fromId).getContent() == null
                || !(world.getField(fromId).getContent() instanceof Unit)) {
            return;
        }


        Unit unit = (Unit) world.getField(fromId).getContent();
        if (world.getField(toId).getContent() instanceof Building
                && ((Building) world.getField(toId)
                .getContent()).getType().equals("farm")) {
            getPlayer(world.getField(toId).getPlayer()).decrementFarms();
        }

        world.removeContent(toId);
        world.addObject(toId, unit);
        world.removeContent(fromId);


        if (!playerActive.getNick().equals(world.getField(toId).getPlayer())) {
            if (!world.getField(toId).getPlayer().equals("")) {
                List<Field> other = getPlayer(world.getField(toId)
                        .getPlayer()).getOwned();
                other.remove(world.getField(toId));
            }
            world.setPlayer(toId, playerActive.getNick());
            playerActive.addOwned(world.getField(toId));
            world.getField(toId).setPlayer(playerActive.getNick());

        }

    }

}