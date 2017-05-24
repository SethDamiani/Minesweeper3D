package Minesweeper3D;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Seth Damiani on 2017-02-01.
 * 3D Minesweeper Game
 * FST for ICS3U
 */

public class Main extends Application {
    public static void main(String[] args) {
        launch(args); // Launch window from IDE
    }
    final private String HIGH_SCORES_LOCATION = System.getProperty("user.home")+"/Documents/Minesweeper3D_HighScores.csv"; // Location of the high scores csv
    private MouseEvent mouseEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, 1, 2, 3, 4, MouseButton.PRIMARY, 5, true, true, true, true, true, true, true, true, true, true, null); // Example MouseEvent
    private Cell[][][] Grid       = new Cell[6][10][10]; // Array of cells
    private Group gameRoot        = new Group(); // Game screen root group
    private Group pauseRoot       = new Group(); // Pause screen root group
    private Group menuRoot        = new Group(); // Menu screen root group
    private Group scoresRoot      = new Group(); // Scores screen root group
    private Group quitRoot        = new Group(); // Quit screen root group
    private Group gX              = new Group(); // X-axis rotate group
    private Group gY              = new Group(); // Y-axis rotate group
    private Group cube            = new Group(); // Cube container
    private Group sideA           = new Group(); // Side A of cube
    private Group sideB           = new Group(); //  ||  B   ||
    private Group sideC           = new Group(); //  ||  C   ||
    private Group sideD           = new Group(); //  ||  D   ||
    private Group sideE           = new Group(); //  ||  E   ||
    private Group sideF           = new Group(); //  ||  F   ||
    private Scene game            = new Scene(gameRoot, 800, 700, true);   // Game   scene
    private Scene pause           = new Scene(pauseRoot, Color.BLACK);                              // Pause  scene
    private Scene menu            = new Scene(menuRoot, 800, 700, Color.LIGHTGRAY);   // Menu   scene
    private Scene scores          = new Scene(scoresRoot, 800, 700, Color.LIGHTGRAY); // Scores scene
    private Scene quit            = new Scene(quitRoot, Color.BLACK);                               // Quit   scene
    private int time; // Tracks time spent in a given game
    private static int totalBombs; // How many bombs are in the board
    private static int flaggedCells; // How many cells have been flagged
    private static int flaggedBombs; // How many cells have been correctly flagged
    private static Text stopwatch = new Text(100, 100, "Time: 0"); // Displays the time elapsed
    private static Text bombs     = new Text(450, 100, "Bombs: "+flaggedCells+"/"+totalBombs); // Displays the cells flagged / the total bombs
    private HashMap<Long, HighScore> highScoresMap = new HashMap<>(); // Keeps high scores in memory for manipulation
    private TableView table; // High scores table
    final private Image bg          = new Image("/images/minesweeper.png");      // Background graphic in menu
    final private Image one         = new Image("/images/minesweeper-1.png");    // Cell graphic for cell touching 1 bombs
    final private Image two         = new Image("/images/minesweeper-2.png");    // Cell graphic for cell touching 2 bombs
    final private Image three       = new Image("/images/minesweeper-3.png");    // Cell graphic for cell touching 3 bombs
    final private Image four        = new Image("/images/minesweeper-4.png");    // Cell graphic for cell touching 4 bombs
    final private Image five        = new Image("/images/minesweeper-5.png");    // Cell graphic for cell touching 5 bombs
    final private Image six         = new Image("/images/minesweeper-6.png");    // Cell graphic for cell touching 6 bombs
    final private Image seven       = new Image("/images/minesweeper-7.png");    // Cell graphic for cell touching 7 bombs
    final private Image bomb        = new Image("/images/minesweeper-bomb.png"); // Cell graphic for cell that is a bomb
    final private Image flag        = new Image("/images/minesweeper-flag.png"); // Cell graphic for cell that is flagged
    final private Image[] images    = {null, one, two, three, four, five, six, seven, flag}; // Array of cell graphics for easy access
    private static boolean gameLost = false; // True if the game has been lost
    private static boolean gameWon  = false; // True if the game has been won
    private String difficulty       = "";    // Stores the current game mode (Easy, Medium, or Hard)
    private TableColumn IDCol = new TableColumn("Date");
    private TableColumn difficultyCol = new TableColumn("Difficulty");
    private TableColumn firstNameCol = new TableColumn("First");
    private TableColumn lastNameCol = new TableColumn("Last");
    private TableColumn timeCol = new TableColumn("Time");

    @Override
    public void start(Stage primaryStage) throws Exception { // Main JavaFX method
        cube.getChildren().addAll(sideA, sideB, sideC, sideD, sideE, sideF); // Add all sides into the cube group
        cube.setLayoutX(150); // Set proper layout
        cube.setLayoutY(190);
        createCube(cube, gX, gY, game, primaryStage); // Create the main cube object
        gameRoot.getChildren().add(gY); // Properly nest cube rotation control groups
        gY.getChildren().add(gX);       //       ||
        gX.getChildren().add(cube);     //       ||

        stopwatch.setFont(new Font(45));
        bombs.setFont(new Font(45));
        gameRoot.getChildren().addAll(stopwatch, bombs);
        Timeline timeTask = new Timeline(new KeyFrame(Duration.seconds(1), event -> { // Task to keep track of time
            time++;
            stopwatch.setText("Time: "+String.valueOf(time));
        }));
        timeTask.setCycleCount(Timeline.INDEFINITE); // Make timeTask run forever and ever

        for (int x = 0; x < Grid.length; x++){ // Add the cells to their proper side groups
            for (int y = 0; y < Grid[x].length; y++){
                for (int z = 0; z < Grid[x][y].length; z++){
                    Grid[x][y][z] = new Cell(cube, 40, 40*y, 40*z, x);
                    switch (x){
                        case 0: sideA.getChildren().add(Grid[x][y][z].cell); break;
                        case 1: sideB.getChildren().add(Grid[x][y][z].cell); break;
                        case 2: sideC.getChildren().add(Grid[x][y][z].cell); break;
                        case 3: sideD.getChildren().add(Grid[x][y][z].cell); break;
                        case 4: sideE.getChildren().add(Grid[x][y][z].cell); break;
                        case 5: sideF.getChildren().add(Grid[x][y][z].cell); break;
                    }
                }
            }
        }

        placeBombs(100); // Place bombs randomly
        placeNumbers(); // Place the correct numbers according to the new bomb positions

        // -------------------------------------------------------------------------------
        // Quit screen UI setup
        Rectangle quitBox = new Rectangle(800, 700, Color.INDIANRED); // Initialize and configure background
        Text quitText = new Text(260, 200, "Game Over"); // Initialize and configure the title
        quitText.setFont(new Font(80));
        quitText.setStroke(Color.WHITE);
        quitText.setFill(Color.WHITE);
        quitText.setTextAlignment(TextAlignment.CENTER);
        quitText.setX(400-quitText.getLayoutBounds().getWidth()/2);
        Text endScoreText = new Text(260, 320, "Time: "); // Initialize and configure the time text
        endScoreText.setFont(new Font(40));
        endScoreText.setTextAlignment(TextAlignment.CENTER);
        endScoreText.setFill(Color.WHITE);
        endScoreText.setStroke(Color.WHITE);
        endScoreText.setX(350-endScoreText.getLayoutBounds().getWidth()/2); // Center the text within the button
        TextField firstNameInput = new TextField(); // Initialize and configure first name input field
        firstNameInput.setFont(new Font(24));
        Text firstNameLabel = new Text("First name: "); // Initialize and configure first name input label
        firstNameLabel.setFont(new Font(28));
        firstNameLabel.setFill(Color.WHITE);
        HBox firstNameBox = new HBox(5, firstNameLabel, firstNameInput); // Initialize and configure horizontal box for first name input
        firstNameBox.setLayoutX(180);
        firstNameBox.setLayoutY(395);
        TextField lastNameInput = new TextField(); // Initialize and configure last name input field
        lastNameInput.setFont(new Font(24));
        Text lastNameLabel = new Text("Last name: "); // Initialize and configure last name input label
        lastNameLabel.setFont(new Font(28));
        lastNameLabel.setFill(Color.WHITE);
        HBox lastNameBox = new HBox(5, lastNameLabel, lastNameInput); // Initialize and configure horizontal box for last name input
        lastNameBox.setLayoutX(182);
        lastNameBox.setLayoutY(450);
        Rectangle continueRect = new Rectangle(300, 550, 500, 70); // Initialize and configure continue button border
        continueRect.setStroke(Color.WHITE);
        continueRect.setFill(Color.TRANSPARENT);
        continueRect.setStrokeWidth(5);
        Text continueText = new Text("Submit and Continue"); // Initialize and configure continue button text
        continueText.setFont(new Font(50));
        continueText.setFill(Color.WHITE);
        continueText.setY(600);
        continueText.setX(400-continueText.getLayoutBounds().getWidth()/2);
        continueRect.setWidth(continueText.getLayoutBounds().getWidth()+40);
        continueRect.setX(400-continueRect.getLayoutBounds().getWidth()/2);
        firstNameInput.setOnAction(event -> lastNameInput.requestFocus()); // Advance to next field on enter press

        game.addEventHandler(KeyEvent.KEY_PRESSED, event -> { // Manual game-win override
            if (event.getCode() == KeyCode.BACK_SLASH && event.isAltDown()){
                gameWon = true;
                endScoreText.setText("Time: "+time);
                endScoreText.setX(400-endScoreText.getLayoutBounds().getWidth()/2);
                game.getOnMouseClicked().handle(mouseEvent);
            }
        });

        lastNameInput.setOnAction(event -> { // Submit score when enter is pressed in last name entry
            if (gameWon) {
                String firstName = firstNameInput.getText();
                String lastName = lastNameInput.getText();
                Long date = System.currentTimeMillis() / 1000;
                highScoresMap.put(date, new HighScore(date, difficulty, firstName, lastName, time));
                refreshTableData();
                try {
                    writeHighScores();
                } catch (IOException e){
                    System.out.println("ERROR: couldn't save high scores to file.");
                }
            }
            primaryStage.setScene(menu);
        });

        quitRoot.getChildren().addAll(quitBox, quitText, endScoreText, firstNameBox, lastNameBox, continueRect, continueText);

        EventHandler<MouseEvent> submitScores = event -> { // Process new high score data into table and .csv
            if (gameWon) {
                String firstName = firstNameInput.getText();
                String lastName = lastNameInput.getText();
                Long date = System.currentTimeMillis() / 1000;
                highScoresMap.put(date, new HighScore(date, difficulty, firstName, lastName, time));
                refreshTableData();
                try {
                    writeHighScores();
                } catch (IOException e){
                    System.out.println("ERROR: couldn't save high scores to file.");
                }
            }
            primaryStage.setScene(menu);
        };
        continueRect.addEventHandler(MouseEvent.MOUSE_CLICKED, submitScores);
        continueText.addEventHandler(MouseEvent.MOUSE_CLICKED, submitScores);

        // Pause screen UI setup
        Rectangle pauseBox = new Rectangle(800,700,Color.BLACK);
        Text pauseText = new Text(260,350,"Paused");
        pauseText.setFont(new Font(80));
        pauseText.setStroke(Color.WHITE);
        pauseText.setFill(Color.WHITE);
        pauseText.setTextAlignment(TextAlignment.CENTER);
        Text timeText = new Text(260, 430, "Time: ");
        timeText.setFont(new Font(40));
        timeText.setTextAlignment(TextAlignment.CENTER);
        timeText.setFill(Color.WHITE);
        timeText.setStroke(Color.WHITE);
        timeText.setX(250-timeText.getLayoutBounds().getWidth()/2);
        Text bombsText = new Text(550, 430, bombs.getText());
        bombsText.setFont(new Font(40));
        bombsText.setTextAlignment(TextAlignment.CENTER);
        bombsText.setFill(Color.WHITE);
        bombsText.setStroke(Color.WHITE);
        bombsText.setX(500-bombsText.getLayoutBounds().getWidth()/2);
        Text infoText = new Text("Loading");
        infoText.setFont(new Font(40));
        infoText.setTextAlignment(TextAlignment.CENTER);
        infoText.setFill(Color.WHITE);
        infoText.setStroke(Color.WHITE);
        infoText.setX(400-infoText.getLayoutBounds().getWidth()/2);
        infoText.setY(430);
        Rectangle resume = new Rectangle(300, 500, 200, 70);
        resume.setFill(Color.TRANSPARENT);
        resume.setStroke(Color.WHITE);
        resume.setStrokeWidth(5);
        Text resumeText = new Text("Resume");
        resumeText.setFont(new Font(50));
        resumeText.setFill(Color.WHITE);
        resumeText.setY(550);
        resumeText.setX(400-resumeText.getLayoutBounds().getWidth()/2);
        pauseRoot.getChildren().addAll(pauseBox, pauseText, resume, resumeText, infoText);
        EventHandler<MouseEvent> resumeButton = event -> {
            timeTask.play();
            primaryStage.setScene(game);
        };
        resume.addEventHandler(MouseEvent.MOUSE_CLICKED, resumeButton);
        resumeText.addEventHandler(MouseEvent.MOUSE_CLICKED, resumeButton);

        pause.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE){
                primaryStage.setScene(game);
                timeTask.play();
            }
            else if (event.getCode() == KeyCode.ESCAPE) primaryStage.close();
        });
        game.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE){
                timeTask.pause();
                infoText.setText("Time: "+time+"s  Bombs: "+flaggedCells+"/"+totalBombs);
                infoText.setX(400-infoText.getLayoutBounds().getWidth()/2);
                primaryStage.setScene(pause);
            }
        });
        // High scores UI setup
        scores.setFill(new ImagePattern(bg));
        Text scoresTitle = new Text(120, 120, "High Scores");
        scoresRoot.getChildren().add(scoresTitle);
        scoresTitle.setFont(new Font(70));
        scoresTitle.setStroke(Color.BLACK);
        scoresTitle.setStrokeWidth(5);
        scoresTitle.setTextAlignment(TextAlignment.CENTER);
        scoresTitle.setX(400-scoresTitle.getLayoutBounds().getWidth()/2);
        Rectangle back = new Rectangle(100, 550, 200, 70);
        back.setFill(Color.TRANSPARENT);
        back.setStroke(Color.BLACK);
        back.setStrokeWidth(5);
        back.setFill(new Color(1,1,1,.5));
        back.setOnMouseEntered(event -> back.setFill(new Color(1,1,1,.8)));
        back.setOnMouseExited( event -> back.setFill(new Color(1,1,1,.5)));
        scoresRoot.getChildren().add(back);
        Text backText = new Text("Back");
        backText.setFont(new Font(50));
        backText.setFill(Color.BLACK);
        backText.setY(600);
        backText.setX(200-backText.getLayoutBounds().getWidth()/2);
        backText.setStroke(Color.BLACK);
        backText.setStrokeWidth(3);
        backText.setOnMouseEntered(event -> back.setFill(new Color(1,1,1,.8)));
        backText.setOnMouseExited( event -> back.setFill(new Color(1,1,1,.5)));
        scoresRoot.getChildren().add(backText);
        EventHandler<MouseEvent> backButton = event -> primaryStage.setScene(menu);
        back.addEventHandler(MouseEvent.MOUSE_CLICKED, backButton);
        backText.addEventHandler(MouseEvent.MOUSE_CLICKED, backButton);
        Rectangle deleteScore = new Rectangle(500, 550, 200, 70);
        deleteScore.setFill(Color.TRANSPARENT);
        deleteScore.setStroke(Color.BLACK);
        deleteScore.setStrokeWidth(5);
        deleteScore.setFill(new Color(1,1,1,.5));
        deleteScore.setOnMouseEntered(event -> deleteScore.setFill(new Color(1,1,1,.8)));
        deleteScore.setOnMouseExited( event -> deleteScore.setFill(new Color(1,1,1,.5)));
        scoresRoot.getChildren().add(deleteScore);
        Text deleteScoresText = new Text("Delete");
        deleteScoresText.setFont(new Font(50));
        deleteScoresText.setFill(Color.BLACK);
        deleteScoresText.setY(600);
        deleteScoresText.setX(600-deleteScoresText.getLayoutBounds().getWidth()/2);
        deleteScoresText.setStroke(Color.BLACK);
        deleteScoresText.setStrokeWidth(3);
        deleteScoresText.setOnMouseEntered(event -> deleteScore.setFill(new Color(1,1,1,.8)));
        deleteScoresText.setOnMouseExited( event -> deleteScore.setFill(new Color(1,1,1,.5)));
        scoresRoot.getChildren().add(deleteScoresText);
        EventHandler<MouseEvent> deleteScoreButton = event -> { // Delete selected high score from table and .csv
            if (table.getSelectionModel().getSelectedIndex() == -1) return;
            HighScore current = (HighScore) table.getSelectionModel().getSelectedItem();
            highScoresMap.remove(Long.parseLong(current.getRawID()));
            refreshTableData();
            try {
                writeHighScores();
            } catch (IOException e){
                System.out.println("ERROR: couldn't save high scores to file.");
            }
        };
        deleteScore.addEventHandler(MouseEvent.MOUSE_CLICKED, deleteScoreButton);
        deleteScoresText.addEventHandler(MouseEvent.MOUSE_CLICKED, deleteScoreButton);

        CheckBox editable = new CheckBox("Edit mode");
        editable.setSelected(true);
        editable.setLayoutX(630);
        editable.setLayoutY(30);
        editable.setFont(Font.font("Tahoma", FontWeight.BLACK, 18));
        scoresRoot.getChildren().add(editable);

        // High scores table setup
        table = new TableView();
        table.setLayoutX(100);
        table.setLayoutY(200);
        table.setPrefWidth(600);
        table.setPrefHeight(300);
        table.setEditable(true);
        EventHandler<TableColumn.CellEditEvent> fieldEditCommit = event -> {
            if (table.getSelectionModel().getSelectedIndex() == -1) return;
            HighScore current = (HighScore) table.getSelectionModel().getSelectedItem();
            switch (event.getTableColumn().getId()){
                case "Difficulty": highScoresMap.get(Long.parseLong(current.getRawID())).setDifficulty((String)event.getNewValue()); break;
                case "First Name": highScoresMap.get(Long.parseLong(current.getRawID())).setFirstName((String)event.getNewValue()); break;
                case "Last Name": highScoresMap.get(Long.parseLong(current.getRawID())).setLastName((String)event.getNewValue()); break;
            }
            try {
                writeHighScores();
            } catch (IOException e){
                System.out.println("Error saving scores");
            }
            refreshTableData();
        };
        table.setStyle(".list-view .scroll-bar:horizontal .increment-arrow,.list-view .scroll-bar:horizontal .decrement-arrow,.list-view .scroll-bar:horizontal .increment-button,.list-view .scroll-bar:horizontal .decrement-button {-fx-padding:0;}");
        IDCol.setCellValueFactory(new PropertyValueFactory<HighScore, String>("ID"));
        IDCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        IDCol.setId("ID");
        IDCol.setSortable(false);
        IDCol.setResizable(false);
        difficultyCol.setCellValueFactory(new PropertyValueFactory<HighScore, String>("difficulty"));
        difficultyCol.setCellFactory(TextFieldTableCell.forTableColumn());
        difficultyCol.setOnEditCommit(fieldEditCommit);
        difficultyCol.prefWidthProperty().bind(table.widthProperty().multiply(0.175));
        difficultyCol.setId("Difficulty");
        difficultyCol.setSortable(false);
        difficultyCol.setResizable(false);
        firstNameCol.setCellValueFactory(new PropertyValueFactory<HighScore, String>("firstName"));
        firstNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        firstNameCol.setOnEditCommit(fieldEditCommit);
        firstNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.175));
        firstNameCol.setId("First Name");
        firstNameCol.setSortable(false);
        firstNameCol.setResizable(false);
        lastNameCol.setCellValueFactory(new PropertyValueFactory<HighScore, String>("lastName"));
        lastNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        lastNameCol.setOnEditCommit(fieldEditCommit);
        lastNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.175));
        lastNameCol.setId("Last Name");
        lastNameCol.setSortable(false);
        lastNameCol.setResizable(false);
        timeCol.setCellValueFactory(new PropertyValueFactory<HighScore, String>("time"));
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.172));
        timeCol.setId("Time");
        timeCol.setSortable(true);
        timeCol.setResizable(false);
        table.getSortOrder().setAll(timeCol);
        timeCol.setSortType(TableColumn.SortType.ASCENDING);
        table.getColumns().addAll(IDCol, difficultyCol, firstNameCol, lastNameCol, timeCol);
        table.setOnMousePressed(event -> table.getSortOrder().setAll(timeCol));
        MenuItem tableDelete = new MenuItem("Delete");
        tableDelete.setOnAction(event -> {
            if (table.getSelectionModel().getSelectedIndex() == -1) return;
            HighScore current = (HighScore) table.getSelectionModel().getSelectedItem();
            highScoresMap.remove(Long.parseLong(current.getRawID()));
            refreshTableData();
        });
        editable.setOnAction(event -> {
            if (event.getSource() instanceof CheckBox){
                CheckBox checkBox = (CheckBox) event.getSource();
                if (checkBox.isSelected()){
                    difficultyCol.setEditable(true);
                    firstNameCol.setEditable(true);
                    lastNameCol.setEditable(true);
                }
                else {
                    difficultyCol.setEditable(false);
                    firstNameCol.setEditable(false);
                    lastNameCol.setEditable(false);
                }
            }
        });
        // Search abilities setup
        table.setPlaceholder(new Label("Search did not return any results."));
        ObservableList<String> searchOptions = FXCollections.observableArrayList("Date", "Difficulty", "First Name", "Last Name", "Time");
        ComboBox searchSelector = new ComboBox(searchOptions);
        searchSelector.getSelectionModel().select(2);
        searchSelector.setLayoutX(585-searchSelector.getLayoutBounds().getWidth()/2);
        searchSelector.setLayoutY(160);
        TextField search = new TextField();
        search.setPromptText("Search");
        search.setPrefWidth(480);
        search.setPrefHeight(searchSelector.getHeight());
        search.setLayoutX(100);
        search.setLayoutY(160);
        // Execute search on selection mode change
        searchSelector.setOnAction(event -> refreshTableData(search.getText(), (String)searchSelector.getSelectionModel().getSelectedItem()));
        // Execute search on any keystroke
        search.setOnKeyReleased(event -> {
            String query = search.getText();
            refreshTableData(query, (String)searchSelector.getSelectionModel().getSelectedItem());
        });
        scoresRoot.getChildren().addAll(table, searchSelector, search);

        // Menu setup
        menu.setFill(new ImagePattern(bg));
        Text title = new Text(120, 120, "3D Minesweeper");
        menuRoot.getChildren().add(title);
        title.setFont(new Font(70));
        title.setStroke(Color.BLACK);
        title.setStrokeWidth(5);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setX(400-title.getLayoutBounds().getWidth()/2);

        Text name = new Text(150, 200, "Computer Science (ICS3U-01) FST\nBy Seth Damiani");
        menuRoot.getChildren().add(name);
        name.setFont(new Font(30));
        name.setTextAlignment(TextAlignment.CENTER);
        name.setStroke(Color.BLACK);
        name.setStrokeWidth(2);
        name.setX(400-name.getLayoutBounds().getWidth()/2);

        Rectangle easy = new Rectangle(250, 300, 300, 75);
        menuRoot.getChildren().add(easy);
        easy.setStroke(Color.BLACK);
        easy.setStrokeWidth(5);
        easy.setFill(new Color(1,1,1,.5));
        easy.setOnMouseEntered(event -> easy.setFill(new Color(1,1,1,.8)));
        easy.setOnMouseExited( event -> easy.setFill(new Color(1,1,1,.5)));

        Text easyText = new Text(400, 337.5, "Easy");
        menuRoot.getChildren().add(easyText);
        easyText.setFont(new Font(50));
        easyText.setTextAlignment(TextAlignment.CENTER);
        easyText.setStroke(Color.BLACK);
        easyText.setStrokeWidth(3);
        easyText.setX(400-easyText.getLayoutBounds().getWidth()/2);
        easyText.setY(350);
        easyText.setOnMouseEntered(event -> easy.setFill(new Color(1,1,1,.8)));
        easyText.setOnMouseExited( event -> easy.setFill(new Color(1,1,1,.5)));

        Rectangle medium = new Rectangle(250, 400, 300, 75);
        menuRoot.getChildren().add(medium);
        medium.setStroke(Color.BLACK);
        medium.setStrokeWidth(5);
        medium.setFill(Color.TRANSPARENT);
        medium.setFill(new Color(1,1,1,.5));
        medium.setOnMouseEntered(event -> medium.setFill(new Color(1,1,1,.8)));
        medium.setOnMouseExited( event -> medium.setFill(new Color(1,1,1,.5)));

        Text mediumText = new Text(400, 337.5, "Medium");
        menuRoot.getChildren().add(mediumText);
        mediumText.setFont(new Font(50));
        mediumText.setTextAlignment(TextAlignment.CENTER);
        mediumText.setStroke(Color.BLACK);
        mediumText.setStrokeWidth(3);
        mediumText.setX(400-mediumText.getLayoutBounds().getWidth()/2);
        mediumText.setY(450);
        mediumText.setOnMouseEntered(event -> medium.setFill(new Color(1,1,1,.8)));
        mediumText.setOnMouseExited( event -> medium.setFill(new Color(1,1,1,.5)));

        Rectangle hard = new Rectangle(250, 500, 300, 75);
        menuRoot.getChildren().add(hard);
        hard.setStroke(Color.BLACK);
        hard.setStrokeWidth(5);
        hard.setFill(Color.TRANSPARENT);
        hard.setFill(new Color(1,1,1,.5));
        hard.setOnMouseEntered(event -> hard.setFill(new Color(1,1,1,.8)));
        hard.setOnMouseExited( event -> hard.setFill(new Color(1,1,1,.5)));

        Text hardText = new Text(400, 337.5, "Hard");
        menuRoot.getChildren().add(hardText);
        hardText.setFont(new Font(50));
        hardText.setTextAlignment(TextAlignment.CENTER);
        hardText.setStroke(Color.BLACK);
        hardText.setStrokeWidth(3);
        hardText.setX(400-hardText.getLayoutBounds().getWidth()/2);
        hardText.setY(550);
        hardText.setOnMouseEntered(event -> hard.setFill(new Color(1,1,1,.8)));
        hardText.setOnMouseExited( event -> hard.setFill(new Color(1,1,1,.5)));

        Rectangle highscores = new Rectangle(600, 550, 150, 100);
        menuRoot.getChildren().add(highscores);
        highscores.setStroke(Color.BLACK);
        highscores.setStrokeWidth(5);
        highscores.setFill(Color.TRANSPARENT);
        highscores.setFill(new Color(1,1,1,.5));
        highscores.setOnMouseEntered(event -> highscores.setFill(new Color(1,1,1,.8)));
        highscores.setOnMouseExited( event -> highscores.setFill(new Color(1,1,1,.5)));

        Text highscoresText = new Text("High\nScores");
        menuRoot.getChildren().add(highscoresText);
        highscoresText.setFont(new Font(40));
        highscoresText.setTextAlignment(TextAlignment.CENTER);
        highscoresText.setStroke(Color.BLACK);
        highscoresText.setStrokeWidth(3);
        highscoresText.setX(675-highscoresText.getLayoutBounds().getWidth()/2);
        highscoresText.setY(590);
        highscoresText.setOnMouseEntered(event -> highscores.setFill(new Color(1,1,1,.8)));
        highscoresText.setOnMouseExited( event -> highscores.setFill(new Color(1,1,1,.5)));

        Rectangle exit = new Rectangle(50, 550, 150, 100);
        menuRoot.getChildren().add(exit);
        exit.setStroke(Color.BLACK);
        exit.setStrokeWidth(5);
        exit.setFill(Color.TRANSPARENT);
        exit.setFill(new Color(1,1,1,.5));
        exit.setOnMouseEntered(event -> exit.setFill(new Color(1,1,1,.8)));
        exit.setOnMouseExited( event -> exit.setFill(new Color(1,1,1,.5)));

        Text exitText = new Text("Exit");
        menuRoot.getChildren().add(exitText);
        exitText.setFont(new Font(40));
        exitText.setTextAlignment(TextAlignment.CENTER);
        exitText.setStroke(Color.BLACK);
        exitText.setStrokeWidth(3);
        exitText.setX(125-exitText.getLayoutBounds().getWidth()/2);
        exitText.setY(610);
        exitText.setOnMouseEntered(event -> exit.setFill(new Color(1,1,1,.8)));
        exitText.setOnMouseExited( event -> exit.setFill(new Color(1,1,1,.5)));

        // Handle click on easy button
        EventHandler<MouseEvent> easyButton = event -> {
            resetGame();
            placeBombs(50);
            totalBombs = 50;
            flaggedCells = 0;
            placeNumbers();
            bombs.setText("Bombs: "+flaggedCells+"/"+totalBombs);
            difficulty = "Easy";
            primaryStage.setScene(game);
            time = 0;
            timeTask.play();
        };
        easy.addEventHandler(MouseEvent.MOUSE_CLICKED, easyButton);
        easyText.addEventHandler(MouseEvent.MOUSE_CLICKED, easyButton);

        // Handle click on medium button
        EventHandler<MouseEvent> mediumButton = event -> {
            resetGame();
            placeBombs(100);
            totalBombs = 100;
            flaggedCells = 0;
            placeNumbers();
            bombs.setText("Bombs: "+flaggedCells+"/"+totalBombs);
            difficulty = "Medium";
            primaryStage.setScene(game);
            time = 0;
            timeTask.play();
        };
        medium.addEventHandler(MouseEvent.MOUSE_CLICKED, mediumButton);
        mediumText.addEventHandler(MouseEvent.MOUSE_CLICKED, mediumButton);

        // Handle click on hard button
        EventHandler<MouseEvent> hardButton = event -> {
            resetGame();
            placeBombs(150);
            totalBombs = 150;
            flaggedCells = 0;
            placeNumbers();
            bombs.setText("Bombs: "+flaggedCells+"/"+totalBombs);
            difficulty = "Hard";
            primaryStage.setScene(game);
            time = 0;
            timeTask.play();
        };
        hard.addEventHandler(MouseEvent.MOUSE_CLICKED, hardButton);
        hardText.addEventHandler(MouseEvent.MOUSE_CLICKED, hardButton);

        EventHandler<MouseEvent> exitButton = event -> System.exit(0);
        exit.addEventHandler(MouseEvent.MOUSE_CLICKED, exitButton);
        exitText.addEventHandler(MouseEvent.MOUSE_CLICKED, exitButton);

        EventHandler<MouseEvent> highScoresButton = event -> primaryStage.setScene(scores);
        highscoresText.addEventHandler(MouseEvent.MOUSE_CLICKED, highScoresButton);
        highscores.addEventHandler(MouseEvent.MOUSE_CLICKED, highScoresButton);

        readHighScores();
        refreshTableData();
        // Continuously check if game has been won or lost
        game.setOnMouseClicked(event -> {
            if (gameLost){
                timeTask.stop();
                endScoreText.setText("Time: "+time);
                endScoreText.setX(400-endScoreText.getLayoutBounds().getWidth()/2);
                quitBox.setFill(Color.INDIANRED);
                quitText.setText("You Lost");
                quitText.setX(400-quitText.getLayoutBounds().getWidth()/2);
                firstNameBox.setVisible(false);
                lastNameBox.setVisible(false);
                endScoreText.setVisible(false);
                continueText.setText("Menu");
                continueText.setX(400-continueText.getLayoutBounds().getWidth()/2);
                continueRect.setWidth(continueText.getLayoutBounds().getWidth()+40);
                continueRect.setX(400-continueRect.getLayoutBounds().getWidth()/2);
                primaryStage.setScene(quit);
            }
            if (gameWon){
                timeTask.stop();
                quitBox.setFill(Color.FORESTGREEN);
                quitText.setText("You Won!");
                quitText.setX(400-quitText.getLayoutBounds().getWidth()/2);
                firstNameBox.setVisible(true);
                lastNameBox.setVisible(true);
                endScoreText.setVisible(true);
                endScoreText.setText("Time: "+time);
                continueText.setText("Submit & Continue");
                continueText.setX(400-continueText.getLayoutBounds().getWidth()/2);
                continueRect.setWidth(continueText.getLayoutBounds().getWidth()+40);
                continueRect.setX(400-continueRect.getLayoutBounds().getWidth()/2);
                primaryStage.setScene(quit);
            }
        });

        primaryStage.setTitle("Minesweeper 3D - Seth Damiani");
        primaryStage.setResizable(false);
        primaryStage.setScene(menu);
        primaryStage.show();
    }
    private void placeBombs(int bombCount){
        for (int b = 0; b < bombCount;){
            Random rn = new Random();
            int x = rn.nextInt(6);
            int y = rn.nextInt(10);
            int z = rn.nextInt(10);
            if (!Grid[x][y][z].isBomb) b++;
            Grid[x][y][z].isBomb = true;
        }
    }
    private void resetGame(){
        for (int x = 0; x < Grid.length; x++){
            for (int y = 0; y < Grid[x].length; y++){
                for (int z = 0; z < Grid[x][y].length; z++){
                    Grid[x][y][z].isBomb = false;
                    Grid[x][y][z].isClicked = false;
                    Grid[x][y][z].isFlagged = false;
                    Grid[x][y][z].cell.setFill(Color.GRAY);
                    flaggedCells = 0;
                }
            }
        }
        gameWon = false;
        gameLost = false;
        time = 0;
        flaggedCells = 0;
        flaggedBombs = 0;
        totalBombs = 0;
    }
    private void placeNumbers(){
        for (int x = 0; x < Grid.length; x++){
            for (int y = 0; y < Grid[x].length; y++){
                for (int z = 0; z < Grid[x][y].length; z++){
                    HashSet<Cell> output = getSurroundingCells(x,y,z);
                    Grid[x][y][z].surroundingCells = output;
                    HashSet<Cell> outputBombs = new HashSet<>();
                    for (Cell c:output){
                        if (c.isBomb) outputBombs.add(c);
                    }
                    Grid[x][y][z].surroundingBombs = outputBombs;
                    Grid[x][y][z].surroundingBombsCount = outputBombs.size();
                    if (Grid[x][y][z].isBomb) Grid[x][y][z].clickedFill = new ImagePattern(bomb);
                    else if (Grid[x][y][z].surroundingBombsCount == 0) Grid[x][y][z].clickedFill = Color.LIGHTGRAY;
                    else Grid[x][y][z].clickedFill = new ImagePattern(images[Grid[x][y][z].surroundingBombsCount]);
                }
            }
        }
    }
    private void createCube(Group cubeGroup, Group gX, Group gY, Scene scene, Stage stage){
        cubeGroup.setTranslateZ(-200);

        sideB.setTranslateX(200);
        sideB.setTranslateZ(200);
        sideB.setRotationAxis(new Point3D(0,1,0));
        sideB.setRotate(90);

        sideC.setTranslateZ(200);
        sideC.setTranslateX(-200);
        sideC.setRotationAxis(new Point3D(0,1,0));
        sideC.setRotate(90);

        sideD.setTranslateZ(400);

        sideE.setRotationAxis(new Point3D(1,0,0));
        sideE.setRotate(90);
        sideE.setTranslateY(200);
        sideE.setTranslateZ(200);

        sideF.setRotationAxis(new Point3D(1,0,0));
        sideF.setRotate(90);
        sideF.setTranslateY(-200);
        sideF.setTranslateZ(200);

        RotateTransition rightRotate = new RotateTransition(Duration.millis(10000), gY);
        rightRotate.setAxis(Rotate.Y_AXIS);
        rightRotate.setByAngle(-1000);
        rightRotate.setInterpolator(Interpolator.LINEAR);

        RotateTransition leftRotate = new RotateTransition(Duration.millis(10000), gY);
        leftRotate.setAxis(Rotate.Y_AXIS);
        leftRotate.setByAngle(1000);
        leftRotate.setInterpolator(Interpolator.LINEAR);

        RotateTransition upRotate = new RotateTransition(Duration.millis(10000), gX);
        upRotate.setAxis(Rotate.X_AXIS);
        upRotate.setByAngle(-1000);
        upRotate.setInterpolator(Interpolator.LINEAR);

        RotateTransition downRotate = new RotateTransition(Duration.millis(10000), gX);
        downRotate.setAxis(Rotate.X_AXIS);
        downRotate.setByAngle(1000);
        downRotate.setInterpolator(Interpolator.LINEAR);



        EventHandler<KeyEvent> KeyPressHandler = event -> {
            if (event.getCode() == KeyCode.ESCAPE) stage.close();
            else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) rightRotate.play();
            else if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT)  leftRotate.play();
            else if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP)    upRotate.play();
            else if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN)  downRotate.play();
        };
        scene.addEventHandler(KeyEvent.KEY_PRESSED, KeyPressHandler);


        EventHandler<KeyEvent> KeyReleaseHandler = event -> {
            if (event.getCode() == KeyCode.ESCAPE) stage.close();
            else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) rightRotate.stop();
            else if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT)  leftRotate.stop();
            else if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP)    upRotate.stop();
            else if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN)  downRotate.stop();
        };
        scene.addEventHandler(KeyEvent.KEY_RELEASED, KeyReleaseHandler);
    }
    private HashSet<Cell> getSurroundingCells(int x, int y, int z){
        HashSet<Cell> cellsList = new HashSet<>();
        if (!Grid[x][y][z].isEdge){
            cellsList.add(Grid[x][y+1][z]  );
            cellsList.add(Grid[x][y+1][z+1]);
            cellsList.add(Grid[x][y]  [z+1]);
            cellsList.add(Grid[x][y-1][z]  );
            cellsList.add(Grid[x][y-1][z-1]);
            cellsList.add(Grid[x][y]  [z-1]);
            cellsList.add(Grid[x][y+1][z-1]);
            cellsList.add(Grid[x][y-1][z+1]);
        }
        else {
            if (Grid[x][y][z].x == 0){                      // if side is side 0
                if (Grid[x][y][z].edge == 1 ){
                    cellsList.add(Grid[5][y+1][0]  );
                    cellsList.add(Grid[5][y]  [0]  );
                    cellsList.add(Grid[5][y-1][0]  );
                    cellsList.add(Grid[x][y+1][z]  );
                    cellsList.add(Grid[x][y-1][z]  );
                    cellsList.add(Grid[x][y]  [z+1]);
                    cellsList.add(Grid[x][y+1][z+1]);
                    cellsList.add(Grid[x][y-1][z+1]);
                }
                if (Grid[x][y][z].edge == 2 ){
                    cellsList.add(Grid[1][9]  [z-1]);
                    cellsList.add(Grid[1][9]  [z]  );
                    cellsList.add(Grid[1][9]  [z+1]);
                    cellsList.add(Grid[x][y]  [z+1]);
                    cellsList.add(Grid[x][y]  [z-1]);
                    cellsList.add(Grid[x][y-1][z+1]);
                    cellsList.add(Grid[x][y-1][z]  );
                    cellsList.add(Grid[x][y-1][z-1]);
                }
                if (Grid[x][y][z].edge == 3 ){
                    cellsList.add(Grid[4][y-1][0]  );
                    cellsList.add(Grid[4][y]  [0]  );
                    cellsList.add(Grid[4][y+1][0]  );
                    cellsList.add(Grid[x][y+1][z]  );
                    cellsList.add(Grid[x][y-1][z]  );
                    cellsList.add(Grid[x][y-1][z-1]);
                    cellsList.add(Grid[x][y]  [z-1]);
                    cellsList.add(Grid[x][y+1][z-1]);
                }
                if (Grid[x][y][z].edge == 4 ){
                    cellsList.add(Grid[2][9]  [z-1]);
                    cellsList.add(Grid[2][9]  [z]  );
                    cellsList.add(Grid[2][9]  [z+1]);
                    cellsList.add(Grid[x][y]  [z+1]);
                    cellsList.add(Grid[x][y]  [z-1]);
                    cellsList.add(Grid[x][y+1][z+1]);
                    cellsList.add(Grid[x][y+1][z]  );
                    cellsList.add(Grid[x][y+1][z-1]);
                }
                if (Grid[x][y][z].edge == 14){
                    cellsList.add(Grid[5][0][0]);
                    cellsList.add(Grid[5][1][0]);
                    cellsList.add(Grid[2][9][0]);
                    cellsList.add(Grid[2][9][1]);
                    cellsList.add(Grid[0][0][1]);
                    cellsList.add(Grid[0][1][0]);
                    cellsList.add(Grid[0][1][1]);
                }
                if (Grid[x][y][z].edge == 12){
                    cellsList.add(Grid[5][8][0]);
                    cellsList.add(Grid[5][9][0]);
                    cellsList.add(Grid[1][9][0]);
                    cellsList.add(Grid[1][9][1]);
                    cellsList.add(Grid[0][8][1]);
                    cellsList.add(Grid[0][8][0]);
                    cellsList.add(Grid[0][9][1]);
                }
                if (Grid[x][y][z].edge == 23){
                    cellsList.add(Grid[4][8][0]);
                    cellsList.add(Grid[4][9][0]);
                    cellsList.add(Grid[1][9][8]);
                    cellsList.add(Grid[1][9][9]);
                    cellsList.add(Grid[0][9][8]);
                    cellsList.add(Grid[0][8][8]);
                    cellsList.add(Grid[0][8][9]);
                }
                if (Grid[x][y][z].edge == 34){
                    cellsList.add(Grid[2][9][8]);
                    cellsList.add(Grid[2][9][9]);
                    cellsList.add(Grid[4][0][0]);
                    cellsList.add(Grid[4][1][0]);
                    cellsList.add(Grid[0][0][8]);
                    cellsList.add(Grid[0][1][8]);
                    cellsList.add(Grid[0][1][9]);
                }
            }
            if (Grid[x][y][z].x == 1){                      // if side is side 1
                if (Grid[x][y][z].edge == 1 ){
                    cellsList.add(Grid[5][9]  [mirrored(y)-1]);
                    cellsList.add(Grid[5][9]  [mirrored(y)]  );
                    cellsList.add(Grid[5][9]  [mirrored(y)+1]);
                    cellsList.add(Grid[1][y+1][0]);
                    cellsList.add(Grid[1][y-1][0]);
                    cellsList.add(Grid[1][y]  [1]);
                    cellsList.add(Grid[1][y+1][1]);
                    cellsList.add(Grid[1][y-1][1]);
                }
                if (Grid[x][y][z].edge == 2 ){
                    cellsList.add(Grid[0][9]  [z-1]);
                    cellsList.add(Grid[0][9]  [z]  );
                    cellsList.add(Grid[0][9]  [z+1]);
                    cellsList.add(Grid[1][y]  [z+1]);
                    cellsList.add(Grid[1][y]  [z-1]);
                    cellsList.add(Grid[1][y-1][z+1]);
                    cellsList.add(Grid[1][y-1][z]  );
                    cellsList.add(Grid[1][y-1][z-1]);
                }
                if (Grid[x][y][z].edge == 3 ){
                    cellsList.add(Grid[4][9]  [mirrored(y)+1]);
                    cellsList.add(Grid[4][9]  [mirrored(y)]  );
                    cellsList.add(Grid[4][9]  [mirrored(y)-1]);
                    cellsList.add(Grid[1][y+1][9]);
                    cellsList.add(Grid[1][y-1][9]);
                    cellsList.add(Grid[1][y-1][8]);
                    cellsList.add(Grid[1][y]  [8]);
                    cellsList.add(Grid[1][y+1][8]);
                }
                if (Grid[x][y][z].edge == 4 ){
                    cellsList.add(Grid[3][9]  [z-1]);
                    cellsList.add(Grid[3][9]  [z]  );
                    cellsList.add(Grid[3][9]  [z+1]);
                    cellsList.add(Grid[x][y]  [z+1]);
                    cellsList.add(Grid[x][y]  [z-1]);
                    cellsList.add(Grid[x][y+1][z+1]);
                    cellsList.add(Grid[x][y+1][z]  );
                    cellsList.add(Grid[x][y+1][z-1]);
                }
                if (Grid[x][y][z].edge == 12){
                    cellsList.add(Grid[0][9][0]);
                    cellsList.add(Grid[0][9][1]);
                    cellsList.add(Grid[5][9][0]);
                    cellsList.add(Grid[5][9][1]);
                    cellsList.add(Grid[1][9][1]);
                    cellsList.add(Grid[1][8][0]);
                    cellsList.add(Grid[1][8][1]);
                }
                if (Grid[x][y][z].edge == 14){
                    cellsList.add(Grid[5][9][8]);
                    cellsList.add(Grid[5][9][9]);
                    cellsList.add(Grid[3][9][0]);
                    cellsList.add(Grid[3][9][1]);
                    cellsList.add(Grid[1][0][1]);
                    cellsList.add(Grid[1][1][0]);
                    cellsList.add(Grid[1][1][1]);
                }
                if (Grid[x][y][z].edge == 34){
                    cellsList.add(Grid[4][9][8]);
                    cellsList.add(Grid[4][9][9]);
                    cellsList.add(Grid[3][9][8]);
                    cellsList.add(Grid[3][9][9]);
                    cellsList.add(Grid[1][0][8]);
                    cellsList.add(Grid[1][1][8]);
                    cellsList.add(Grid[1][1][9]);
                }
                if (Grid[x][y][z].edge == 23){
                    cellsList.add(Grid[4][9][0]);
                    cellsList.add(Grid[4][9][1]);
                    cellsList.add(Grid[0][9][8]);
                    cellsList.add(Grid[0][9][9]);
                    cellsList.add(Grid[1][9][8]);
                    cellsList.add(Grid[1][8][8]);
                    cellsList.add(Grid[1][8][9]);
                }
            }
            if (Grid[x][y][z].x == 2){                      // if side is side 2
                if (Grid[x][y][z].edge == 1 ){
                    cellsList.add(Grid[5][0]  [mirrored(y)-1]);
                    cellsList.add(Grid[5][0]  [mirrored(y)]  );
                    cellsList.add(Grid[5][0]  [mirrored(y)+1]);
                    cellsList.add(Grid[2][y+1][0]);
                    cellsList.add(Grid[2][y-1][0]);
                    cellsList.add(Grid[2][y]  [1]);
                    cellsList.add(Grid[2][y+1][1]);
                    cellsList.add(Grid[2][y-1][1]);
                }
                if (Grid[x][y][z].edge == 2 ){
                    cellsList.add(Grid[0][0]  [z-1]);
                    cellsList.add(Grid[0][0]  [z]  );
                    cellsList.add(Grid[0][0]  [z+1]);
                    cellsList.add(Grid[2][y]  [z+1]);
                    cellsList.add(Grid[2][y]  [z-1]);
                    cellsList.add(Grid[2][y-1][z+1]);
                    cellsList.add(Grid[2][y-1][z]  );
                    cellsList.add(Grid[2][y-1][z-1]);
                }
                if (Grid[x][y][z].edge == 3 ){
                    cellsList.add(Grid[4][0]  [mirrored(y)+1]);
                    cellsList.add(Grid[4][0]  [mirrored(y)]  );
                    cellsList.add(Grid[4][0]  [mirrored(y)-1]);
                    cellsList.add(Grid[2][y+1][9]);
                    cellsList.add(Grid[2][y-1][9]);
                    cellsList.add(Grid[2][y-1][8]);
                    cellsList.add(Grid[2][y]  [8]);
                    cellsList.add(Grid[2][y+1][8]);
                }
                if (Grid[x][y][z].edge == 4 ){
                    cellsList.add(Grid[3][0]  [z-1]);
                    cellsList.add(Grid[3][0]  [z]  );
                    cellsList.add(Grid[3][0]  [z+1]);
                    cellsList.add(Grid[x][y]  [z+1]);
                    cellsList.add(Grid[x][y]  [z-1]);
                    cellsList.add(Grid[x][y+1][z+1]);
                    cellsList.add(Grid[x][y+1][z]  );
                    cellsList.add(Grid[x][y+1][z-1]);
                }
                if (Grid[x][y][z].edge == 14){
                    cellsList.add(Grid[3][0][0]);
                    cellsList.add(Grid[3][0][1]);
                    cellsList.add(Grid[5][0][9]);
                    cellsList.add(Grid[5][0][8]);
                    cellsList.add(Grid[2][0][1]);
                    cellsList.add(Grid[2][1][0]);
                    cellsList.add(Grid[2][1][1]);
                }
                if (Grid[x][y][z].edge == 12){
                    cellsList.add(Grid[5][0][0]);
                    cellsList.add(Grid[5][0][1]);
                    cellsList.add(Grid[0][0][0]);
                    cellsList.add(Grid[0][0][1]);
                    cellsList.add(Grid[2][9][1]);
                    cellsList.add(Grid[2][8][0]);
                    cellsList.add(Grid[2][8][1]);
                }
                if (Grid[x][y][z].edge == 23){
                    cellsList.add(Grid[0][0][8]);
                    cellsList.add(Grid[0][0][9]);
                    cellsList.add(Grid[4][0][0]);
                    cellsList.add(Grid[4][0][1]);
                    cellsList.add(Grid[2][9][8]);
                    cellsList.add(Grid[2][8][8]);
                    cellsList.add(Grid[2][8][9]);
                }
                if (Grid[x][y][z].edge == 34){
                    cellsList.add(Grid[4][0][9]);
                    cellsList.add(Grid[4][0][8]);
                    cellsList.add(Grid[3][0][8]);
                    cellsList.add(Grid[3][0][9]);
                    cellsList.add(Grid[2][0][8]);
                    cellsList.add(Grid[2][1][8]);
                    cellsList.add(Grid[2][1][9]);
                }
            }
            if (Grid[x][y][z].x == 3) {                      // if side is side 3
                if (Grid[x][y][z].edge == 1) {
                    cellsList.add(Grid[5][y + 1][9]);
                    cellsList.add(Grid[5][y][9]);
                    cellsList.add(Grid[5][y - 1][9]);
                    cellsList.add(Grid[x][y + 1][z]);
                    cellsList.add(Grid[x][y - 1][z]);
                    cellsList.add(Grid[x][y][z + 1]);
                    cellsList.add(Grid[x][y + 1][z + 1]);
                    cellsList.add(Grid[x][y - 1][z + 1]);
                }
                if (Grid[x][y][z].edge == 2) {
                    cellsList.add(Grid[1][0][z - 1]);
                    cellsList.add(Grid[1][0][z]);
                    cellsList.add(Grid[1][0][z + 1]);
                    cellsList.add(Grid[x][y][z + 1]);
                    cellsList.add(Grid[x][y][z - 1]);
                    cellsList.add(Grid[x][y - 1][z + 1]);
                    cellsList.add(Grid[x][y - 1][z]);
                    cellsList.add(Grid[x][y - 1][z - 1]);
                }
                if (Grid[x][y][z].edge == 3) {
                    cellsList.add(Grid[4][y - 1][9]);
                    cellsList.add(Grid[4][y][9]);
                    cellsList.add(Grid[4][y + 1][9]);
                    cellsList.add(Grid[x][y + 1][z]);
                    cellsList.add(Grid[x][y - 1][z]);
                    cellsList.add(Grid[x][y - 1][z - 1]);
                    cellsList.add(Grid[x][y][z - 1]);
                    cellsList.add(Grid[x][y + 1][z - 1]);
                }
                if (Grid[x][y][z].edge == 4) {
                    cellsList.add(Grid[2][0][z - 1]);
                    cellsList.add(Grid[2][0][z]);
                    cellsList.add(Grid[2][0][z + 1]);
                    cellsList.add(Grid[x][y][z + 1]);
                    cellsList.add(Grid[x][y][z - 1]);
                    cellsList.add(Grid[x][y + 1][z + 1]);
                    cellsList.add(Grid[x][y + 1][z]);
                    cellsList.add(Grid[x][y + 1][z - 1]);
                }
                if (Grid[x][y][z].edge == 14) {
                    cellsList.add(Grid[5][0][9]);
                    cellsList.add(Grid[5][1][9]);
                    cellsList.add(Grid[2][0][0]);
                    cellsList.add(Grid[2][0][1]);
                    cellsList.add(Grid[3][0][1]);
                    cellsList.add(Grid[3][1][0]);
                    cellsList.add(Grid[3][1][1]);
                }
                if (Grid[x][y][z].edge == 12) {
                    cellsList.add(Grid[5][8][9]);
                    cellsList.add(Grid[5][9][9]);
                    cellsList.add(Grid[1][0][0]);
                    cellsList.add(Grid[1][0][1]);
                    cellsList.add(Grid[3][8][1]);
                    cellsList.add(Grid[3][8][0]);
                    cellsList.add(Grid[3][9][1]);
                }
                if (Grid[x][y][z].edge == 23) {
                    cellsList.add(Grid[1][0][8]);
                    cellsList.add(Grid[1][0][9]);
                    cellsList.add(Grid[4][8][9]);
                    cellsList.add(Grid[4][9][9]);
                    cellsList.add(Grid[3][9][8]);
                    cellsList.add(Grid[3][8][8]);
                    cellsList.add(Grid[3][8][9]);
                }
                if (Grid[x][y][z].edge == 34) {
                    cellsList.add(Grid[4][0][9]);
                    cellsList.add(Grid[4][1][9]);
                    cellsList.add(Grid[2][0][8]);
                    cellsList.add(Grid[2][0][9]);
                    cellsList.add(Grid[3][0][8]);
                    cellsList.add(Grid[3][1][8]);
                    cellsList.add(Grid[3][1][9]);
                }
            }
            if (Grid[x][y][z].x == 4) {                      // if side is side 4
                if (Grid[x][y][z].edge == 1) {
                    cellsList.add(Grid[0][y + 1][9]);
                    cellsList.add(Grid[0][y][9]);
                    cellsList.add(Grid[0][y - 1][9]);
                    cellsList.add(Grid[x][y + 1][z]);
                    cellsList.add(Grid[x][y - 1][z]);
                    cellsList.add(Grid[x][y][z + 1]);
                    cellsList.add(Grid[x][y + 1][z + 1]);
                    cellsList.add(Grid[x][y - 1][z + 1]);
                }
                if (Grid[x][y][z].edge == 2) {
                    cellsList.add(Grid[1][mirrored(z)-1][9]);
                    cellsList.add(Grid[1][mirrored(z)][9]);
                    cellsList.add(Grid[1][mirrored(z)+1][9]);
                    cellsList.add(Grid[x][y][z + 1]);
                    cellsList.add(Grid[x][y][z - 1]);
                    cellsList.add(Grid[x][y - 1][z + 1]);
                    cellsList.add(Grid[x][y - 1][z]);
                    cellsList.add(Grid[x][y - 1][z - 1]);
                }
                if (Grid[x][y][z].edge == 3) {
                    cellsList.add(Grid[3][y - 1][9]);
                    cellsList.add(Grid[3][y][9]);
                    cellsList.add(Grid[3][y + 1][9]);
                    cellsList.add(Grid[x][y + 1][z]);
                    cellsList.add(Grid[x][y - 1][z]);
                    cellsList.add(Grid[x][y - 1][z - 1]);
                    cellsList.add(Grid[x][y][z - 1]);
                    cellsList.add(Grid[x][y + 1][z - 1]);
                }
                if (Grid[x][y][z].edge == 4) {
                    cellsList.add(Grid[2][mirrored(z)-1][9]);
                    cellsList.add(Grid[2][mirrored(z)][9]);
                    cellsList.add(Grid[2][mirrored(z)+1][9]);
                    cellsList.add(Grid[x][y][z + 1]);
                    cellsList.add(Grid[x][y][z - 1]);
                    cellsList.add(Grid[x][y + 1][z + 1]);
                    cellsList.add(Grid[x][y + 1][z]);
                    cellsList.add(Grid[x][y + 1][z - 1]);
                }
                if (Grid[x][y][z].edge == 14) {
                    cellsList.add(Grid[0][0][9]);
                    cellsList.add(Grid[0][1][9]);
                    cellsList.add(Grid[2][8][9]);
                    cellsList.add(Grid[2][9][9]);
                    cellsList.add(Grid[4][0][1]);
                    cellsList.add(Grid[4][1][0]);
                    cellsList.add(Grid[4][1][1]);
                }
                if (Grid[x][y][z].edge == 12) {
                    cellsList.add(Grid[0][8][9]);
                    cellsList.add(Grid[0][9][9]);
                    cellsList.add(Grid[1][9][9]);
                    cellsList.add(Grid[1][8][9]);
                    cellsList.add(Grid[4][8][1]);
                    cellsList.add(Grid[4][8][0]);
                    cellsList.add(Grid[4][9][1]);
                }
                if (Grid[x][y][z].edge == 23) {
                    cellsList.add(Grid[1][1][9]);
                    cellsList.add(Grid[1][0][9]);
                    cellsList.add(Grid[3][8][9]);
                    cellsList.add(Grid[3][9][9]);
                    cellsList.add(Grid[4][9][8]);
                    cellsList.add(Grid[4][8][8]);
                    cellsList.add(Grid[4][8][9]);
                }
                if (Grid[x][y][z].edge == 34) {
                    cellsList.add(Grid[2][0][9]);
                    cellsList.add(Grid[2][1][9]);
                    cellsList.add(Grid[3][0][9]);
                    cellsList.add(Grid[3][1][9]);
                    cellsList.add(Grid[4][0][8]);
                    cellsList.add(Grid[4][1][8]);
                    cellsList.add(Grid[4][1][9]);
                }
            }
            if (Grid[x][y][z].x == 5) {                      // if side is side 5
                if (Grid[x][y][z].edge == 1) {
                    cellsList.add(Grid[0][y + 1][0]);
                    cellsList.add(Grid[0][y][0]);
                    cellsList.add(Grid[0][y - 1][0]);
                    cellsList.add(Grid[x][y + 1][z]);
                    cellsList.add(Grid[x][y - 1][z]);
                    cellsList.add(Grid[x][y][z + 1]);
                    cellsList.add(Grid[x][y + 1][z + 1]);
                    cellsList.add(Grid[x][y - 1][z + 1]);
                }
                if (Grid[x][y][z].edge == 2) {
                    cellsList.add(Grid[1][mirrored(z)-1][0]);
                    cellsList.add(Grid[1][mirrored(z)][0]);
                    cellsList.add(Grid[1][mirrored(z)+1][0]);
                    cellsList.add(Grid[x][y][z + 1]);
                    cellsList.add(Grid[x][y][z - 1]);
                    cellsList.add(Grid[x][y - 1][z + 1]);
                    cellsList.add(Grid[x][y - 1][z]);
                    cellsList.add(Grid[x][y - 1][z - 1]);
                }
                if (Grid[x][y][z].edge == 3) {
                    cellsList.add(Grid[3][y - 1][0]);
                    cellsList.add(Grid[3][y][0]);
                    cellsList.add(Grid[3][y + 1][0]);
                    cellsList.add(Grid[x][y + 1][z]);
                    cellsList.add(Grid[x][y - 1][z]);
                    cellsList.add(Grid[x][y - 1][z - 1]);
                    cellsList.add(Grid[x][y][z - 1]);
                    cellsList.add(Grid[x][y + 1][z - 1]);
                }
                if (Grid[x][y][z].edge == 4) {
                    cellsList.add(Grid[2][mirrored(z)-1][0]);
                    cellsList.add(Grid[2][mirrored(z)][0]);
                    cellsList.add(Grid[2][mirrored(z)+1][0]);
                    cellsList.add(Grid[x][y][z + 1]);
                    cellsList.add(Grid[x][y][z - 1]);
                    cellsList.add(Grid[x][y + 1][z + 1]);
                    cellsList.add(Grid[x][y + 1][z]);
                    cellsList.add(Grid[x][y + 1][z - 1]);
                }
                if (Grid[x][y][z].edge == 34) {
                    cellsList.add(Grid[2][0][0]);
                    cellsList.add(Grid[2][1][0]);
                    cellsList.add(Grid[3][0][0]);
                    cellsList.add(Grid[3][1][0]);
                    cellsList.add(Grid[5][0][8]);
                    cellsList.add(Grid[5][1][8]);
                    cellsList.add(Grid[5][1][9]);
                }
                if (Grid[x][y][z].edge == 23) {
                    cellsList.add(Grid[3][8][0]);
                    cellsList.add(Grid[3][9][0]);
                    cellsList.add(Grid[1][0][0]);
                    cellsList.add(Grid[1][1][0]);
                    cellsList.add(Grid[5][8][9]);
                    cellsList.add(Grid[5][8][8]);
                    cellsList.add(Grid[5][9][8]);
                }
                if (Grid[x][y][z].edge == 12) {
                    cellsList.add(Grid[1][8][0]);
                    cellsList.add(Grid[1][9][0]);
                    cellsList.add(Grid[0][8][0]);
                    cellsList.add(Grid[0][9][0]);
                    cellsList.add(Grid[5][9][1]);
                    cellsList.add(Grid[5][8][1]);
                    cellsList.add(Grid[5][8][0]);
                }
                if (Grid[x][y][z].edge == 14) {
                    cellsList.add(Grid[0][0][0]);
                    cellsList.add(Grid[0][1][0]);
                    cellsList.add(Grid[2][8][0]);
                    cellsList.add(Grid[2][9][0]);
                    cellsList.add(Grid[5][0][1]);
                    cellsList.add(Grid[5][1][1]);
                    cellsList.add(Grid[5][1][0]);
                }
            }
        }
        Grid[x][y][z].surroundingBombs = cellsList;
        return cellsList;
    }
    private int mirrored(int i){
        if      (i==0) return 9;
        else if (i==1) return 8;
        else if (i==2) return 7;
        else if (i==3) return 6;
        else if (i==4) return 5;
        else if (i==5) return 4;
        else if (i==6) return 3;
        else if (i==7) return 2;
        else if (i==8) return 1;
        else if (i==9) return 0;
        else           return 99;
    }
    static void setFlaggedCells(int cells){ flaggedCells = cells; bombs.setText("Bombs: "+flaggedCells+"/"+totalBombs); }
    static int getFlaggedCells(){ return flaggedCells; }
    static void setFlaggedBombs(int bombCount){ flaggedBombs = bombCount; }
    static int getFlaggedBombs(){ return flaggedBombs; }
    static int getTotalBombs(){ return totalBombs; }
    private void readHighScores() throws IOException{
        // CSV Schema: ID, difficulty, firstName, lastName, time
        File highscores = new File(HIGH_SCORES_LOCATION);
        if (highscores.createNewFile()) System.out.println("highScores.csv created.");
        else System.out.println("highScores.csv already exists.");
        Scanner scanner1 = new Scanner(highscores);
        while (scanner1.hasNextLine()){
            Scanner scanner = new Scanner(scanner1.nextLine());
            scanner.useDelimiter(",");
            String IDs = scanner.next();
            long ID = Long.parseLong(IDs);
            String difficulty = scanner.next();
            String firstName = scanner.next();
            String lastName = scanner.next();
            String times = scanner.next();
            int time = Integer.parseInt(times);
            highScoresMap.put(ID, new HighScore(ID, difficulty, firstName, lastName, time));
        }
    }
    private void refreshTableData(String search, String field){ // Method to execute search on high scores
        List<HighScore> tempList = new ArrayList<>();
        for (long id:highScoresMap.keySet()){
            HighScore current = highScoresMap.get(id);
            if      (field.equals("Date") && current.getID().toLowerCase().contains(search.toLowerCase())) tempList.add(current);
            else if (field.equals("Difficulty") && current.getDifficulty().toLowerCase().contains(search.toLowerCase())) tempList.add(current);
            else if (field.equals("First Name") && current.getFirstName().toLowerCase().contains(search.toLowerCase())) tempList.add(current);
            else if (field.equals("Last Name") && current.getLastName().toLowerCase().contains(search.toLowerCase())) tempList.add(current);
            else if (field.equals("Last Name") && current.getLastName().toLowerCase().contains(search.toLowerCase())) tempList.add(current);
            else if (field.equals("Time") && Integer.toString(current.getTime()).toLowerCase().contains(search.toLowerCase())) tempList.add(current);
            else if (field.equals("")) tempList.add(current);
        }
        ObservableList<HighScore> result = FXCollections.observableList(tempList);
        table.setItems(result);
        table.getSortOrder().setAll(timeCol);
    }
    private void refreshTableData(){ refreshTableData("", ""); }
    private void writeHighScores() throws IOException{
        PrintWriter clear = new PrintWriter(HIGH_SCORES_LOCATION);
        clear.print("");
        clear.close();
        FileWriter writer = new FileWriter(HIGH_SCORES_LOCATION);
        for (Long key:highScoresMap.keySet()){
            HighScore current = highScoresMap.get(key);
            writer.append(current.getRawID());
            writer.append(",");
            writer.append(current.getDifficulty());
            writer.append(",");
            writer.append(current.getFirstName());
            writer.append(",");
            writer.append(current.getLastName());
            writer.append(",");
            writer.append(Integer.toString(current.getTime()));
            writer.append("\n");
        }
        writer.flush();
        writer.close();
    }
    static void gameLost(){ gameLost = true; }
    static void gameWon() { gameWon  = true; }
}