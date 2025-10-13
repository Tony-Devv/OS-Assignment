import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;

class Parser {
    private String commandName;
    private String[] args;

    public void parse(String input) {
        String[] commandLine = input.trim().split("\\s+");
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
                    case "mkdir":
                        mkdir(arguments);
                        break;
                    case "rmdir":
                        rmdir(arguments);
                        break;
                    case "cp-r":
                        cp_r(arguments);
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

    public static void mkdir(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: mkdir needs at least one argument.");
            return;
        }
        for (String dirName : args) {
            File dir = new File(dirName);
            if (!dir.isAbsolute()) {
                dir = new File(currentPath, dirName);
            }
            if (dir.exists()) {
                System.out.println("Directory already exists: " + dir.getPath());
            } else {
                if (dir.mkdirs()) {
                    System.out.println("Directory created: " + dir.getPath());
                } else {
                    System.out.println("Error: Failed to create directory " + dir.getPath());
                }
            }
        }
    }

    public static void rmdir(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: rmdir takes exactly one argument.");
            return;
        }
        String arg = args[0];
        if (arg.equals("*")) {
            File currentDir = new File(currentPath);
            File[] subFiles = currentDir.listFiles();
            if (subFiles != null) {
                for (File f : subFiles) {
                    if (f.isDirectory() && f.list().length == 0) {
                        if (f.delete()) {
                            System.out.println("Deleted empty directory: " + f.getName());
                        }
                    }
                }
            }
        } else {
            File dir = new File(arg);
            if (!dir.isAbsolute()) {
                dir = new File(currentPath, arg);
            }
            if (!dir.exists()) {
                System.out.println("Error: Directory does not exist.");
                return;
            }
            if (!dir.isDirectory()) {
                System.out.println("Error: Not a directory.");
                return;
            }
            if (dir.list().length > 0) {
                System.out.println("Error: Directory is not empty.");
                return;
            }
            if (dir.delete()) {
                System.out.println("Directory deleted: " + dir.getPath());
            } else {
                System.out.println("Error: Could not delete directory.");
            }
        }
    }

    public static void cp_r(String[] args) {
        if (args.length != 2) {
            System.out.println("Error: cp -r takes exactly 2 arguments (sourceDir, destDir).");
            return;
        }
        File source = new File(args[0]);
        File dest = new File(args[1]);
        if (!source.isAbsolute()) source = new File(currentPath, args[0]);
        if (!dest.isAbsolute()) dest = new File(currentPath, args[1]);
        if (!source.exists() || !source.isDirectory()) {
            System.out.println("Error: Source directory does not exist or is not a directory.");
            return;
        }
        File newDest = new File(dest, source.getName());
        if (!newDest.exists()) newDest.mkdirs();
        try {
            copyDirectory(source, newDest);
            System.out.println("Directory copied successfully.");
        } catch (IOException e) {
            System.out.println("Error copying directory: " + e.getMessage());
        }
    }

    private static void copyDirectory(File source, File dest) throws IOException {
        if (source.isDirectory()) {
            if (!dest.exists()) dest.mkdirs();
            String[] children = source.list();
            if (children != null) {
                for (String child : children) {
                    copyDirectory(new File(source, child), new File(dest, child));
                }
            }
        } else {
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(dest)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }
}
