package footsiebot.gui;

import footsiebot.Core;
import footsiebot.ai.Suggestion;
import footsiebot.datagathering.Article;
import java.io.*;
import java.time.*;
import java.util.*;
import javafx.stage.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.TextAlignment;
import javafx.geometry.*;
import javafx.animation.*;
import javafx.collections.*;
import javafx.util.Duration;
import javafx.beans.property.*;
import javafx.application.Platform;


public class GUIcore implements IGraphicalUserInterface {
    private String style; //holds the filename of the stylesheet to use
    private ListProperty<Node> messages; /*keeps a record of the Messages on the
                                         messageBoard - used for resizing
                                         purposes*/
    private Core core; /*the instance of Core which this instance of GUIcore
                       belongs to*/

    private Timeline newDataTimeline; /*Timeline to regularly call the
                                      onNewDataAvailable() method in the Core*/
    private Timeline tradingHourTimeline; /*Timeline to regularly call the
                                          onTradingHour() method in the Core*/

    private Thread dataDownload; //a Thread to perform the download of data
    private volatile Boolean closing = false; /*a boolean value which indicates
                                              whether the application is
                                              closing*/

    private Stage stage;
    private Scene scene;
    private StackPane root;

    private StackPane topBar;
    private ImageView settingsIcon;

    private StackPane chatPane;
    private ScrollPane boardWrapper;
    private VBox messageBoard;
    private StackPane inputWrapper;
    private Rectangle inputVisual;
    private TextField input;
    private Button btnSend;

    private StackPane sidePane;
    private StackPane newsPane;
    private ScrollPane newsWrapper;
    private FlowPane newsBoard;
    private Label noNews;

    private GridPane settingsPane;
    private ComboBox<String> timeSelector;
    private Spinner<Double> changeSelector;
    private Button saveChanges;
    private FadeTransition settingsPaneTrans;
    private FadeTransition newsPaneTrans;
    private RotateTransition settingsIconTrans;

   /**
    * Constructor for the user interface using default styling
    *
    * @param primaryStage the initial stage of the application
    * @param core the core of the application
    */
    public GUIcore(Stage primaryStage, Core core) {
        stage = primaryStage;
        style = "main";
        this.core = core;
        setup();
    }

   /**
    * Constructor for the user interface using custom styling
    *
    * @param primaryStage the initial stage of the application
    * @param style the name of the css file used for styling
    * @param core the core of the application
    */
    public GUIcore(Stage primaryStage, String style, Core core) {
        stage = primaryStage;
        this.style = style;
        this.core = core;
        setup();
    }

   /**
    * Builds the user interface on the initial stage of the application
    */
    private void setup() {
        if (core.FULLSCREEN)
            stage.setFullScreen(true);

        stage.setMinWidth(500);
        stage.setMinHeight(299);

        root = new StackPane();
        root.setId("root");

        scene = new Scene(root, 800, 600);
        scene.getStylesheets().setAll("file:src/gui/css/" + style + ".css");

        messages = new SimpleListProperty<Node>();

        initChat();
        initSide();
        initTop();
        setupListeners();
        setupActions();
        startDataDownload();
        startNewDataTimeline();
        startTradingHourTimeline();

        root.getChildren().addAll(chatPane, sidePane, topBar);
        root.setAlignment(topBar, Pos.TOP_LEFT);
        root.setAlignment(chatPane, Pos.BOTTOM_LEFT);
        root.setAlignment(sidePane, Pos.BOTTOM_RIGHT);

        stage.setTitle("Footsiebot");
        stage.setScene(scene);
        stage.hide();
        stage.show();
    }

   /**
    * Initialises the chat pane and all nodes on it
    */
    private void initChat() {
        chatPane = new StackPane();
        chatPane.setId("chat-pane");
        chatPane.minHeightProperty().bind(scene.heightProperty().subtract(45));
        chatPane.maxHeightProperty().bind(scene.heightProperty().subtract(45));
        chatPane.minWidthProperty().bind(scene.widthProperty().subtract(250));
        chatPane.maxWidthProperty().bind(scene.widthProperty().subtract(250));

        boardWrapper = new ScrollPane();
        boardWrapper.setId("board-wrapper");
        boardWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        boardWrapper.minWidthProperty().bind(chatPane.widthProperty());
        boardWrapper.maxWidthProperty().bind(chatPane.widthProperty());

        messageBoard = new VBox();
        Insets boardPadding = new Insets(0, 0, 0, 16);
        messageBoard.setPadding(boardPadding);
        messageBoard.setId("message-board");
        messageBoard.minWidthProperty().bind(chatPane.widthProperty().subtract(18));
        messageBoard.maxWidthProperty().bind(chatPane.widthProperty().subtract(18));

        inputWrapper = new StackPane();
        Insets inputPadding = new Insets(0, 5, 0, 5);
        Insets inputMargin = new Insets(0, 0, 0, 5);
        inputWrapper.setPadding(inputPadding);
        inputWrapper.setId("input-wrapper");
        inputWrapper.setMaxHeight(45);
        inputWrapper.setMinHeight(45);

        inputVisual = new Rectangle();
        inputVisual.setHeight(35);
        inputVisual.setId("input-visual");
        inputVisual.widthProperty().bind(chatPane.widthProperty().subtract(60));

        input = new TextField();
        input.setId("input");
        input.minWidthProperty().bind(chatPane.widthProperty().subtract(65));
        input.maxWidthProperty().bind(chatPane.widthProperty().subtract(65));
        input.setMinHeight(25);
        input.setMaxHeight(25);
        input.setPromptText("Type something here...");

        btnSend = new Button("Send");
        btnSend.setId("send-button");

        inputWrapper.getChildren().addAll(inputVisual, input, btnSend);
        inputWrapper.setAlignment(Pos.CENTER_LEFT);
        inputWrapper.setMargin(input, inputMargin);

        inputWrapper.setAlignment(btnSend, Pos.CENTER_RIGHT);
        boardWrapper.setContent(messageBoard);
        chatPane.getChildren().addAll(inputWrapper, boardWrapper);
        chatPane.setAlignment(inputWrapper, Pos.BOTTOM_LEFT);
        chatPane.setAlignment(boardWrapper, Pos.TOP_LEFT);
        Insets boardWrapperMargins = new Insets(0, 0, 45, 0);
        chatPane.setMargin(boardWrapper, boardWrapperMargins);
    }

   /**
    * Initialises the sidepane and all nodes on it
    */
    private void initSide() {
        sidePane = new StackPane();
        sidePane.setId("side-pane");
        sidePane.setMinWidth(250);
        sidePane.setMaxWidth(250);
        sidePane.minHeightProperty().bind(scene.heightProperty().subtract(45));
        sidePane.maxHeightProperty().bind(scene.heightProperty().subtract(45));

        settingsPane = new GridPane();
        settingsPane.setId("settings-pane");
        settingsPane.setVisible(false);
        Insets settingsPadding = new Insets(10, 10, 10, 10);
        settingsPane.setPadding(settingsPadding);
        settingsPaneTrans = new FadeTransition(Duration.millis(500), settingsPane);

        newsPane = new StackPane();
        newsPane.setId("news-pane");
        newsPane.setVisible(true);
        newsPane.minWidthProperty().bind(sidePane.widthProperty());
        newsPane.maxWidthProperty().bind(sidePane.widthProperty());
        newsPaneTrans = new FadeTransition(Duration.millis(500), newsPane);

        newsWrapper = new ScrollPane();
        newsWrapper.setId("news-wrapper");
        newsWrapper.minWidthProperty().bind(sidePane.widthProperty());
        newsWrapper.maxWidthProperty().bind(sidePane.widthProperty());
        newsWrapper.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        newsBoard = new FlowPane();
        newsBoard.setId("news-board");
        newsBoard.setVgap(10);

        noNews = new Label("Oh no! It looks like we don't have any news for you right now!");
        noNews.setId("no-news");
        noNews.setWrapText(true);
        noNews.setTextAlignment(TextAlignment.CENTER);

        Insets wrapperPadding = new Insets(0, 0, 0, -1);
        newsWrapper.setPadding(wrapperPadding);
        newsWrapper.setContent(newsBoard);

        initSettings();

        newsPane.getChildren().addAll(newsWrapper, noNews);
        sidePane.getChildren().addAll(settingsPane, newsPane);
    }

   /**
    * Initialises the settings page and all nodes on it
    */
    private void initSettings() {
        timeSelector = new ComboBox<String>();
        timeSelector.setMaxWidth(85);
        timeSelector.setMinWidth(85);

        ObservableList<String> timeOptions = FXCollections.observableArrayList();
        int hour = 8;
        for (int i = 0; i  < 20; i++) {
            if (i%2 == 0)
                timeOptions.add(hour + ":00");
            else
                timeOptions.add(hour++ + ":30");
        }
        timeSelector.setItems(timeOptions);
        timeSelector.setValue(tradingTimeToString());

        changeSelector = new Spinner<Double>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.00, 10.00, core.LARGE_CHANGE_THRESHOLD, 0.05));
        changeSelector.setMaxWidth(85);
        changeSelector.setMinWidth(85);
        changeSelector.setEditable(true);

        CheckBox fullscrnCkB = new CheckBox();
        fullscrnCkB.setAllowIndeterminate(false);
        fullscrnCkB.setSelected(core.FULLSCREEN);

        Button saveChanges = new Button("Save Changes");
        saveChanges.setMinWidth(100);
        saveChanges.setMaxWidth(100);
        saveChanges.setOnAction(e -> {
            if ((String) timeSelector.getValue() != null)
                core.updateSettings(timeSelector.getValue(), changeSelector.getValue(), fullscrnCkB.isSelected());
            else
                core.updateSettings(null, changeSelector.getValue(), fullscrnCkB.isSelected());

            saveChanges.setDisable(true);
        });

        saveChanges.setDisable(true);

        Button cancelChanges = new Button("Cancel");
        cancelChanges.setMinWidth(100);
        cancelChanges.setMaxWidth(100);
        cancelChanges.setOnAction(e -> {
            timeSelector.setValue(tradingTimeToString());
            changeSelector.getValueFactory().setValue(core.LARGE_CHANGE_THRESHOLD);
            saveChanges.setDisable(true);
        });

        Button summaryBtn = new Button("Show Daily Summary Now");
        summaryBtn.setOnAction(e -> {
            core.onTradingHour();
        });

        ColumnConstraints labelCol = new ColumnConstraints(22.8, 22.8, 22.8);
        labelCol.setHalignment(HPos.RIGHT);
        ColumnConstraints buttonLeftCol = new ColumnConstraints(91.2, 91.2, 91.2);
        ColumnConstraints buttonRightCol = new ColumnConstraints(22.8, 22.8, 22.8);
        ColumnConstraints inputCol = new ColumnConstraints(91.2, 91.2, 91.2);

        RowConstraints timeRow = new RowConstraints(50, 50, 50);
        RowConstraints changeRow = new RowConstraints(50, 50, 50);
        RowConstraints fullscreenRow = new RowConstraints(30, 30, 30);
        RowConstraints buttonsRow = new RowConstraints(50, 50, 50);
        RowConstraints summaryRow = new RowConstraints(30, 30, 30);

        Insets labelPadding = new Insets(0, 7, 0, 0);

        Label timeDesc = new Label("Time for daily summary:");
        timeDesc.setWrapText(true);
        timeDesc.setTextAlignment(TextAlignment.RIGHT);
        timeDesc.setPadding(labelPadding);
        Label changeDesc = new Label("Percentage change for warnings:");
        changeDesc.setWrapText(true);
        changeDesc.setTextAlignment(TextAlignment.RIGHT);
        changeDesc.setPadding(labelPadding);
        Label fullscrnLbl = new Label("Open in fullscreen:");
        fullscrnLbl.setPadding(labelPadding);

        Label percentSign = new Label("%");
        percentSign.setId("percent-sign");

        settingsPane.getColumnConstraints().addAll(labelCol, buttonLeftCol, buttonRightCol, inputCol);
        settingsPane.getRowConstraints().addAll(timeRow, changeRow, fullscreenRow, buttonsRow, summaryRow);
        settingsPane.add(timeDesc, 0, 0);
        settingsPane.add(timeSelector, 3, 0);
        settingsPane.add(changeDesc, 0, 1);
        settingsPane.add(changeSelector, 3, 1);
        settingsPane.add(fullscrnLbl, 0, 2);
        settingsPane.add(fullscrnCkB, 3, 2);
        settingsPane.add(cancelChanges, 2, 3);
        settingsPane.add(saveChanges, 0, 3);
        settingsPane.add(summaryBtn, 0, 4);
        settingsPane.add(percentSign, 3, 1);

        settingsPane.setColumnSpan(timeDesc, 3);
        settingsPane.setColumnSpan(changeDesc, 3);
        settingsPane.setColumnSpan(saveChanges, 2);
        settingsPane.setColumnSpan(cancelChanges, 2);
        settingsPane.setColumnSpan(summaryBtn, 4);
        settingsPane.setColumnSpan(fullscrnLbl, 3);
        settingsPane.setColumnSpan(fullscrnCkB, 4);
        settingsPane.setHalignment(saveChanges, HPos.CENTER);
        settingsPane.setHalignment(cancelChanges, HPos.CENTER);
        settingsPane.setHalignment(summaryBtn, HPos.CENTER);
        settingsPane.setHalignment(percentSign, HPos.CENTER);

        timeSelector.valueProperty().addListener(e -> {
            if (timeSelector.getValue() != tradingTimeToString())
                saveChanges.setDisable(false);
        });

        changeSelector.valueProperty().addListener(e -> {
            if (changeSelector.getValue() != core.LARGE_CHANGE_THRESHOLD)
                saveChanges.setDisable(false);
        });

        fullscrnCkB.setOnAction(e -> {
            saveChanges.setDisable(false);
        });
    }

   /**
    * Initialises the top bar and all nodes on it
    */
    private void initTop() {
        topBar = new StackPane();
        topBar.setId("top-bar");
        topBar.setMinHeight(45);
        topBar.setMaxHeight(45);

        Label logo = new Label("Footsiebot");
        Calendar today = Calendar.getInstance();
        if ((today.get(Calendar.DAY_OF_MONTH) == 14) && (today.get(Calendar.MONTH) == Calendar.FEBRUARY))
            logo.setId("logo-valentines");
        else
            logo.setId("logo");

        settingsIcon = new ImageView("file:src/img/settings.png");
        settingsIcon.setPreserveRatio(true);
        settingsIcon.setFitWidth(27);
        settingsIcon.setId("settings-icon");
        topBar.getChildren().addAll(settingsIcon, logo);
        topBar.setAlignment(settingsIcon, Pos.CENTER_RIGHT);
        Insets settingsIconMargin = new Insets(0, 10, 0, 0);
        topBar.setMargin(settingsIcon, settingsIconMargin);
        settingsIconTrans = new RotateTransition(Duration.millis(300), settingsIcon);
    }

   /**
    * Sets the listeners for Nodes in the Stage
    */
    private void setupListeners() {
        //resize nodes to conform to layout
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newsBoard.getHeight() > newsWrapper.getHeight()) {
                newsBoard.setMinWidth(sidePane.getWidth() - 15);
                newsBoard.setMaxWidth(sidePane.getWidth() - 15);
            } else {
                newsBoard.setMinWidth(sidePane.getWidth());
                newsBoard.setMaxWidth(sidePane.getWidth());
            }
            resizeNews(newsBoard.getWidth());
            stage.setScene(scene);
        });

        //resizes nodes to conform to layout
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newsBoard.getHeight() > newsWrapper.getHeight()) {
                newsBoard.setMinWidth(sidePane.getWidth() - 15);
                newsBoard.setMaxWidth(sidePane.getWidth() - 15);
            } else {
                newsBoard.setMinWidth(sidePane.getWidth());
                newsBoard.setMaxWidth(sidePane.getWidth());
            }

            resizeMessages();
            resizeNews(newsBoard.getMinWidth());

            messageBoard.applyCss();
            messageBoard.layout();
            stage.setScene(scene);
        });

        //keeps the message board scrolled to the bottom when a new message is added
        messageBoard.heightProperty().addListener((obs, oldVal, newVal) -> {
            messageBoard.applyCss();
            messageBoard.layout();
            boardWrapper.setVvalue(1);
        });

        //resizes NewsBlocks on the news board to accomodate for the scroll bar
        newsBoard.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newsBoard.getHeight() > newsWrapper.getHeight()) {
                newsBoard.setMinWidth(sidePane.getWidth() - 15);
                newsBoard.setMaxWidth(sidePane.getWidth() - 15);
            } else {
                newsBoard.setMinWidth(sidePane.getWidth());
                newsBoard.setMaxWidth(sidePane.getWidth());
            }

            noNews.setVisible((newsBoard.getChildren().size() == 0));

            resizeNews(newsBoard.getMinWidth());
            newsBoard.applyCss();
            newsBoard.layout();
        });

        //resizes messages on the message board
        messages.addListener((obs, oldVal, newVal) -> {
            resizeMessages();
        });
    }

   /**
    * Sets the actions performed by Nodes in the Stage
    */
    private void setupActions() {
        //send user input
        input.setOnAction(e -> {
            onUserInput();
        });

        //send user input
        btnSend.setOnAction(e -> {
            onUserInput();
        });

        //play the clockwise rotation animation for the settings icon
        settingsIcon.setOnMouseEntered(e -> {
            settingsIconTrans.setFromAngle(0);
            settingsIconTrans.setToAngle(45);
            settingsIconTrans.play();
        });

        //play the anticlockwise rotation animation for the settings icon
        settingsIcon.setOnMouseExited(e -> {
            settingsIconTrans.stop();
            settingsIconTrans.setFromAngle(45);
            settingsIconTrans.setToAngle(0);
            settingsIconTrans.play();
        });

        //change the side pane between the news board and the settings page
        settingsIcon.setOnMouseClicked(e -> {
            changeSidePane();
        });
    }

   /**
    * Swaps the side pane between the news board and the settings page
    */
    private void changeSidePane() {
        if (settingsPane.visibleProperty().getValue()) {
            newsPane.setVisible(true);
            newsPaneTrans.setFromValue(0);
            newsPaneTrans.setToValue(1);
            newsPaneTrans.setOnFinished(event -> {
                newsPane.setVisible(true);
            });

            settingsPane.setVisible(true);
            settingsPaneTrans.setFromValue(1);
            settingsPaneTrans.setToValue(0);
            settingsPaneTrans.setOnFinished(event -> {
                settingsPane.setVisible(false);
            });

            settingsPane.setDisable(true);
            newsPaneTrans.play();
            settingsPaneTrans.play();
            newsPane.setDisable(false);
        } else {
            newsPaneTrans.setFromValue(1);
            newsPaneTrans.setToValue(0);
            newsPaneTrans.setOnFinished(event -> {
                newsPane.setVisible(false);
            });

            settingsPane.setVisible(true);
            settingsPaneTrans.setFromValue(0);
            settingsPaneTrans.setToValue(1);
            settingsPaneTrans.setOnFinished(event -> {
                settingsPane.setVisible(true);
            });
            newsPane.setDisable(true);
            newsPaneTrans.play();
            settingsPaneTrans.play();
            settingsPane.setDisable(false);
        }
    }

   /**
    * Converts the TRADING_TIME variable in Core to a String
    *
    * @return a String representation of the TRADING_TIME
    */
    private String tradingTimeToString() {
        Long hr = core.TRADING_TIME / 3600000;
        String hrStr = hr.toString();

        if (hr < 10)
            hrStr = "0" + hr;

        if (core.TRADING_TIME % 3600000 != 0)
            return hrStr + ":30";
        else
            return hrStr + ":00";
    }

   /**
    * Creates a background thread to scrape the LSE website
    * regularly, so that data is available when needed.
    */
    private void startDataDownload(){
        dataDownload = new Thread(() -> {
            try {
                while(!closing){
                    core.downloadNewData();
                    if(closing){
                        break;
                    }
                    Thread.sleep(core.DOWNLOAD_RATE);
                }
            }
            catch (InterruptedException e){
                closing = true;
                Thread.currentThread().interrupt();
                System.out.println("Thread received interrupt");
                return;//Maybe?
            }
            catch (Exception e) {
                // should not be able to get here...
                System.out.println("Error in thread");
                e.printStackTrace();
            }
        },"ddthread");
        dataDownload.start();
    }

   /**
    * Stops the data gathering background thread when the application is closing
    */
    public void stopDataDownload(){
        closing = true;
        try {
            dataDownload.interrupt();//NOTE: for some reason, this does not seems to set the Thread.interrupted() flag.
            dataDownload.setName("closing");//Because of the above note, this appalling hack is used. I'm sorry.
            System.out.println(dataDownload.getName());
            System.out.println("Interrupted thread");
            dataDownload.join();
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("Stopped download thread");
    }

   /**
    * Calls the suggestionIrrelevant() method in the Core if the remove message button is pressed
    *
    * @param s the suggestion which is irrelevant
    */
    public void suggestionIrrelevant(Suggestion s){
        core.suggestionIrrelevant(s);
    }

   /**
    * Starts the newDataTimeline to run the core action regularly
    * REF: http://tomasmikula.github.io/blog/2014/06/04/timers-in-javafx-and-reactfx.html
    */
    private void startNewDataTimeline() {
        newDataTimeline = new Timeline(new KeyFrame(
            Duration.millis(core.DATA_REFRESH_RATE),
            ae -> core.onNewDataAvailable()));
        newDataTimeline.setCycleCount(Animation.INDEFINITE);
        newDataTimeline.playFrom(Duration.millis(core.DATA_REFRESH_RATE - core.DOWNLOAD_RATE)); //Running the core function at regular times, but starting soon after program startup
    }

   /**
    * Starts the tradingHourTimeline to run the core action regularly
    */
    public void startTradingHourTimeline() {
        tradingHourTimeline = new Timeline(new KeyFrame(
            Duration.millis(86400000),//24 hour refresh time
            ae -> core.onTradingHour()));

        newDataTimeline.setCycleCount(Animation.INDEFINITE);
        Long timeOfDayInMillis = ((Instant.now().toEpochMilli())%86400000);
        Long targetTimeOfDay = core.TRADING_TIME;
        Long startDuration;

        if (timeOfDayInMillis > targetTimeOfDay)
            startDuration = (timeOfDayInMillis - targetTimeOfDay);
        else
            startDuration = 86400000-(targetTimeOfDay - timeOfDayInMillis);

        tradingHourTimeline.playFrom(Duration.millis(startDuration));
        //Skips forward by the current time of day + the trading hour time.
    }

   /**
    * Stops the tradingHourTimeline
    */
    public void stopTradingHourTimeline(){
        if (tradingHourTimeline != null) {
            tradingHourTimeline.stop();
            tradingHourTimeline = null;
        }
    }

   /**
    * Manages input from the user
    */
    private void onUserInput() {
        if (checkInput()) {
            messageBoard.getChildren().add(new Message(input.getText().trim(), true, null, this));
            core.onUserInput(input.getText().trim());
            messages.setValue(messageBoard.getChildren());
            input.clear();
        }
    }

   /**
    * Sets the css used for the application
    *
    * @param style the name of the css file to be used
    */
    public void setStyle(String style) {
        this.style = style;
        scene.getStylesheets().setAll("file:src/gui/css/" + style + ".css");
        stage.setScene(scene);
    }

   /**
    * Displays a suggestion message from the system to the user
    *
    * @param msg a string containing the message to be displayed
    * @param sugg a Suggestion from the AI
    */
    public void displayMessage(String msg, Suggestion sugg) {
        if (msg != null) {
            messageBoard.getChildren().add(new Message(msg, false, sugg, this));
        }
        messages.setValue(messageBoard.getChildren());
    }

   /**
    * Displays a child message from the system to the user
    *
    * @param msg a string containing the message to be displayed
    * @param parent the parent message
    */
    public void displayMessage(String msg, Message parent) {
        if (msg != null) {
            if (parent != null)
                messageBoard.getChildren().add(new Message(msg, this, parent));
            else
                messageBoard.getChildren().add(new Message(msg, false, null, this));
        }
        messages.setValue(messageBoard.getChildren());
    }

   /**
    * Displays a simple message to the user
    *
    * @param msg the message to be displayed
    */
    public void displayMessage(String msg){
        if (msg != null) {
            messageBoard.getChildren().add(new Message(msg, false, null, this));
        }
        messages.setValue(messageBoard.getChildren());
    }

   /**
    * Displays news results
    *
    * @param news the array of Articles to displayed
    * @param Suggestion a Suggestion from the AI - can be null
    */
    public void displayResults(Article[] news, Suggestion s) {
        if (news != null) {
            newsBoard.getChildren().clear();
            for (Article a : news) {
                if (a != null)
                    newsBoard.getChildren().add(new NewsBlock(a, (sidePane.getWidth() - 15), core, this));
            }
        }
    }

   /**
    * Resizes the messages displayed
    */
    private void resizeMessages() {
        if (messageBoard.getChildren().size() > 25)
            messageBoard.getChildren().remove(0, (messageBoard.getChildren().size() - 25));

        for (int i = 0; i < messageBoard.getChildren().size(); i++) {
            if (messageBoard.getChildren().get(i) instanceof Message) {
                Message tmp = (Message) messageBoard.getChildren().get(i);
                tmp.resize(chatPane.getWidth());
            }
        }
    }

   /**
    * Resizes news blocks in the news pane
    *
    * @param width the current width of the news pane
    */
    private void resizeNews(double width) {
        for (int i = 0; i < newsBoard.getChildren().size(); i++) {
            if (newsBoard.getChildren().get(i) instanceof NewsBlock) {
                NewsBlock tmp = (NewsBlock) newsBoard.getChildren().get(i);
                tmp.resize(width);
            }
        }
    }

   /**
    * Verifies the input
    *
    * @return true if the input is valid, false if not
    */
    private boolean checkInput() {
        String in = input.getText();
        boolean flag = false;
        for (int i = 0; i < in.length(); i++) {
            if (in.charAt(i) != ' ')
                return true;
        }
        input.clear();
        return false;
    }

   /**
    * Accessor for the message board
    *
    * @return the message board of the GUI
    */
    public VBox getMessageBoard() {
        return messageBoard;
    }

   /**
    * Accessor for the news board
    *
    * @return the news board of the GUI
    */
    public FlowPane getNewsBoard() {
        return newsBoard;
    }

   /**
    * Accessor for stage
    *
    * @return the stage used by the GUI
    */
    public Stage getStage() {
        return stage;
    }
}
