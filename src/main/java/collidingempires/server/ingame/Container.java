package collidingempires.server.ingame;

/**
 * Represents a unit or building in the {@link Game}
 */
public class Container {
    private int level;
    private int rent;
    private int cost;

    /**
     * The Getter for the level field.
     *
     * @return The level of the building or unit.
     */
    public int getLevel() {
        return level;
    }

    /**
     * The Setter for the level field.
     *
     * @param level Number to which level is set.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * The Getter for the gold field.
     *
     * @return How much gold a building or produces or consumes in 1 turn.
     */
    public int getRent() {
        return rent;
    }

    /**
     * The Setter for the gold field.
     *
     * @param gold How much gold you want a building or unit to produce.
     */
    public void setRent(int gold) {
        this.rent = gold;
    }

    /**
     * The Getter for the cost field.
     *
     * @return How much it costs to buy a unit or building.
     */
    public int getCost() {
        return cost;
    }

    /**
     * The Setter for the cost field.
     *
     * @param cost How much you want a unit or building to cost.
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

}
