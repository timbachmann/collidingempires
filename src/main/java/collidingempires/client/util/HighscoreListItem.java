package collidingempires.client.util;

/**
 * Class representing a member list item.
 */
public class HighscoreListItem {

    private static int idCount = 0;
    private final int id;
    private final String name;
    private final int score;

    /**
     * Constructor initialising the variables of an item.
     *
     * @param name Item name.
     * @param score Item score.
     */
    public HighscoreListItem(String name, int score) {
        id = idCount;
        this.name = name;
        this.score = score;
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
     * Getter for time.
     *
     * @return score.
     */
    public int getTime() {
        return score;
    }
}
