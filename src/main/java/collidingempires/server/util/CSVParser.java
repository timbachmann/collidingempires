package collidingempires.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class CSVParser {
    static final String DELIMITER = ",";

    /**
     * Reads all lines of a csv file ans aves them into a {@link LinkedList}.
     * Returns null if an IOException occurs or the filer doesnt exists.
     * @param path the path to the csvFile to read.
     * @return a {@link LinkedList} with the read lines.
     */
    public static LinkedList<String[]> readLines(String path) {
        File csvFile = new File(path);
        if (csvFile.isFile()) {
            try {
                BufferedReader csvReader = new BufferedReader(new FileReader(csvFile));
                LinkedList<String[]> data = new LinkedList<>();
                String line;
                while ((line = csvReader.readLine()) != null) {
                    data.add(line.split(DELIMITER));
                }
                csvReader.close();
                return data;
            } catch (IOException e) {
               return null;
            }
        }
        return null;
    }

    /**
     * Creates a new File or recreates it and write lines
     * in the csv format in it.
     * @param path full path to the file to write in
     * @param lines the lines to write as an 2D-Array of Strings.
     * @return success of the operation: False for an IOException
     */
    public static boolean writeLines(String path, LinkedList<String[]> lines) {
        File csvFile = new File(path);
        try {
            if (!recreate(csvFile)) {
                throw new IOException();
            }
            FileWriter csvWriter = new FileWriter(csvFile);
            for (String[] line : lines) {
                csvWriter.append(String.join(DELIMITER, line));
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Delete and create a file for the given
     * {@link File} Object
     * @param file The file to recreate
     * @return success of recreating the file
     */
    public static boolean recreate(File file) {
        try {
            file.delete();
            file.createNewFile();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
