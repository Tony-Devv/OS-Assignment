import java.util.Scanner;

class Parser {
    private String commandName;
    private String[] args;

    public void parse(String input) {
        String[] commandLine = input.trim().split("\\s+"); // regex to split on spaces
        commandName = commandLine[0];
        args = new String[commandLine.length - 1];

        for (int i = 1; i < commandLine.length; i++) {
            args[i - 1] = commandLine[i];
        }
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

public class Terminal {
    private static String currentPath = System.getProperty("user.dir");
    private static Parser parser = new Parser();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Terminal Test 1 2 3 ");

        while (true) {
            System.out.print(currentPath + " > ");
            String input = sc.nextLine();

            if (input.equals("exit")) break;

            parser.parse(input);
            String command = parser.getCommandName();
            String[] arguments = parser.getArgs();

            try {
                switch (command) {
                    case "pwd":
                        pwd();
                        break;

                    case "cd":
                        cd(arguments);
                        break;

                    default:
                        System.out.println("Invalid command!");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error executing command: " + e.getMessage());
            }
        }

        sc.close();
    }

    public static void pwd() {
        System.out.println(currentPath);
    }

    public static void cd(String[] args) {

    }
}
