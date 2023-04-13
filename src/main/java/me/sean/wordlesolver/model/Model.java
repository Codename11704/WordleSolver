package me.sean.wordlesolver.model;

import javafx.scene.paint.Color;
import me.sean.wordlesolver.view.Observer;

import java.io.*;
import java.util.*;

/**
 * A model which represents the wordle game
 *
 * @author Sean Droll
 */
public class Model {
    private static final int MAX_GUESSES = 6;
    private static final int MAX_LENGTH = 5;
    private static final String VALID_WORDS_PATH = "src/main/resources/valid-wordle-words.txt";
    private static final String VALID_ANSWERS_PATH = "src/main/resources/valid-wordle-solutions.txt";
    private static final List<String> VALID_WORDS = initializeFile(VALID_WORDS_PATH);
    private final Observer<Model, String> observer;
    private final Cell<String>[][] board = new Cell[MAX_GUESSES][MAX_LENGTH];
    private int currentGuess = 0;
    private int currentCharacter = 0;
    private final String secretWord;
    private String lastGuessed = "";
    private Colors[] lastColors = {null, null, null, null, null};
    private GameState gameState;
    private final Random rng = new Random();
    private final Map<Object, Colors> letterStatuses = new HashMap<>();

    /**
     * Enum which represents the curerent state of the game
     */
    public enum GameState {

        STANDARD("Guess a word!"),
        WIN("You Won!"),
        LOSE("You lose!"),
        INVALID("Invalid Word!");

        final String message;

        /**
         * Represents the current state of the game
         * @param message a message to be displayed during that gamestate
         */
        GameState(String message) {
            this.message = message;
        }

        /**
         * Get the message describing the gamestate
         * @return the message
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Enum representing the different colors that can result from a guess
     */
    public enum Colors {
        GREEN(Color.LIMEGREEN),
        YELLOW(Color.YELLOW),
        GRAY(Color.GRAY),
        WHITE(Color.WHITE);
        private final Color c;

        /**
         * Creates a new color
         * @param c JavaFX to visually represent color
         */
        Colors(Color c) {
            this.c = c;
        }

        /**
         * Gets the JavaFX color representing the color
         * @return the JavaFX color representing the color
         */
        public Color getColor() {
            return c;
        }
    }

    /**
     * Creates a new model with an observer
     * @param observer the observer that will monitor the Model
     */
    public Model(Observer<Model, String> observer) {
        this.observer = observer;
        this.secretWord = initializeSecretWord();
        this.gameState = GameState.STANDARD;

        char[] alph = "QWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();
        for(char c : alph) {
            letterStatuses.put(String.valueOf(c), Colors.WHITE);
        }
        for(int i = 0; i < MAX_GUESSES; i++) {
            for(int j = 0; j < MAX_LENGTH; j++) {
                this.board[i][j] = new Cell<>(null, Colors.WHITE);
            }
        }
    }

    /**
     * Adds a character to the board at the current guess
     * @param c the character to add
     */
    public void addCharacter(String c) {
        if(this.gameState == GameState.WIN || this.gameState == GameState.LOSE) return;
        if(this.currentCharacter == MAX_LENGTH) return;
        board[this.currentGuess][this.currentCharacter].setValue(c);
        this.currentCharacter++;
        updateObserver("Added Character");
    }

    /**
     * Removes the last character added to the board
     */
    public void removeLastCharacter() {
        if(currentCharacter == 0) return;
        if(this.gameState == GameState.WIN || this.gameState == GameState.LOSE) return;
        this.currentCharacter--;
        board[this.currentGuess][this.currentCharacter].setValue(null);
        this.gameState = GameState.STANDARD;
        updateObserver("Removed Character");
    }

    /**
     * Guesses a word and updates the board to contain the results
     * GREEN = Correct letter correct position
     * YELLOW = Correct letter wrong position
     * GRAY = Wrong letter wrong position
     */
    public void guessWord() {
        StringBuilder guess = new StringBuilder();
        for(Cell<String> item : this.board[this.currentGuess]) {
            guess.append(item.getValue());
        }
        if(this.gameState == GameState.WIN || this.gameState == GameState.LOSE) return;
        if(!VALID_WORDS.contains(guess.toString())) {
            this.gameState = GameState.INVALID;
            updateObserver("Invalid");
            return;
        }
        this.lastGuessed = guess.toString();
        Colors[] colors = {Colors.GRAY, Colors.GRAY, Colors.GRAY, Colors.GRAY, Colors.GRAY};
        char[] solutionArray = this.secretWord.toCharArray();
        char[] guessArray = guess.toString().toCharArray();
        Map<Character, Integer> letterCounts = getLetterCounts(this.secretWord);
        for(int i = 0; i < MAX_LENGTH; i++) {
            char letter = guessArray[i];
            if(solutionArray[i] == letter) {
                colors[i] = Colors.GREEN;
                int value = letterCounts.get(letter) - 1;
                letterCounts.put(letter, value);
            }
        }
        for(int i = 0; i < 5; i++) {
            char letter = guessArray[i];
            if(colors[i] != Colors.YELLOW) {
                if(this.secretWord.contains(String.valueOf(letter))) {
                    if(letterCounts.get(letter) > 0) {
                        colors[i] = Colors.YELLOW;
                        int value = letterCounts.get(letter) - 1;
                        letterCounts.put(letter, value);

                    }
                }
            }
        }
        this.lastColors = colors;
        for(int i = 0; i < MAX_LENGTH; i++) {
            this.board[this.currentGuess][i].setStatus(colors[i]);
            Colors curr = this.letterStatuses.get(this.board[currentGuess][i].getValue());
            if(curr == Colors.GREEN) continue;
            if(curr == Colors.YELLOW && colors[i] == Colors.GRAY) continue;
            this.letterStatuses.put(this.board[this.currentGuess][i].getValue(), colors[i]);
        }

        this.currentGuess++;
        this.currentCharacter = 0;
        if(guess.toString().equals(this.secretWord)) {
            this.gameState = GameState.WIN;
        } else if(this.currentGuess == MAX_GUESSES){
            this.gameState = GameState.LOSE;
        }
        updateObserver("Guessed");
    }

    /**
     * Gets a map of each letter in the word and how many times it occurs
     * @param word the word to get the letter counts from
     * @return a map containing each letter in the word and its occurrences
     */
    private static Map<Character, Integer> getLetterCounts(String word) {
        Map<Character, Integer> counts = new HashMap<>();
        for(char c : word.toCharArray()) {
            if(counts.containsKey(c)) {
                int count = counts.get(c);
                count++;
                counts.put(c, count);
            } else {
                counts.put(c, 1);
            }
        }
        return counts;
    }


    /**
     * Reads a file of words and puts it into a list
     * @param path path of the file
     * @return a list of the words in the file
     */
    private static List<String> initializeFile(String path) {
        List<String> list = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
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
     * Gets the secret word from a list of possible secret words
     * @return the secret word chosen
     */
    private String initializeSecretWord() {
        List<String> validAnswers = initializeFile(VALID_ANSWERS_PATH);
        int ind = this.rng.nextInt(validAnswers.size())-1;
        return validAnswers.get(ind);
    }

    /**
     * Gets a cell from the board
     * @param a the column to get it from
     * @param b the row to get it from
     * @return cell at that location
     */
    public Cell<String> get(int a, int b) {
        return this.board[b][a];
    }

    /**
     * Gets the status of a letter from a list of every letter in the alphabet
     * @param s the letter you wish to get
     * @return the status of that letter
     */
    public Colors getLetterStatus(String s) {
        return letterStatuses.get(s);
    }

    /**
     * Get the current game state
     * @return the current game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * get the last word that was guessed
     * @return the last word that was guessed
     */
    public String getLastGuessed() {
        return this.lastGuessed;
    }

    /**
     * Gets an array representing the results of the last guess
     * @return an array of Colors representing the results of the last guess
     */
    public Colors[] getLastColors() {
        return this.lastColors;
    }

    /**
     * Updates each the observer of this model
     * @param message the message to send to the observer
     */
    private void updateObserver(String message) {
        this.observer.update(this, message);
    }

    /**
     * Returns a string representing the board state
     * @return a string representing the board state
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(Cell<String>[] row : this.board) {
            for(Cell<String> item : row) {
                s.append("[");
                if(item.getValue() != null) {
                    s.append(item.getValue());
                }
                s.append("]");
            }
            s.append("\n");
        }
        return s.toString();
    }
}
