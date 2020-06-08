package collidingempires.server.util;


import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Holds static Methods to manage the Highscores.
 */
public class ScoreHandler {
    private static final String SCORE_PATH = Paths.get("logs/scores.csv").toString();

    /**
     * Converts a list of data to a Hashmap.
     * This method needs to have the checked data.
     * @param rawData the Data to be converted into a Hashmap.
     * @return the map.
     */
    private static HashMap<String, Integer> toMap(LinkedList<String[]> rawData) {
        HashMap<String, Integer> map = new HashMap<>();
        for (String[] line : rawData) {
            map.put(line[0], Integer.parseInt(line[1]));
        }
        return map;
    }
    /**
     * Reads Score out of a Specified CSV-File, checks the format of the data
     * and returns it.
     * @return null if an exception has been thrown or file was empty,
     *          a {@link LinkedList} with the lines of data otherwise
     */
    public static HashMap<String, Integer> getScore() {
        LinkedList<String[]> data = CSVParser.readLines(SCORE_PATH);
        try {
            data.removeIf(line -> !formatChecker(line));
            return toMap(data);
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Writes a Hashmap into a csv-file.
     * @param table the Hashmap to write
     * @return if the operation was successful or not
     */
    public static boolean dumpScore(HashMap<String, Integer> table) {
        LinkedList<String[]> toWrite = new LinkedList<>();
        for (String key : table.keySet()) {
            toWrite.add(new String[]{key, String.valueOf(table.get(key))});
        }
        return CSVParser.writeLines(SCORE_PATH, toWrite);
    }

    /**
     * Checks whether a row of data is in correct format (Name, #WonGames).
     * Checks length of data and the datatype of #Wongames.
     * @param row the line of data to check
     * @return is the line in correct format ?
     */
    private static boolean formatChecker(String[] row) {
        try {
            if (row.length != 2) {
                throw new IndexOutOfBoundsException();
            }
            Integer.parseInt(row[1]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

}
