package collidingempires.server.ingame;


/**
 * A Field represents the individual parts of the {@link Map}
 * and keeps Game-relevant information about itself.
 */
public class Field {
    private int level;
    private boolean exists;
    private String player;
    private int gold;
    private Container content;
    private String id;


    /**
     * Constructor for a field and setting a boolean true if it exists on the visible map.
     *
     * @param exists true if on map, false if not
     * @param id The Id of the field to set
     */
    public Field(boolean exists, String id) {
        level = 0;
        this.gold = 1;
        this.exists = exists;
        this.player = "";
        this.id = id;
        this.content = null;
    }

    /**
     * Gets the owner of the field.
     *
     * @return String of the owning player
     */
    public String getPlayer() {
        return player;
    }


    /**
     * Assigns a String player to the Field, setting
     * the owner of the Field.
     *
     * @param player Name of the player who owns the Field.
     */
    public void setPlayer(String player) {
        this.player = player;
    }

    /**
     * Gets if the field is on the map or not.
     *
     * @return boolean if on map
     */
    public boolean exists() {
        return this.exists;
    }

    /**
     * Sets the level of the field.
     *
     * @param level desired level of the field
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Gets the level of the field.
     *
     * @return level of the field
     */
    public int getLevel() {
        return level;
    }

    /**
     * Shows how much gold the Field is worth each turn.
     *
     * @return The variable gold.
     */
    public int getGold() {
        return this.gold;
    }

    /**
     * Sets gold production per round.
     *
     * @param gold desired gold production of field per round
     */
    public void setGold(int gold) {
        this.gold = gold;
    }

    /**
     * Adds a unit or building to the content of the Field.
     *
     * @param container Contains the information of the object that is added.
     *                  (Unit or Building)
     */
    public void addContent(Container container) {
        if (!exists()) {
            return;
        }
        if (this.content == null) {
            this.content = container;
            this.setGold(1 + container.getRent());

            if (container.getLevel() > this.getLevel()) {
                this.setLevel(container.getLevel());
            }
        } else if (container instanceof Unit && this.content instanceof Unit) {
            ((Unit) this.content).levelUp();
            this.setGold(1 + this.content.getRent());
            this.setLevel(this.content.getLevel());
        }



    }

    /**
     * Removes the specified building or unit from the content of the Field.
     */
    public void removeContent() {
        setLevel(0);
        content = null;
        gold = 1;
    }

    /**
     * The Getter for the content field.
     *
     * @return The buildings and units in a Field.
     */
    public Container getContent() {
        return content;
    }


    /**
     * Getter for Id of the Field.
     * @return String Id.
     */
    public String getId() {
        return id;
    }

    /**
     * Resets the Gold generation and owner of the field.
     */
    public void reset() {
        this.gold = 1;
        this.player = "";
    }
}
