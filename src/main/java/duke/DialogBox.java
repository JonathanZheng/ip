package duke;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Displays one message in the SevenSix conversation.
 *
 * <p>The conversation is between a person and an app, so the two sides look different: the
 * user's short commands appear as compact colored bubbles on the right, while the chatbot's
 * longer replies appear as plain cards on the left that are easy to read.
 */
public class DialogBox extends HBox {
    /** Widest a message may grow, so that long lines stay readable. */
    private static final double MAX_MESSAGE_WIDTH = 480.0;

    /** The label containing the message text. */
    private final Label messageLabel;

    /**
     * Creates a message row whose look is defined by a stylesheet class.
     *
     * @param message the message text to display.
     * @param styleClass the stylesheet class that gives the message its look.
     * @param alignment the side of the conversation the message sits on.
     */
    private DialogBox(String message, String styleClass, Pos alignment) {
        messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(MAX_MESSAGE_WIDTH);
        messageLabel.getStyleClass().addAll("message", styleClass);

        setMaxWidth(Double.MAX_VALUE);
        setAlignment(alignment);
        getChildren().add(messageLabel);
    }

    /**
     * Creates a right-aligned bubble for a command the user entered.
     *
     * @param message the command text.
     * @return the user's message row.
     */
    public static DialogBox getUserDialog(String message) {
        return new DialogBox(message, "user-message", Pos.TOP_RIGHT);
    }

    /**
     * Creates a left-aligned card for a chatbot reply.
     *
     * @param message the reply text.
     * @return the chatbot's message row.
     */
    public static DialogBox getBotDialog(String message) {
        return new DialogBox(message, "bot-message", Pos.TOP_LEFT);
    }

    /**
     * Creates a left-aligned, highlighted card for a chatbot reply that reports an error.
     *
     * @param message the error reply text.
     * @return the chatbot's error message row.
     */
    public static DialogBox getErrorDialog(String message) {
        return new DialogBox(message, "error-message", Pos.TOP_LEFT);
    }
}
