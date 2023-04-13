package me.sean.wordlesolver.solver;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a list of WordData that contains the average information to gain if that
 * word was guessed
 *
 * @author Sean Droll
 */
public class CalculateInformation extends Thread {
    private final DataMatrix matrix;
    private final List<String> validWords;
    private final List<String> chunk;
    private List<WordData> data;

    /**
     * Creates a new instance of TopAnswers
     * @param matrix the datamatrix to use to compute the best possible word
     * @param validWords a list of the valid words in the game
     * @param chunk the chunk of words to computer
     */
    public CalculateInformation(DataMatrix matrix, List<String> validWords, List<String> chunk) {
        this.matrix = matrix;
        this.validWords = new ArrayList<>(validWords);
        this.chunk = new ArrayList<>(chunk);
    }

    /**
     * Computes the possible words that could be the secret word given the results of a guess
     * @param data current possible words
     * @param word the guess
     * @param results the results of that guess
     * @return the remaining valid words
     */
    private List<String> updateList(List<String> data, String word, int results) {
        List<String> possible = this.matrix.getIndices(word, results);
        List<String> newList = new ArrayList<>(data);

        for (String possibility : data) {
            if (!possible.contains(possibility)) {
                newList.remove(possibility);

            }
        }
        return newList;
    }

    /**
     * Computes the log2 of a value
     * @param x the value
     * @return the log2 of the value unless it is less than 0, then it will return 0
     */
    private double safeLogTwo(double x) {
        if(x > 0) {
            return Math.log10(x)/Math.log10(2);
        }
        return 0;
    }

    /**
     * Generates a list of WordData objects
     * @return a list of WordData objects that contains the word and the
     * average information that would be hoped to gain if that word was guessed
     */
    private List<WordData> calculateAverageInformation() {
        List<WordData> wordData = new ArrayList<>();
        for(String word : this.chunk) {
            double avgInfo = 0;
            for(int i = 0; i < 363; i++) {
                List<String> newList = updateList(this.validWords, word, i);
                double prob = (double)newList.size()/(double)this.validWords.size();

                if(prob != 0) {
                    double info = safeLogTwo(1/prob);
                    avgInfo += prob*info;
                }
            }
            wordData.add(new WordData(word, avgInfo));
        }
        return wordData;
    }

    /**
     * Gets the list of word data objects
     * @return the word data objects
     */
    public List<WordData> getData() {
        return this.data;
    }

    /**
     * initializes the list of word data
     */
    @Override
    public void run() {
        this.data = calculateAverageInformation();
    }
}
