package collidingempires.server.ingame;

import static java.lang.Math.random;

/**
 * Class to generate random maps.
 * Inspiration: http://www.math.com/students/wonders/life/life.html (EGdP Übung 10)
 */
public class MapGenerator {

    private final int width = 20;
    private final int height = 9;

    /**
     * Generates a random map with width and height of available space.
     * @return Random map in form of String.
     */
    public String generateMap() {
        boolean[][] map = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (random() < 0.1f) {
                    map[x][y] = true;
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            map = simulationStep(map);
        }
        return arrayToString(map);
    }

    /**
     * Represents one simulation step.
     * @param oldMap Map the step is performed on.
     * @return New map with modified entries.
     */
    private boolean[][] simulationStep(boolean[][] oldMap) {
        boolean[][] newMap = new boolean[width][height];
        for (int x = 0; x < oldMap.length; x++) {
            for (int y = 0; y < oldMap[0].length; y++) {
                int nbs = aliveNeighbours(oldMap, x, y);
                if (oldMap[x][y]) {
                    newMap[x][y] = nbs >= 3;
                } else {
                    newMap[x][y] = nbs > 4;
                }
            }
        }
        return newMap;
    }

    /**
     * Counts alive neighbours of a cell.
     * @param map Map of all cells.
     * @param x X-coordinate of cell.
     * @param y Y-coordinate of cell.
     * @return Number of alive neighbours.
     */
    private int aliveNeighbours(boolean[][] map, int x, int y) {
        int count = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int neighbourX = x + i;
                int neighbourY = y + j;
                if (i != 0 || j != 0) {
                    if (neighbourX < 0 || neighbourY < 0 || neighbourX >= map.length
                            || neighbourY >= map[0].length) {
                        count = count + 1;
                    } else if (map[neighbourX][neighbourY]) {
                        count = count + 1;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Converts positions of boolean[][] into a specific
     * String format readable by client and server.
     *
     * @param array Array to read values from.
     * @return String readable by client and server.
     */
    private String arrayToString(boolean[][] array) {
        StringBuilder stringBuilder = new StringBuilder(180);
        for (int count = 0, j = 0, line = 1; count < 180; count++) {
            if (count % 18 == 0 && count != 0) {
                j = 0;
                line += 2;
            }
            if (count % 2 == 0) {
                line--;
            } else {
                line++;
            }
            stringBuilder.append(array[line][j] ? "0" : "1");
            if (line % 2 != 0) {
                j++;
            }
        }
        return stringBuilder.toString();
    }
}
