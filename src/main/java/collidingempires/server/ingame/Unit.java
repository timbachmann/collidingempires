package collidingempires.server.ingame;

/**
 * Represents a Unit in the {@link Game}.
 * Is a type of {@link Container}.
 */
public class Unit extends Container {
    private static final int BASE_LEVEL = 1;
    private static final int BASE_RENT = -1;
    private static final int COST = 10;
    private static final int MAX_LEVEL = 4;
    private boolean moved;

    /**
     * Creates a unit.
     */
    public Unit() {
        setMoved(false);
        setLevel(BASE_LEVEL);
        setCost(COST);
        setRent(BASE_RENT);
    }

    /**
     * Levels up a unit by 1 if the level is below max level.
     */
    public void levelUp() {
        if (this.getLevel() < MAX_LEVEL) {
            setLevel(this.getLevel() + 1);
            int gold = -2;
            switch (getLevel()) {
                case 2:
                    gold = -5;
                    break;
                case 3:
                    gold = -10;
                    break;
                case 4:
                    gold = -20;
                    break;
                default:
                    break;
            }
            setRent(gold);
        }
    }

    /**
     * Getter for MAX_LEVEL.
     *
     * @return MAX_LEVEL
     */
    public static int getMaxLevel() {
        return MAX_LEVEL;
    }

    /**
     * The Getter for the moved field.
     *
     * @return True if the unit has moved in this turn, else false.
     */
    public boolean isMoved() {
        return moved;
    }

    /**
     * The Setter for the moved field.
     *
     * @param moved true if the unit has moved, false else.
     */
    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    /**
     * Gets the Cost of the Unit.
     * @return Cost of Unit
     */
    public static int cost() {
        return COST;

    }
}
