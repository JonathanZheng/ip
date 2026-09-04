package duke;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Displays one message in the SevenSix conversation.
 */
public class DialogBox extends HBox {
    /** The label containing the message text. */
    private final Label messageLabel;

    /**
     * Creates a message bubble for the specified sender.
     *
     * @param message the message text to display.
     * @param isUserMessage whether the message was entered by the user.
     */
    public DialogBox(String message, boolean isUserMessage) {
        messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(480.0);
        messageLabel.setPadding(new Insets(10.0, 14.0, 10.0, 14.0));
        messageLabel.setStyle(isUserMessage
                ? "-fx-background-color: #dbeafe; -fx-background-radius: 14;"
                : "-fx-background-color: #f1f5f9; -fx-background-radius: 14;");

        setMaxWidth(Double.MAX_VALUE);
        setAlignment(isUserMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        getChildren().add(messageLabel);
    }
}
