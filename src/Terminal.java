import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

class Parser {
    private String commandName;
    private String[] args;
    private String redirectFile;
    private boolean append;

    public boolean parse(String input) {
        // Check if input is null or empty
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        // Split input into parts by whitespace
        String[] parts = input.trim().split("\\s+");
        // Command name is the first part
        commandName = parts[0];
        // Initialize defaults
        redirectFile = null;
        append = false;
        // Look for redirection symbol (">" or ">>")
        int redirectIndex = -1;
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].equals(">") || parts[i].equals(">>")) {
                // Prevent multiple redirections (invalid)
                if (redirectIndex != -1) {
                    return false;
                }
                redirectIndex = i;
                append = parts[i].equals(">>");
            }
        }
        // If a redirect symbol is found
        if (redirectIndex != -1) {
            // It cannot be the last element (must have a file name after it)
            if (redirectIndex == parts.length - 1) {
                return false;
            }
            // Copy the args before the redirect symbol
            args = Arrays.copyOfRange(parts, 1, redirectIndex);
            // Set the file name after the redirect symbol
            redirectFile = parts[redirectIndex + 1];
        } else {
            // No redirection: copy all parts after the command as args
            args = Arrays.copyOfRange(parts, 1, parts.length);
        }
        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }

    public String getRedirectFile() {
        return redirectFile;
    }

    public boolean isAppend() {
        return append;
    }
}

public class Terminal {
    private String currentPath = System.getProperty("user.dir");
    private Parser parser = new Parser();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Terminal terminal = new Terminal();
        System.out.println("Welcome to the Terminal Simulator!");

        while (true) {
            System.out.print(terminal.currentPath + " > ");
            String input = sc.nextLine();

            if (input.equals("exit"))
                break;

            if (!terminal.parser.parse(input)) {
                System.out.println("Invalid command syntax!");
                continue;
            }

            // Moved Command Choosing functionality from Main to it's correct Function
            terminal.chooseCommandAction(
                    terminal.parser.getCommandName(),
                    terminal.parser.getArgs(),
                    terminal.parser.getRedirectFile(),
                    terminal.parser.isAppend());
        }

        sc.close();
    }

    public void chooseCommandAction(String command, String[] arguments, String redirectFileName, boolean append) {
        PrintStream originalOut = System.out;
        PrintStream fileOut = null;

        if (redirectFileName != null) {
            try {
                // Print message before redirecting output (so it appears on console, not in file)
                if (append) {
                    System.out.println("Output will be appended to " + redirectFileName);
                } else {
                    System.out.println("Output will be redirected to " + redirectFileName);
                }
                // Prepare the target file for output
                File redirectFile = new File(redirectFileName);
                // If path is relative, make it relative to currentPath
                if (!redirectFile.isAbsolute()) {
                    redirectFile = new File(currentPath, redirectFileName);
                }
                // Create PrintStream for redirection (append or overwrite mode)
                fileOut = new PrintStream(new FileOutputStream(redirectFile, append));
                // Redirect System.out to the chosen file
                System.setOut(fileOut);
            }
            catch (IOException e) {
                // If file cannot be created or opened
                System.out.println("Error: Cannot open file for redirection → " + e.getMessage());
                return; // Skip executing command
            }
        }

        try {
            switch (command) {
                case "pwd":
                    pwd();
                    break;
                case "cd":
                    cd(arguments);
                    break;
                case "ls":
                    if (arguments.length > 0) {
                        System.out.println("Error: 'ls' takes no arguments.");
                    } else {
                        ls();
                    }
                    break;
                case "cp":
                    cp(arguments);
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
                case "rm":
                    rm(arguments);
                    break;
                case "zip":
                    zip(arguments);
                    break;
                case "unzip":
                    unzip(arguments);
                    break;
                case "wc":
                    wc(arguments);
                    break;
                case "echo":
                    echo(arguments);
                    break;
                default:
                    System.out.println("Invalid command!");
                    break;
            }
        } catch (Exception e) {
            System.out.println("Error executing command: " + e.getMessage());
        } finally {
            // Restore original System.out if redirection was used
            if (redirectFileName != null) {
                if (fileOut != null) {
                    // Close the file output stream
                    fileOut.close();
                }
                // Restore original System.out
                System.setOut(originalOut);
            }
        }
    }

    public void pwd() {
        System.out.println(currentPath);
    }

    public void cd(String[] args) {
        if (args.length == 0) {
            currentPath = System.getProperty("user.home");
            return;
        }
        if (args.length > 1) {
            System.out.println("Error: cd takes no argument or 1 argument only.");
            return;
        }
        if (args[0].equals("..")) {
            File parent = new File(currentPath).getParentFile();
            if (parent != null) {
                currentPath = parent.getAbsolutePath();
            } else {
                System.out.println("Error: Already at root directory.");
            }
            return;
        }
        File newDir = new File(args[0]);
        if (!newDir.isAbsolute()) {
            newDir = new File(currentPath, args[0]);
        }
        if (!newDir.exists() || !newDir.isDirectory()) {
            System.out.println("Error: Directory does not exist or is not a directory.");
            return;
        }
        currentPath = newDir.getAbsolutePath();
    }

    public void ls() {
        File f = new File(currentPath);
        if (!f.exists()) {
            System.out.println("This directory does not exist.");
            return;
        }
        File[] files = f.listFiles();
        if (files == null) {
            System.out.println("Unable to list files in this directory.");
            return;
        }
        Arrays.sort(files);
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("Directory: " + file.getName());
            } else {
                System.out.println("File: " + file.getName());
            }
        }
    }

    public void touch(String[] args) {
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

    public void cat(String[] args) {

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
                continue;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

            }

            catch (IOException e) {
                System.out.println("Error: Cannot read file - " + fileName);
            }
        }
    }

    public void mkdir(String[] args) {
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

    public void rmdir(String[] args) {
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

    public void cp(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: cp takes at least 2 arguments ([-r] source dest).");
            return;
        }

        boolean recursive = false;
        int index = 0;

        if (args[0].equals("-r")) {
            recursive = true;
            index = 1;
            if (args.length != 3) {
                System.out.println("Error: cp -r takes exactly 2 arguments (sourceDir destDir).");
                return;
            }
        } else if (args.length != 2) {
            System.out.println("Error: cp takes exactly 2 arguments (sourceFile destFile).");
            return;
        }

        File source = new File(args[index]);
        File dest = new File(args[index + 1]);

        if (!source.isAbsolute())
            source = new File(currentPath, args[index]);
        if (!dest.isAbsolute())
            dest = new File(currentPath, args[index + 1]);

        if (!source.exists()) {
            System.out.println("Error: Source does not exist.");
            return;
        }

        try {
            if (recursive) {
                if (!source.isDirectory()) {
                    System.out.println("Error: cp -r source must be a directory.");
                    return;
                }
                File newDest = new File(dest, source.getName());
                if (!newDest.exists())
                    newDest.mkdirs();
                copyDirectory(source, newDest);
                System.out.println("Directory copied successfully.");
            } else {
                if (source.isDirectory()) {
                    System.out.println("Error: Source is a directory. Use 'cp -r' for directories.");
                    return;
                }
                try (InputStream in = new FileInputStream(source);
                        OutputStream out = new FileOutputStream(dest)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
                System.out.println("File copied successfully.");
            }
        } catch (IOException e) {
            System.out.println("Error copying: " + e.getMessage());
        }
    }

    private void copyDirectory(File source, File dest) throws IOException {
        File[] files = source.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            File newFile = new File(dest, file.getName());
            if (file.isDirectory()) {
                newFile.mkdirs();
                copyDirectory(file, newFile);
            } else {
                try (InputStream in = new FileInputStream(file);
                        OutputStream out = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
            }
        }
    }

    public void rm(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: rm takes exactly one argument (file name).");
            return;
        }
        File file = new File(args[0]);
        if (!file.isAbsolute()) {
            file = new File(currentPath, args[0]);
        }
        if (!file.exists()) {
            System.out.println("Error: File not found - \"" + file.getPath() + "\"");
            return;
        }
        if (file.isDirectory()) {
            System.out.println("Error: \"" + file.getName() + "\" is a directory. Use rmdir or cp -r for directories.");
            return;
        }
        if (file.delete()) {
            System.out.println("File deleted successfully: " + file.getPath());
        } else {
            System.out.println("Error: Unable to delete file: " + file.getPath());
        }
    }

    public void zip(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: zip [-r] should take two or more arguments (zipName file1 ...).");
            return;
        }
        boolean recursive = false;
        int startIndex = 0;

        if (args[0].equals("-r")) {
            recursive = true;
            startIndex = 1;
        }
        if (args.length <= startIndex + 1) {
            System.out.println("Error: You must specify at least one file or directory to compress.");
            return;
        }

        String zipFileName = args[startIndex];
        File zipFile = new File(zipFileName);
        if (!zipFile.isAbsolute()) {
            zipFile = new File(currentPath, zipFileName);
        }
        try (FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (int i = startIndex + 1; i < args.length; i++) {
                File file = new File(args[i]);
                if (!file.isAbsolute())
                    file = new File(currentPath, args[i]);
                if (!file.exists()) {
                    System.out.println("Warning: Skipping missing file " + file.getName());
                    continue;
                }
                addToZip(file, file.getName(), zos, recursive);
            }

            System.out.println("Zip File created successfully at: " + zipFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error while creating the Zip: " + e.getMessage());
        }
    }

    private void addToZip(File file, String zipEntryName, ZipOutputStream zos, boolean recursive) throws IOException {
        if (file.isDirectory()) {
            if (recursive) {
                File[] childFiles = file.listFiles();
                if (childFiles != null) {
                    for (File child : childFiles) {
                        addToZip(child, (zipEntryName + "/" + child.getName()), zos, true);
                    }
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipEntry entry = new ZipEntry(zipEntryName);
                zos.putNextEntry(entry);
                fis.transferTo(zos);
                zos.closeEntry();
            }
        }
    }

    public void unzip(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: unzip archive_name.zip [-d destination_folder] .");
            return;
        }
        File zipFile = new File(args[0]);
        if (!zipFile.isAbsolute()) {
            zipFile = new File(currentPath, args[0]);
        }

        if (!zipFile.exists()) {
            System.out.println("Error: File not found - \"" + zipFile.getPath() + "\"");
            return;
        }
        File destDir = new File(currentPath);
        if (args.length >= 3 && args[1].equals("-d")) {
            destDir = new File(args[2]);
            if (!destDir.isAbsolute()) {
                destDir = new File(currentPath, args[2]);
            }
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
        }
        try (FileInputStream fis = new FileInputStream(zipFile);
                ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        zis.transferTo(fos);
                    }
                }
                zis.closeEntry();
            }
            System.out.println("Zip File extracted successfully to: " + destDir.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error while extracting the Zip: " + e.getMessage());
        }
    }

    // Word Count Function
    public  void wc(String[] args) {
        // Check if exactly one file is specified
        if (args.length != 1) {
            System.out.println("Error: You must specify exactly one file.");
            return;
        }
        File file = new File(args[0]);
        // If path is relative, make it relative to currentPath
        if (!file.isAbsolute()) {
            file = new File(currentPath, args[0]);
        }
        // Check if file exists and is a file
        if (!file.exists() || !file.isFile()) {
            System.out.println("Error: File not found or is a directory - \"" + file.getPath() + "\"");
            return;
        }
        // Count lines, words, and characters
        int no_lines = 0;
        int no_words = 0;
        int no_characters = 0;
        // Read file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            // Count lines, words, and characters
            while ((line = reader.readLine()) != null) {
                no_lines++;
                // Count words if line is not empty
                if (!line.trim().isEmpty()) {
                    no_words += line.split("\\s+").length;
                }
                // Count characters including newline
                no_characters += line.length() + 1;
            }
            // Print results
            System.out.println(no_lines + " " + no_words + " " + no_characters + " " + file.getName());
        } catch (IOException e) {
            // Print error message
            System.out.println("Error: Unable to read file - \"" + file.getPath() + "\"");
        }
    }

    // Echo Function prints the arguments to the console
    public  void echo(String[] args) {
        // If no arguments are provided, print a newline
        if (args.length == 0){
            System.out.println();
            return;
        }
        // Join all arguments with a space and print
        System.out.println(String.join(" ", args));
    }
}
