package collidingempires.server.ingame;

/**
 * Represents a building in the {@link Game}
 * Is a type of {@link Container}.
 */
public class Building extends Container {
    private static final int TOWERCOST = 15;
    private static final int FARMCOST = 12;
    private String type;

    /**
     * A constructor for all types of building.
     *
     * @param type tower, farm or tree
     */
    public Building(String type) {
        setType(type);
        if (type.equals("farm")) {
            setCost(FARMCOST);
            setRent(3);
            setLevel(0);
        } else if (type.equals("tower")) {
            setRent(0);
            setLevel(1);
            setCost(TOWERCOST);
        } else if (type.equals("tree")) {
            setRent(-1);
            setLevel(0);
            setCost(0);
        }
    }

    /**
     * The Getter for the type field of the building
     *
     * @return The type of the building, either farm,tower or tree.
     */
    public String getType() {
        return type;
    }

    /**
     * The Setter for the type field of the building
     *
     * @param type Either farm, tower or tree.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for field Towercost.
     *
     * @return towercost
     */
    public static int getTOWERCOST() {
        return TOWERCOST;
    }

    /**
     * Getter for field Farmcost.
     *
     * @return Farmcost
     */
    public static int getFARMCOST() {
        return FARMCOST;
    }
}
