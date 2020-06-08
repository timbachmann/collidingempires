package collidingempires.server.ingame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Map class specifies the environment in which the Game occurs,
 * by defining a Matrix of {@link Field}.
 */
public class Map {
    private HashMap<String, Field> fields;
    private final ArrayList<String> trees = new ArrayList<>();
    private String mapString = "";
    /* Map example: "00000011010000000000001111110011010000001111101111110011"
        + "0111111111111000111111111111111110111111111111111100011111111"
        + "111110000111111111111110000111111111111111100001000111111111100";*/

    /**
     * Creates a random Map.
     */
    public Map() {
        MapGenerator mapGenerator = new MapGenerator();
        mapString = mapGenerator.generateMap();
        char[] existing = mapString.toCharArray();
        fields = new HashMap<>(180);
        for (int i = 0; i < 180; i++) {
            String id = Integer.toString(i);
            if (existing[i] == 49) {
                fields.put(id, new Field(true, id));

            } else {
                fields.put(id, new Field(false, id));

            }

        }
    }

    /**
     * Creates a map with with custom layout given by the String parameter.
     * Used for Testing purposes.
     * @param mapString String with is a Sequence of 1 and 0.
     */
    public Map(String mapString) {
        this.mapString = mapString;
        char[] existing = mapString.toCharArray();
        fields = new HashMap<>(180);
        for (int i = 0; i < 180; i++) {
            String id = Integer.toString(i);
            if (existing[i] == 49) {
                fields.put(id, new Field(true, id));

            } else {
                fields.put(id, new Field(false, id));
            }
        }
    }

    /**
     * Changes the owner of the field.
     *
     * @param id     id of the field (0 -179)
     * @param player nickname of new owner of the field.
     */
    public void setPlayer(String id, String player) {
        fields.get(id).setPlayer(player);
    }

    /**
     * Adds a new unit or building to a Field.
     *
     * @param id        id of the field (0 - 179)
     * @param container Contains the information of the object that is added.
     *                  (Unit or Building)
     */
    public void addObject(String id, Container container) {
        if (!getMap().containsKey(id)) {
            return;
        }
        if (container instanceof Building && ((Building) container).getType().equals(
                "tower")) {
            for (String s : existingNeighborsList(id)) {
                if (getField(s).getLevel() < container.getLevel()) {
                    getField(s).setLevel(container.getLevel());
                }
            }
        }
        fields.get(id).addContent(container);
    }

    /**
     * Adds a new tree to a Field.
     *
     * @param id id of the field (0 - 179)
     */
    public void addTree(String id) {
        Building tree = new Building("tree");
        trees.add(id);
        fields.get(id).addContent(tree);
    }

    /**
     * Returns all trees on Map.
     *
     * @return List of ids. (range 0 - 179)
     */
    public ArrayList<String> getTrees() {
        return trees;
    }

    /**
     * The Getter for the coordinates field.
     *
     * @return The fields of the map as a two-dimensional Array.
     */
    public HashMap<String, Field> getMap() {
        return fields;
    }

    /**
     * Getter for the individual fields of the map.
     *
     * @param id id of the field to check (0 - 179)
     * @return The specific Field of the Map.
     */
    public Field getField(String id) {
        return fields.get(id);
    }

    /**
     * Method to reset field.
     *
     * @param id id of the field (0 - 179)
     */
    public void resetField(String id) {
        removeContent(id);
        getField(id).reset();
    }


    /**
     * Removes a building or unit from the specified field.
     *
     * @param id id of the field to check (0 - 179)
     */
    public void removeContent(String id) {
        if (!getMap().containsKey(id)) {
            return;
        }
        if (getField(id).getContent() instanceof Building
                && ((Building) getField(id).getContent()).getType().equals("tower")) {
            getField(id).removeContent();
            for (String s : existingNeighborsList(id)) {
                Field fs = getField(s);
                if (fs.getContent() instanceof Building
                        && ((Building) fs.getContent()).getType().equals("tower")) {
                    getField(id).setLevel(1);
                }
                boolean tower = false;
                for (String v : existingNeighborsList(s)) {
                    Field fv = getField(v);
                    if (fv.getContent() instanceof Building
                            && ((Building) fv.getContent()).getType().equals("tower")) {
                        tower = true;
                    }

                }
                if (!tower) {
                    if (fs.getContent() == null) {
                        fs.setLevel(0);
                    } else if (fs.getContent() instanceof Building
                            && (((Building) fs.getContent()).getType().equals("tree")
                            || ((Building) fs.getContent()).getType().equals("farm"))) {
                        fs.setLevel(0);
                    }
                }
            }
        } else {
            if (getField(id).getContent() instanceof Building
                    && ((Building) getField(id).getContent()).getType().equals("tree")) {
                trees.remove(id);
            }
            getField(id).removeContent();
            List<String> towerCheck = existingNeighborsList(id);
            boolean neighborTower = false;
            for (String t : towerCheck) {
                if (getField(t).getContent() instanceof Building
                        && ((Building) getField(t).getContent())
                        .getType().equals("tower")) {
                    neighborTower = true;
                }
            }
            if (neighborTower) {
                getField(id).setLevel(1);
            }
        }
    }


    /**
     * Gets neighboring x and y value for given x and y value disregarding possibility.
     *
     * @param id if of the field to check (0 - 179)
     * @return 2D array with y value in collumn 0 and x value in collumn 1
     */
    public List<String> getNeighborIds(String id) {
        List<String> neighbor = new ArrayList<>();
        int in = Integer.parseInt(id);

        if (in % 2 == 1) {
            neighbor.add(Integer.toString(in - 18));
            neighbor.add(Integer.toString(in - 1));
            neighbor.add(Integer.toString(in + 1));
            neighbor.add(Integer.toString(in + 17));
            neighbor.add(Integer.toString(in + 18));
            neighbor.add(Integer.toString(in + 19));
        } else {
            neighbor.add(Integer.toString(in - 18));
            neighbor.add(Integer.toString(in - 19));
            neighbor.add(Integer.toString(in - 17));
            neighbor.add(Integer.toString(in - 1));
            neighbor.add(Integer.toString(in + 18));
            neighbor.add(Integer.toString(in + 1));
        }
        return neighbor;
    }

    /**
     * Gets a List of the neighboring coordinates of the initial field which exist in
     * the game.
     *
     * @param id id of the field to check (0 -179)
     * @return List of coordinates of neighbors
     */
    public List<String> existingNeighborsList(String id) {
        List<String> existingNeighbors = new ArrayList<>();
        List<String> neighborIds = getNeighborIds(id);
        for (String s : neighborIds) {
            int i = Integer.parseInt(s);
            if (i < 180 && i > 0) {
                if (getField(s).exists()) {
                    existingNeighbors.add(s);
                }
            }
        }
        return existingNeighbors;
    }

    /**
     * Checks if field with coordinate c borders players territory.
     *
     * @param against id of current field (0 - 179)
     * @param toCheck id of field to attack (0 - 179)
     * @return true if it borders, false if not
     */
    public boolean borderField(String against, String toCheck) {
        //if parameter Ids are Invalid we return false;
        //Fields also have to exist
        if (!getMap().containsKey(against) || !getMap().containsKey(toCheck)) {
            return false;
        } else if (!getField(against).exists() || !getField(toCheck).exists()) {
            return false;
        }
        List<String> checkList = existingNeighborsList(toCheck);
        List<String> againstList = existingNeighborsList(against);
        String owner = getField(against).getPlayer();
        boolean isBorder = false;

        for (String id : checkList) {
            //if its next to initial field
            if (checkList.contains(against)) {
                isBorder = true;
            } else if (againstList.contains(id)) {
                if (getField(id).getPlayer().equals(owner)) {
                    isBorder = true;
                }
            }
        }
        return isBorder;
    }

    /**
     * Checks if Level of attacking unit is big enough to move to c.
     *
     * @param against id of current field (0 - 179)
     * @param toCheck id of field to attack (0 - 179)
     * @return true if attackable, false if not
     */
    public boolean isAttackable(String against, String toCheck) {
        //if parameter Ids are Invalid we return false;
        //Fields also have to exist
        if (!getMap().containsKey(against) || !getMap().containsKey(toCheck)) {
            return false;
        } else if (!getField(against).exists() || !getField(toCheck).exists()) {
            return false;
        }
        //compares level of attacking unit with level of destination.
        Field current = getField(against);
        Field des = getField(toCheck);
        String owner = current.getPlayer();
        List<String> checkTower = existingNeighborsList(toCheck);
        boolean ownTower = false;
        boolean otherTower = false;
        for (String s : checkTower) {
            Field check = getField(s);
            if (check.getContent() instanceof Building
                    && ((Building) check.getContent()).getType()
                    .equals("tower") && check.getPlayer().equals(owner)) {
                ownTower = true;
            } else if (check.getContent() instanceof Building
                    && ((Building) check.getContent()).getType()
                    .equals("tower") && !(check.getPlayer().equals(owner))) {
                otherTower = true;
            }
        }

        if (!des.getPlayer().equals(owner)) {
            if (!otherTower && (des.getContent() == null
                    || (des.getContent() instanceof Building
                    && ((Building) des.getContent()).getType().equals("tree")))) {
                return true;
            } else {
                return (current.getContent().getLevel() > des.getLevel());
            }

        } else {
            return (!otherTower && !(des.getContent() instanceof Unit));
        }
    }

    /**
     * Gets the coordinates of fields which can possibly be moved to.
     *
     * @param id id of initial field (0 - 179)
     * @return a list of coordinates
     */
    public List<Integer> getPossibleIds(String id) {
        List<String> possibleIds = new ArrayList<>();
        if (!getMap().containsKey(id)
                || !(getField(id).getContent() instanceof Unit)) {
                    return stringToInt(possibleIds);
        }
        List<String> toCheck = fieldsInRange(id);
        for (String check : toCheck) {
            if (borderField(id, check) && isAttackable(id, check)) {
                possibleIds.add(check);
            }
        }
        return stringToInt(possibleIds);
    }

    /**
     * Get fields in range for field.
     *
     * @param id id of initial Field (0 - 179)
     * @return List of fields in range.
     */
    public List<String> fieldsInRange(String id) {

        if (!(getMap().containsKey(id))) {
            return new ArrayList<>();
        }

        List<String> distance1 = existingNeighborsList(id);
        List<String> distance2;
        List<String> toCheck = new ArrayList<>();

        for (String close : distance1) {
            distance2 = existingNeighborsList(close);
            if (!toCheck.contains(close) && !id.equals(close)) {
                toCheck.add(close);
            }
            for (String further : distance2) {

                if (!toCheck.contains(further) && !id.equals(further)) {
                    toCheck.add(further);
                }
            }
        }
        return toCheck;
    }

    /**
     * Converts a List of Strings to a List of Integers
     *
     * @param toConvert List of Strings representing Ids (0 - 179)
     * @return List of Integers
     */
    public List<Integer> stringToInt(List<String> toConvert) {
        List<Integer> output = new ArrayList<>();
        for (String s : toConvert) {
            output.add(Integer.parseInt(s));
        }
        return output;
    }

    /**
     * Checks if there are any Fields, where the content is null.
     *
     * @return true if all Fields have content, false if some fields are empty
     */
    public boolean isFull() {
        for (int i = 0; i < getMap().size(); i++) {
            Field f = getMap().get(String.valueOf(i));
            if (f.getContent() == null && f.exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Getter for the String representing the existing Fields of the Map
     *
     * @return Representation of Map by a String
     */
    public String getExist() {
        return mapString;
    }
}
