package me.sean.wordlesolver.solver;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A matrix that contains an integer value described by two strings
 *
 * @author Sean Droll
 */
public class DataMatrix {
    Map<String, Map<String, Integer>> matrix;

    /**
     * Creates a new DataMatrix from data stored in a JSon File
     * @param path the path of the json file to get the data from
     */
    public DataMatrix(String path) {
        this.matrix = new HashMap<>();
        try(JsonReader reader = new JsonReader(new FileReader(path))) {
            reader.setLenient(true);
            reader.beginObject();
            while(!reader.peek().equals(JsonToken.END_OBJECT)) {
                String key1 = reader.nextName();
                reader.beginObject();
                Map<String, Integer> map2 = new HashMap<>();
                while(!reader.peek().equals(JsonToken.END_OBJECT)) {
                    String key2 = reader.nextName();
                    int value = reader.nextInt();
                    map2.put(key2, value);
                }
                reader.endObject();
                matrix.put(key1, map2);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a list of strings that has the specified value at a combination with the specified String
     * @param key the other index in the matrix
     * @param value the value at the given index
     * @return a list a values that if given the specified key, would return the specified value
     */
    public List<String> getIndices(String key, Integer value) {
        List<String> list = new ArrayList<>();
        Map<String, Integer> keys = this.matrix.get(key);
        for(String key2 : keys.keySet()) {
            Integer value2 = keys.get(key2);
            if(value2.equals(value)) {
                list.add(key2);
            }
        }
        return list;
    }

}
