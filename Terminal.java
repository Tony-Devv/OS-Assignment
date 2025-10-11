import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

                    case "touch":
                        touch(arguments);
                        break;

                    case "cat":
                        cat(arguments);
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

    public static void touch(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: touch needs at least one argument.");
            return;
        } else if (args.length != 1) {
            System.out.println("Error: touch takes only one argument.");
            return;
        }

        File file = new File(args[0]);
        if (!file.isAbsolute()) {
            file = new File(currentPath, args[0]);
        }

        try {
            if (file.createNewFile()) {
                System.out.println("The File : \"" + file.getName() + "\" has been created successfully.");
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("Error: Unable to create file.");
        }

    }
        public static void cat(String[] args) {
            if (args.length == 0 || args.length > 2) {
                System.out.println("Error: cat takes 1 or 2 arguments only.");
                return;
            }

            for (String fileName : args) {
                File file = new File(fileName);
                if (!file.isAbsolute()) {
                    file = new File(currentPath, fileName);
                }

                if (!file.exists()) {
                    System.out.println("Error: File not found - \"" + fileName + "\"");
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.out.println("Error: Cannot read file - " + fileName);
                }
            }
        }



    }

