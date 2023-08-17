package org.example;

import java.io.*;
import java.util.Scanner;

public class Counter {

    public static final String FILE_NAME = "counter.txt";
    private File file;

    public Counter() {
        file = new File(FILE_NAME);
    }
    public long getNext() {
        long counter = readFromFile();
        counter += 1;
        saveToFile(String.valueOf(counter));
        return counter;
    }
    private long readFromFile() {
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                return myReader.nextLong();
            }
            myReader.close();
        } catch (Exception e) {
            System.out.println("An error occurred when reading from file " + e.getMessage());
        }
        return 0;
    }

    private void saveToFile(String counter) throws RuntimeException {
        if (file == null) {
            file = new File(FILE_NAME);
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.write(counter);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
