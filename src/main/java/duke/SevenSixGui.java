package duke;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
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
    /** Classpath location of the stylesheet that defines the window's look. */
    private static final String STYLESHEET = "/css/main.css";

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
        HBox header = createHeader();
        scrollPane = createConversation();
        HBox inputBar = createInputBar();
        configureInputHandlers();
        BorderPane mainLayout = createMainLayout(header, inputBar);
        configureStage(stage, mainLayout);
        showGreeting();
    }

    /** Creates the title and subtitle displayed above the conversation.
     *
     * <p>Both labels share one row so that the header uses little vertical space.
     *
     * @return the configured conversation header.
     */
    private HBox createHeader() {
        Label title = new Label("SevenSix");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Your friendly task assistant");
        subtitle.getStyleClass().add("subtitle");
        HBox header = new HBox(title, subtitle);
        header.setAlignment(Pos.BASELINE_LEFT);
        header.getStyleClass().add("header");
        return header;
    }

    /** Creates the scrollable conversation area.
     *
     * @return the configured conversation scroll pane.
     */
    private ScrollPane createConversation() {
        dialogContainer = new VBox();
        dialogContainer.getStyleClass().add("dialog-container");
        ScrollPane conversation = new ScrollPane(dialogContainer);
        conversation.setFitToWidth(true);
        conversation.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        conversation.getStyleClass().add("conversation");
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
        userInput.getStyleClass().add("command-field");
        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.getStyleClass().add("send-button");
        HBox inputBar = new HBox(userInput, sendButton);
        inputBar.getStyleClass().add("input-bar");
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
    private BorderPane createMainLayout(HBox header, HBox inputBar) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(header);
        mainLayout.setCenter(scrollPane);
        mainLayout.setBottom(inputBar);
        return mainLayout;
    }

    /** Configures and displays the primary application window.
     *
     * @param stage the primary application window.
     * @param mainLayout the layout displayed in the window.
     */
    private void configureStage(Stage stage, BorderPane mainLayout) {
        Scene scene = new Scene(mainLayout, 640.0, 640.0);
        scene.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());
        stage.setTitle("SevenSix");
        stage.setMinWidth(480.0);
        stage.setMinHeight(480.0);
        stage.setScene(scene);
        stage.show();
    }

    /** Displays the initial greeting and focuses the command field. */
    private void showGreeting() {
        addMessage(DialogBox.getBotDialog("Hello! I'm SevenSix.\nWhat can I do for you?"));
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
        addMessage(DialogBox.getUserDialog(command));
        addMessage(createReplyDialog(chatbot.getResponse(command)));
    }

    /**
     * Creates the message row for a chatbot reply, highlighting replies that report errors.
     *
     * @param response the chatbot reply.
     * @return an error row for error replies, or a normal reply row otherwise.
     */
    private DialogBox createReplyDialog(String response) {
        return Ui.isErrorResponse(response)
                ? DialogBox.getErrorDialog(response)
                : DialogBox.getBotDialog(response);
    }

    /**
     * Adds one message row to the end of the conversation.
     *
     * @param dialog the message row to display.
     */
    private void addMessage(DialogBox dialog) {
        dialogContainer.getChildren().add(dialog);
    }
}
