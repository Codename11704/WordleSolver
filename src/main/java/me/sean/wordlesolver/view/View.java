package me.sean.wordlesolver.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import me.sean.wordlesolver.model.Cell;
import me.sean.wordlesolver.model.Model;
import me.sean.wordlesolver.solver.Solver;
import me.sean.wordlesolver.solver.WordData;

import java.util.List;

/**
 * This program plays out like a wordle game,
 * but suggests the statistically best words
 * on the left side of the window.
 *
 * @author Sean Droll
 */
public class View extends Application implements Observer<Model, String> {
    private final static BorderPane BORDER_PANE = new BorderPane();
    private Model model;
    private Solver solver;

    /**
     * Initializes the mainstage of the JavaFX Application
     * @param stage The Mainstage of the program
     */
    private void init(Stage stage) {
        this.model = new Model(this);
        this.solver = new Solver();
        Label top = new Label(this.model.getGameState().getMessage());
        top.setStyle("""
            -fx-font-size: 15;
            -fx-font-family: Modelo;
        """);
        BORDER_PANE.setTop(top);
        BorderPane.setAlignment(top, Pos.TOP_CENTER);
        GridPane mainPane = initializeMainGrid();
        BORDER_PANE.setCenter(mainPane);
        GridPane letterGrid = initializeLetterGrid();
        BorderPane bottom = new BorderPane();
        Button enter = initializeEnterButton();
        bottom.setLeft(letterGrid);
        bottom.setRight(enter);
        BorderPane.setAlignment(letterGrid, Pos.BOTTOM_LEFT);
        BorderPane.setAlignment(enter, Pos.CENTER_RIGHT);
        BORDER_PANE.setBottom(bottom);
        BorderPane.setAlignment(mainPane, Pos.CENTER);
        BorderPane.setAlignment(letterGrid, Pos.BOTTOM_CENTER);
        VBox box = initializeSideList();
        BORDER_PANE.setLeft(box);
        Scene scene = new Scene(BORDER_PANE);
        scene.setOnKeyPressed(event->{
            KeyCode key = event.getCode();
            if(key.isLetterKey()) {
                this.model.addCharacter(key.getChar());
            } else if(key.getCode() == KeyCode.BACK_SPACE.getCode()) {
                this.model.removeLastCharacter();
            } else if(key.getCode() == KeyCode.ENTER.getCode()) {
                this.model.guessWord();
            }

        });
        stage.setScene(scene);
    }


    /**
     * Creates the main center grid of the game
     * @return A JavaFX GridPane representing the wordle board
     */
    private GridPane initializeMainGrid() {
        GridPane pane = new GridPane();
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 6; j++) {
                Cell<String> cell = this.model.get(i, j);
                Label label = new Label(cell.getValue());
                label.setStyle("""
                        -fx-alignment: center;
                        -fx-font-family: Modelo;
                        -fx-font-size: 20;
                        -fx-min-height: 40;
                        -fx-min-width: 40;
                        -fx-border-style: solid inside;
                        -fx-border-width: 2;
                        -fx-border-radius: 2;
                        -fx-border-color: black;
                """);
                label.setBackground(new Background(new BackgroundFill(cell.getStatus().getColor(), new CornerRadii(2), null)));
                pane.add(label, i, j);
            }
        }
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    /**
     * Creates the grid of letters at the bottom of the screen each colored
     * with the state of the current game
     * @return A JavaFX GridPane representing the bottom alphabet grid
     */
    private GridPane initializeLetterGrid() {
        char[] alph = "QWERTYUIOPASDFGHJKLZXCVBNM".toCharArray();
        GridPane pane = new GridPane();
        for(int i = 0; i < 10; i++) {
            String value = String.valueOf(alph[i]);
            Button btn = generateLetterButton(value);
            pane.add(btn, i, 0);
        }
        for(int i = 0; i < 9; i++) {
            String value = String.valueOf(alph[i+10]);
            Button btn = generateLetterButton(value);
            pane.add(btn, i, 1);
        }
        for(int i = 0; i < 7; i++) {
            String value = String.valueOf(alph[(19+i)]);
            Button btn = generateLetterButton(value);
            pane.add(btn, i, 2);
        }

        pane.alignmentProperty().setValue(Pos.CENTER);
        pane.setPadding(new Insets(10));
        return pane;

    }

    /**
     * Creates a single button representing a letter and colored with the state
     * of the current letter, the button will display on screen if pressed
     * @param value the letter the button represents
     * @return a JavaFX button that represents a letter
     */
    private Button generateLetterButton(String value) {
        Button btn = new Button(value);
        btn.setStyle( """
                     -fx-border-style: solid inside;
                     -fx-border-width: 1;
                     -fx-border-color: black;
                     -fx-border-radius: 2;
                     -fx-border-insets: 1;
                """);
        btn.setFont(new Font("Menlo", 18));
        btn.setMinSize(40, 40);
        btn.setOnMouseClicked(event -> this.model.addCharacter(value));
        btn.setOnMousePressed(event -> btn.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(2), new Insets(1)))));
        btn.setOnMouseReleased(event -> btn.setBackground(new Background(new BackgroundFill(this.model.getLetterStatus(value).getColor(), new CornerRadii(2), new Insets(1)))));
        btn.setBackground(new Background(new BackgroundFill(this.model.getLetterStatus(value).getColor(), new CornerRadii(2), new Insets(1))));
        btn.setPadding(new Insets(10));
        return btn;
    }

    /**
     * Creates the enter button that will guess the current word on the screen when pressed
     * @return a JavaFX button that will guess a word if pressed
     */
    private Button initializeEnterButton() {
        Button btn = new Button("Enter");
        btn.setStyle( """
                     -fx-border-style: solid inside;
                     -fx-border-width: 1;
                     -fx-border-color: black;
                     -fx-border-radius: 2;
                     -fx-border-insets: 1;
                """);
        btn.setOnMouseClicked(event -> this.model.guessWord());
        return btn;
    }

    /**
     * Shows the top 10 statistically best words to guess
     * @return a JavaFX VBox that contains the top 10 best guesses in order
     */
    private VBox initializeSideList() {
        VBox box = new VBox();
        List<WordData> data = solver.getTopTen();
        int i = 1;
        for(WordData item : data) {
            String word = item.getWord();
            double value = item.getData();
            String line = String.format("\t" + i + ".) " + word + ": %.3f %n", value);
            Label label = new Label(line);
            box.getChildren().add(label);
            i++;
        }
        return box;
    }


    /**
     * Starts the program
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     */
    @Override
    public void start(Stage primaryStage) {
        init(primaryStage);
        primaryStage.setTitle("Wordle");
        primaryStage.show();
    }

    /**
     * Updates the current view when something changes in the model
     *
     * @param model the model the view is observing
     * @param message a message that contains the change in the model
     */
    @Override
    public void update(Model model, String message) {
        if(message.equals("Guessed")) {
            String word = model.getLastGuessed();
            Model.Colors[] colors = model.getLastColors();
            int[] res = new int[5];
            for(int i = 0; i < 5; i++) {
                switch (colors[i]) {
                    case GREEN -> res[i] = 2;
                    case YELLOW -> res[i] = 1;
                    case GRAY -> res[i] = 0;
                }
            }
            this.solver.updateList(word, res);
            this.solver.splitWork(20);
        }
        GridPane mainPane = initializeMainGrid();
        BORDER_PANE.setCenter(mainPane);
        Label top = new Label(this.model.getGameState().getMessage());
        top.setStyle("""
            -fx-font-size: 15;
            -fx-font-family: Modelo;
        """);
        BORDER_PANE.setTop(top);
        BorderPane.setAlignment(top, Pos.TOP_CENTER);
        GridPane letterGrid = initializeLetterGrid();
        BorderPane bottom = new BorderPane();
        Button enter = initializeEnterButton();
        bottom.setLeft(letterGrid);
        bottom.setRight(enter);
        BorderPane.setAlignment(letterGrid, Pos.BOTTOM_LEFT);
        BorderPane.setAlignment(enter, Pos.CENTER_RIGHT);
        BORDER_PANE.setBottom(bottom);
        BorderPane.setAlignment(mainPane, Pos.CENTER);
        BorderPane.setAlignment(letterGrid, Pos.BOTTOM_CENTER);
        VBox box = initializeSideList();
        BORDER_PANE.setLeft(box);
        BorderPane.setAlignment(box, Pos.CENTER_LEFT);
    }
}
