package collidingempires.server.ingame;

import java.util.List;

/**
 * This class represents a player in the {@link Game}.
 */
public class Player {
    private List<Field> owned;
    private int gold;
    private String nick;
    private String startingField;
    private int farms;

    /**
     * Creates a new player with a starting gold of 15 and a
     * Townhall.
     *
     * @param nick  specifies the name of the player.
     * @param start specifies owned fields at the start of the game.
     */
    public Player(String nick, List<Field> start) {
        this.nick = nick;
        this.gold = 100;
        this.farms = 0;
        owned = start;
        for (Field f : owned) {
            f.setPlayer(nick);
        }
    }

    /**
     * The Setter for the startingfield field.
     *
     * @param startingField The startingfield of the player.
     */
    public void setStartingField(String startingField) {
        this.startingField = startingField;
    }

    /**
     * The Getter for the startingfield field.
     *
     * @return The startingfield of the player.
     */
    public String getStartingField() {
        return startingField;
    }

    /**
     * Setter for the gold field.
     *
     * @param gold Amount to which the gold field is set.
     */
    public void setGold(int gold) {
        this.gold = gold;
    }

    /**
     * Getter for the gold Field.
     *
     * @return How much gold the player currently has.
     */
    public int getGold() {
        return gold;
    }

    /**
     * Getter for the owned field.
     *
     * @return A list with all the units and buildings owned by the player.
     */
    public List<Field> getOwned() {
        return owned;
    }

    /**
     * Getter for the nick field.
     *
     * @return The players nickname.
     */
    public String getNick() {
        return nick;
    }

    /**
     * Adds a field to the list of fields owned by the player
     *
     * @param field field which is added.
     */
    public void addOwned(Field field) {
        owned.add(field);
    }

    /**
     * Getter for the gold production of owned buildings and units.
     *
     * @return Amount of gold the player's fields produce or consume.
     */
    public int getGoldPerRound() {
        int out = 0;
        for (Field field : owned) {
            out = out + field.getGold();
        }
        return out;
    }

    /**
     * Gets the number of Farms owned by the Player.
     * @return Number of Farms
     */
    public int getFarms() {
        return this.farms;
    }

    /**
     * Increases the number of Farms of the Player by 1.
     */
    public void incrementFarms() {
        this.farms++;
    }

    /**
     * decreases the number of Farms of the Player by 1.
     */
    public void decrementFarms() {
        this.farms--;
    }
}
