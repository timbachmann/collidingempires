package collidingempires.client.util;

/**
 * Class representing a lobby list item.
 */
public class LobbyListItem {

    private static int idCount = 0;
    private final int id;
    private final String name;
    private int playerCount;

    /**
     * Constructor initialising the variables of an item.
     *
     * @param name Item name.
     * @param playerCount Item playerCount.
     */
    public LobbyListItem(String name, int playerCount) {
        id = idCount;
        this.name = name;
        this.playerCount = playerCount;
        idCount++;
    }

    /**
     * Getter for id.
     *
     * @return Id.
     */
    public int getId() {
        return id;
    }

    /**
     * Getter for name.
     *
     * @return name.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for playerCount.
     *
     * @return playerCount.
     */
    public int getPlayerCount() {
        return playerCount;
    }
}
