package me.sean.wordlesolver.solver;

import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a wordle word and the value representing how good of a guess it is
 *
 *
 * @author Sean Droll
 */
public class WordData {

    private final String word;
    private final double data;
    public WordData(String word, double data) {
        this.word = word;
        this.data = data;
    }

    public String getWord() {
        return word;
    }

    public double getData() {
        return data;
    }

    /**
     * Returns a string that has the word and its value in it,
     * the value is NOT rounded
     * @return a string representing the words data
     */
    @Override
    public String toString() {
        return this.word + ": " + this.data;
    }

    /**
     * Gets a list of words and their data from a json file
     * @param jsonFile path to json file
     * @return the list of word data extracted
     */
    public static List<WordData> getWordDataFromFile(String jsonFile) {
        List<WordData> data = new ArrayList<>();
        try (JsonReader reader = new JsonReader(new FileReader(jsonFile))) {
            reader.beginObject();
            while (reader.hasNext()) {
                String word = reader.nextName();
                double value = reader.nextDouble();
                data.add(new WordData(word, value));
            }
            reader.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }


}
