package collidingempires.client.util;

/**
 * Class representing a member list item.
 */
public class MembersListItem {

    private static int idCount = 0;
    private final int id;
    private final String name;
    private STATUS status;

    /**
     * Defining Status states.
     */
    public enum STATUS {
        READY,
        NOTREADY
    }

    /**
     * Constructor initialising the variables of an item.
     *
     * @param name Item name.
     * @param status Item status.
     */
    public MembersListItem(String name, STATUS status) {
        id = idCount;
        this.name = name;
        this.status = status;
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
     *  @return name.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for status.
     *
     *  @return status.
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Setter for status.
     * @param status Status to set.
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }
}
