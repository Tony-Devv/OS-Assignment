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
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        String[] parts = input.trim().split("\\s+");
        if (parts.length == 0) {
            return false;
        }

        int redirectIndex = -1;
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].equals(">") || parts[i].equals(">>")) {
                if (redirectIndex != -1) {
                    return false;
                }
                redirectIndex = i;
                append = parts[i].equals(">>");
            }
        }

        if (redirectIndex != -1) {
            if (redirectIndex != parts.length - 2) {
                return false;
            }
            commandName = parts[0];
            args = new String[redirectIndex - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
            redirectFile = parts[redirectIndex + 1];
        } else {
            commandName = parts[0];
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
            redirectFile = null;
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
    private static String currentPath = System.getProperty("user.dir");
    private static Parser parser = new Parser();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Terminal Test 1 2 3 ");

        while (true) {
            System.out.print(currentPath + " > ");
            String input = sc.nextLine();

            if (input.equals("exit")) break;

            if (!parser.parse(input)) {
                System.out.println("Invalid command syntax!");
                continue;
            }
            String command = parser.getCommandName();
            String[] arguments = parser.getArgs();
            String redirectFileName = parser.getRedirectFile();
            boolean append = parser.isAppend();

            PrintStream originalOut = System.out;
            PrintStream fileOut = null;

            if (redirectFileName != null) {
                if(append){
                    System.out.println("Output appended to " + redirectFileName);
                }
                else{
                    System.out.println("Output redirecting to " + redirectFileName);
                }
                File redirectFile = new File(redirectFileName);
                if (!redirectFile.isAbsolute()) {
                    redirectFile = new File(currentPath, redirectFileName);
                }
                try {
                    fileOut = new PrintStream(new FileOutputStream(redirectFile, append));
                    System.setOut(fileOut);
                } catch (IOException e) {
                    System.out.println("Error opening file for redirection: " + e.getMessage());
                    continue;
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
                    case "cp-r":
                        cp_r(arguments);
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
                if (redirectFileName != null) {
                    if (fileOut != null) {
                        fileOut.close();
                    }
                    System.setOut(originalOut);
                }
            }
        }

        sc.close();
    }

    public static void pwd() {
        System.out.println(currentPath);
    }

    public static void cd(String[] args) {
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

    public static void ls() {
        File f = new File(System.getProperty("user.dir"));
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
    public static void cp(String[] args) {
        if (args.length != 2) {
            System.out.println("Error: cp takes exactly 2 arguments (sourceFile, destFile).");
            return;
        }
        File source = new File(args[0]);
        File dest = new File(args[1]);
        if (!source.isAbsolute()) {
            source = new File(currentPath, args[0]);
        }
        if (!dest.isAbsolute()){
            dest = new File(currentPath, args[1]);
        }
        if (!source.exists()) {
            System.out.println("Error: Source file does not exist.");
            return;
        }
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
            System.out.println("File copied successfully.");
        } catch (IOException e) {
            System.out.println("Error copying file: " + e.getMessage());
        }
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
    //----------------- rm  -----------------------------
    public static void rm(String [] args){
        if(args.length != 1)
        {
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
    //----------------- zip  ----------------------------
    public static void zip(String [] args)
    {
        if(args.length < 2)
        {
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
        if (!zipFile.isAbsolute())
        {
            zipFile = new File(currentPath, zipFileName);
        }
        try(FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos)){
            for (int i = startIndex + 1; i < args.length; i++) {
                File file = new File(args[i]);
                if (!file.isAbsolute()) file = new File(currentPath, args[i]);
                if (!file.exists()) {
                    System.out.println("Warning: Skipping missing file " + file.getName());
                    continue;
                }
                addToZip(file, file.getName(), zos, recursive);
            }
        System.out.println("Zip File extracted successfully to: " + zipFile.getAbsolutePath());
        }catch (IOException e)
        {
            System.out.println("Error while creating the Zip: " + e.getMessage());
        }

    }

    private static void addToZip(File file, String zipEntryName, ZipOutputStream zos, boolean recursive) throws IOException {
        if(file.isDirectory())
        {
            if(recursive)
            {
                File [] childFiles = file.listFiles();
                if(childFiles != null)
                {
                    for(File child : childFiles)
                    {
                        addToZip(child, (zipEntryName + "/" + child.getName()), zos, true);
                    }
                }
            }
        }
        else{
            try(FileInputStream fis = new FileInputStream(file)){
                ZipEntry entry = new ZipEntry(zipEntryName);
                zos.putNextEntry(entry);
                fis.transferTo(zos);
                zos.closeEntry();
            }
        }
    }

    //----------------- unzip  ----------------------------
    public static void unzip(String [] args)
    {
        if(args.length < 1)
        {
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
        if(args.length >= 3 && args[1].equals("-d"))
        {
            destDir = new File(args[2]);
            if(!destDir.isAbsolute())
            {
                destDir = new File(currentPath, args[2]);
            }
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
        }
        try(FileInputStream fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(fis)){
            ZipEntry entry;
            while( (entry = zis.getNextEntry()) != null){
                File newFile = new File(destDir, entry.getName());
                if(entry.isDirectory())
                {
                    newFile.mkdirs();
                }
                else {
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
        };
    }
    public static void wc(String[] args){
        if(args.length != 1){
            System.out.println("Error: You must specify exactly one file.");
            return;
        }
        File file = new File(args[0]);
        if(!file.isAbsolute()){
            file = new File(currentPath, args[0]);
        }
        if(!file.exists() || !file.isFile()){
            System.out.println("Error: File not found or is a directory - \"" + file.getPath() + "\"");
            return;
        }
        int no_lines = 0;
        int no_words = 0;
        int no_characters = 0;
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            while((line = reader.readLine()) != null){
                no_lines++;
                //  Count words if the line is not empty or whitespace
                if(!line.trim().isEmpty()){
                    no_words += line.split("\\s+").length;
                }
                // Count characters including whitespace and newlines
                no_characters += line.length() + 1;
            }
            System.out.println(no_lines + " " + no_words + " " + no_characters + " " + file.getName());
        }catch (IOException e){
            System.out.println("Error: Unable to read file - \"" + file.getPath() + "\"");
        }
    }
    public static void echo(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(" ");
        }
        System.out.println(sb.toString().trim());
    }

}

