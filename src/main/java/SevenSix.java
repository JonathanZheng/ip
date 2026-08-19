import java.util.Scanner;

/**
 * Starts the SevenSix application and responds to commands entered by the user.
 */
public class SevenSix {
    /**
     * Welcomes the user, echoes each command, and ends the program when the user enters {@code bye}.
     *
     * @param args command-line arguments, which are not used by this application
     */
    public static void main(String[] args) {
        String separator = "____________________________________________________________";

        System.out.println(separator);
        System.out.println("Hello! I'm SevenSix.");
        System.out.println("What can I do for you?");
        System.out.println(separator);

        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();

        while (!command.equals("bye")) {
            System.out.println(" " + command);
            System.out.println(separator);
            command = scanner.nextLine();
        }

        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(separator);
    }
}
