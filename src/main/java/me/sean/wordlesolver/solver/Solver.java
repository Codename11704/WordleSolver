package me.sean.wordlesolver.solver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Solves a given wordle by caulculating the average information gained from each possible result
 *
 * @author Sean Droll
 */
public class Solver {
    private static final String VALID_WORDS_PATH = "src/main/resources/valid-wordle-words.txt";
    private static final String DATA_MATRIX_PATH = "src/main/resources/datamatrix.json";
    private static final String WORD_DATA_PATH = "src/main/resources/worddata.json";
    private static final DataMatrix DATA_MATRIX = new DataMatrix(DATA_MATRIX_PATH);
    private List<String> validWords;
    private List<WordData> wordData;


    /**
     * Creates a new instance of solver
     */
    public Solver() {
        this.validWords = initializeValidWordsPath();
        this.wordData = WordData.getWordDataFromFile(WORD_DATA_PATH);
    }

    /**
     * Initializes the list of valid words
     * @return the list of valid wordle answers
     */
    private static List<String> initializeValidWordsPath() {
        List<String> list = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(Solver.VALID_WORDS_PATH))) {
            String line = br.readLine();
            while(line != null) {
                list.add(line.strip().toUpperCase());
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Valid Words File not Found");
            System.exit(-2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * Converts an array of integers between 0 - 2 and converts it to an integer
     * @param arr array of integers
     * @return an integer describing the array
     */
    private static int arrToInt(int[] arr) {
        int n = 0;
        int ind = arr.length - 1;
        for(int i : arr) {
            n += Math.pow(3, ind)*i;
            ind -= 1;
        }
        return n;
    }

    /**
     * Updates the list of valid words given the results of a wordle guess
     * @param word the word that was guessed
     * @param results the results of that guess as described as an integer array
     */
    public void updateList(String word, int[] results) {
        List<String> possible = DATA_MATRIX.getIndices(word, arrToInt(results));
        List<String> newList = new ArrayList<>(this.validWords);
        for (String possibility : this.validWords) {
            if (!possible.contains(possibility)) {
                newList.remove(possibility);

            }
        }
        this.validWords = new ArrayList<>(newList);
    }

    /**
     * Updates the WordData list to reflect the current state of the game, splits up the work to be
     * run in parallel so it runs faster
     * @param threads the number of threads to run
     */
    public void splitWork(int threads) {
        List<WordData> data = new ArrayList<>();
        List<CalculateInformation> threadList = new ArrayList<>();
        double chunks = (double)validWords.size()/(double)threads;
        for(int i = 0; i < threads; i++) {
            List<String> chunk = validWords.subList((int)Math.floor(chunks*i), (int)Math.floor(chunks*(i+1)));
            CalculateInformation thread = new CalculateInformation(DATA_MATRIX, validWords, chunk);
            thread.start();
            threadList.add(thread);
        }
        for(CalculateInformation thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            data.addAll(thread.getData());
        }
        this.wordData = new ArrayList<>(data);
        this.wordData.sort(Comparator.comparingDouble(WordData::getData));
    }

    /**
     * Gets the top 10 words to be guessed based off average information
     * @return List of top 10 words to guess
     */
    public List<WordData> getTopTen() {
        if(this.wordData.size() >= 10) {
            return this.wordData.subList(0, 10);
        }
        return this.wordData;
    }

}
