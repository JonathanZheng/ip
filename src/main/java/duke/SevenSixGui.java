package duke;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Provides a JavaFX graphical interface for the SevenSix chatbot.
 */
public class SevenSixGui extends Application {
    /** The conversation messages displayed in the scroll pane. */
    private VBox dialogContainer;
    /** The scroll pane containing the conversation. */
    private ScrollPane scrollPane;
    /** The text field where the user enters a command. */
    private TextField userInput;
    /** The button that submits the current command. */
    private Button sendButton;
    /** The command processor shared with the console interface. */
    private final SevenSix chatbot;

    /**
     * Creates the GUI application and its command processor.
     */
    public SevenSixGui() {
        chatbot = new SevenSix();
    }

    /**
     * Creates and displays the SevenSix window.
     *
     * @param stage the primary JavaFX window.
     */
    @Override
    public void start(Stage stage) {
        VBox header = createHeader();
        scrollPane = createConversation();
        HBox inputBar = createInputBar();
        configureInputHandlers();
        BorderPane mainLayout = createMainLayout(header, inputBar);
        configureStage(stage, mainLayout);
        showGreeting();
    }

    /** Creates the title and subtitle displayed above the conversation.
     *
     * @return the configured conversation header.
     */
    private VBox createHeader() {
        Label title = new Label("SevenSix");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label subtitle = new Label("Your friendly task assistant");
        subtitle.setStyle("-fx-text-fill: #64748b;");
        VBox header = new VBox(3.0, title, subtitle);
        header.setPadding(new Insets(18.0, 20.0, 14.0, 20.0));
        return header;
    }

    /** Creates the scrollable conversation area.
     *
     * @return the configured conversation scroll pane.
     */
    private ScrollPane createConversation() {
        dialogContainer = new VBox(12.0);
        dialogContainer.setPadding(new Insets(16.0));
        dialogContainer.setStyle("-fx-background-color: white;");
        ScrollPane conversation = new ScrollPane(dialogContainer);
        conversation.setFitToWidth(true);
        conversation.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        conversation.setStyle("-fx-background: white; -fx-background-color: white;");
        dialogContainer.heightProperty().addListener((observable, oldHeight, newHeight) ->
                conversation.setVvalue(1.0));
        return conversation;
    }

    /** Creates the command input field and send button.
     *
     * @return the configured input bar.
     */
    private HBox createInputBar() {
        userInput = new TextField();
        userInput.setPromptText("Enter a command, such as: todo read book");
        userInput.setPrefHeight(42.0);
        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setPrefHeight(42.0);
        sendButton.setPrefWidth(82.0);
        HBox inputBar = new HBox(10.0, userInput, sendButton);
        inputBar.setPadding(new Insets(12.0, 16.0, 16.0, 16.0));
        HBox.setHgrow(userInput, Priority.ALWAYS);
        return inputBar;
    }

    /** Connects both input controls to the command handler. */
    private void configureInputHandlers() {
        sendButton.setOnAction(event -> handleUserInput());
        userInput.setOnAction(event -> handleUserInput());
    }

    /** Creates the main window layout from its three sections.
     *
     * @param header the header displayed at the top.
     * @param inputBar the command input displayed at the bottom.
     * @return the configured main layout.
     */
    private BorderPane createMainLayout(VBox header, HBox inputBar) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(header);
        mainLayout.setCenter(scrollPane);
        mainLayout.setBottom(inputBar);
        mainLayout.setStyle("-fx-background-color: white;");
        return mainLayout;
    }

    /** Configures and displays the primary application window.
     *
     * @param stage the primary application window.
     * @param mainLayout the layout displayed in the window.
     */
    private void configureStage(Stage stage, BorderPane mainLayout) {
        Scene scene = new Scene(mainLayout, 640.0, 640.0);
        stage.setTitle("SevenSix");
        stage.setMinWidth(480.0);
        stage.setMinHeight(480.0);
        stage.setScene(scene);
        stage.show();
    }

    /** Displays the initial greeting and focuses the command field. */
    private void showGreeting() {
        addMessage("Hello! I'm SevenSix.\nWhat can I do for you?", false);
        userInput.requestFocus();
    }

    /**
     * Adds the entered command and the chatbot response to the conversation.
     */
    private void handleUserInput() {
        String command = userInput.getText().trim();
        if (command.isBlank()) {
            return;
        }

        displayCommandResponse(command);
        userInput.clear();

        if (command.equals("bye")) {
            Platform.exit();
            return;
        }
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    /** Displays the entered command and the chatbot response.
     *
     * @param command the command entered by the user.
     */
    private void displayCommandResponse(String command) {
        addMessage(command, true);
        addMessage(chatbot.getResponse(command), false);
    }

    /**
     * Adds one sender-styled message bubble to the conversation.
     *
     * @param message the message to display.
     * @param isUserMessage whether the message came from the user.
     */
    private void addMessage(String message, boolean isUserMessage) {
        dialogContainer.getChildren().add(new DialogBox(message, isUserMessage));
    }
}
