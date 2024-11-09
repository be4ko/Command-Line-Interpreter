package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    private Path currentDirectory;

    public Main() {
        this.currentDirectory = Paths.get(System.getProperty("user.dir"));
    }

    public void cd(String path) {
        Path newPath = currentDirectory.resolve(path).normalize();
        if ("..".equals(path)) {
            currentDirectory = currentDirectory.getParent() != null ? currentDirectory.getParent() : currentDirectory;
        } else if (Files.isDirectory(newPath)) {
            currentDirectory = newPath;
        } else {
            System.out.println("Directory not found: " + path);
        }
    }

    public String pwd() {
        return currentDirectory.toString();
    }

    public boolean mkdir(String... dirPaths) {
        boolean allCreated = true;

        for (String path : dirPaths) {
            File newDir = new File(currentDirectory.toFile(), path);

            // Use mkdirs() to create any necessary parent directories
            if (newDir.exists()) {
                System.out.println("Directory already exists: " + path);
                allCreated = false; // At least one directory already existed
            } else if (newDir.mkdirs()) {
                System.out.println("Directory created: " + path);
            } else {
                System.out.println("Failed to create directory: " + path);
                allCreated = false; // At least one directory could not be created
            }
        }

        return allCreated;
    }

    public boolean rmdir(String dirName) {
        File dirToRemove = new File(currentDirectory.toFile(), dirName);

        if (!dirToRemove.exists() || !dirToRemove.isDirectory()) {
            System.out.println("Directory does not exist or is not a directory: " + dirName);
            return false;
        }

        if (dirToRemove.list().length > 0) {
            System.out.println("Directory is not empty: " + dirName);
            return false;
        }

        if (!dirToRemove.delete()) {
            System.out.println("Failed to delete directory: " + dirName);
            return false;
        }
        return true;
    }

    public String[] ls() {
        File dir = currentDirectory.toFile();
        File[] filesArray;
        filesArray = dir.listFiles((file) -> !file.getName().startsWith("."));

        assert filesArray != null;
        String[] fileNames = new String[filesArray.length];
        for (int i = 0; i < filesArray.length; i++) {
            fileNames[i] = filesArray[i].getName();
        }
        return fileNames;
    }

    public String[] ls(String option) {
        File dir = currentDirectory.toFile();
        File[] filesArray;
        switch (option) {
            case "-a":
                filesArray = dir.listFiles();
                break;

            case "-r":
                filesArray = dir.listFiles();
                if (filesArray != null) {
                    Arrays.sort(filesArray, Collections.reverseOrder());
                }
                break;
            default:
                filesArray = dir.listFiles((file) -> !file.getName().startsWith("."));
                if (filesArray != null) {
                    Arrays.sort(filesArray);
                }
                break;
        }

        assert filesArray != null;

        // loop converts Array of files to array of String
        String[] fileNames = new String[filesArray.length];
        for (int i = 0; i < filesArray.length; i++) {
            fileNames[i] = filesArray[i].getName();
        }
        return fileNames;
    }

    public boolean mv(String[] command) {
        if (command.length < 3) {
            System.out.println("Invalid command. Usage: mv <source> <target>");
            return false;
        }

        File targetFile = new File(currentDirectory.toFile(), command[command.length - 1]);
        for (int i = 1; i < command.length - 1; i++) {
            File sourceFile = new File(currentDirectory.toFile(), command[i]);
            if (!sourceFile.exists()) {
                System.out.println("mv: cannot stat '" + sourceFile.getPath() + "': No such file or directory");
                return false;
            }
            try {
                Path destination = targetFile.isDirectory() ? targetFile.toPath().resolve(sourceFile.getName())
                        : targetFile.toPath();
                Files.move(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("An error occurred while moving the file: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public boolean rm(String[] command) {
        if (command.length < 2) {
            System.out.println("Missing argument for rm.");
            return false;
        }
        boolean allDeleted = true;
        for (int i = 1; i < command.length; i++) {
            File file = new File(currentDirectory.toFile(), command[i]);
            if (!file.exists() || !file.delete()) {
                System.out.println("Failed to delete file: " + command[i]);
                allDeleted = false;
            }
        }
        return allDeleted;
    }

    public void catOrg(String[] command) {
        var cli = new Main();
        if (command.length > 2 && (command[1].equals(">") || command[1].equals(">>"))) {
            String operator = command[1];
            String outputFileName = command[2];
            boolean append = operator.equals(">>");

            System.out.println("Enter text (press exit to finish):");
            cli.catFromInput(outputFileName, append);

        } else if (command.length > 1) {
            String lastCommand = command[command.length - 2];
            boolean isRedirect = lastCommand.equals(">") || lastCommand.equals(">>");
            String[] fileNames = isRedirect ? Arrays.copyOfRange(command, 1, command.length - 2)
                    : Arrays.copyOfRange(command, 1, command.length);

            if (isRedirect) {
                String operator = lastCommand;
                String outputFileName = command[command.length - 1];
                boolean append = operator.equals(">>");

                try (BufferedWriter writer = new BufferedWriter(
                        new FileWriter(new File(cli.currentDirectory.toFile(), outputFileName), append))) {
                    for (String fileName : fileNames) {
                        File file = new File(cli.currentDirectory.toFile(), fileName);

                        if (!file.exists()) {
                            System.out.println("cat: " + fileName + ": No such file");
                            continue;
                        }

                        if (file.isDirectory()) {
                            System.out.println("cat: " + fileName + ": Is a directory");
                            continue;
                        }

                        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                writer.write(line);
                                writer.newLine();
                            }
                        } catch (IOException e) {
                            System.out.println("cat: An error occurred while reading the file: " + e.getMessage());
                        }
                    }
                    System.out.println("Output written to " + outputFileName);
                } catch (IOException e) {
                    System.out.println("cat: An error occurred while writing to file: " + e.getMessage());
                }
            } else {
                // No redirection; print to console
                cli.cat(fileNames);
            }
        } else {
            // Read from standard input if no files are specified
            System.out.println("Enter text (press exit to finish):");
            cli.catFromInput(null, false);
        }
    }

    public void catFromInput(String outputFileName, boolean append) {
        // Declare writer outside of try-with-resources to manage its lifecycle
        BufferedWriter writer = null;

        try {
            // Initialize the BufferedWriter only if outputFileName is provided
            if (outputFileName != null) {
                writer = new BufferedWriter(
                        new FileWriter(new File(currentDirectory.toFile(), outputFileName), append));
            }

            // Initialize the scanner
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter text (type 'exit' to finish):");

            while (true) {
                if (scanner.hasNextLine()) { // Check if there's a line to read
                    String line = scanner.nextLine();
                    if ("exit".equals(line)) { // Exit if user types "exit"
                        break;
                    }
                    if (writer != null) { // Write to the specified file if writer is not null
                        writer.write(line);
                        writer.newLine();
                    } else { // Otherwise, echo input to the console
                        System.out.println(line);
                    }
                } else {
                    // No more lines to read, can break the loop
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred while writing input: " + e.getMessage());
        } finally {
            // Close writer if it's not null
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Error closing writer: " + e.getMessage());
                }
            }
        }
    }

    public void cat(String... fileNames) {
        for (String fileName : fileNames) {
            File file = new File(currentDirectory.toFile(), fileName);

            if (!file.exists()) {
                System.out.println("cat: " + fileName + ": No such file");
                continue;
            }

            if (file.isDirectory()) {
                System.out.println("cat: " + fileName + ": Is a directory");
                continue;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.lines().forEach(System.out::println);
            } catch (IOException e) {
                System.out.println("cat: An error occurred while reading the file: " + e.getMessage());
            }
        }
    }

    public boolean touch(String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.out.println("Failed to create directory: " + parentDir.getPath());
                return false;
            }
        }
        try {
            if (file.exists()) {
                return true;
            } else if (!file.createNewFile()) {
                System.out.println("File could not be created: " + file.getPath());
                return false;
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void help() {
        System.out.println(helpText());
    }

    private String helpText() {
        return """
                Commands:
                  help               Show this help message
                  ls                 List directory contents
                  mv <source> <dest> Move or rename a file or directory
                  rm <file>          Remove a file
                  mkdir <dir>        Create a new directory
                  rmdir <dir>        Remove an empty directory
                  touch <file>       Create a new empty file or update a file's timestamp
                  exit               Exit the application
                """;
    }

    public void writeToFile(String fileName, boolean append) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, append))) {
            writer.write(helpText());
        }
    }

    public static void main(String[] args) {
        var cli = new Main();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(cli.pwd() + "> ");
            String input = scanner.nextLine();
            String[] command = input.trim().split(" ");

            try {
                switch (command[0]) {
                    case "pwd":
                        System.out.println(cli.pwd());
                        break;
                    case "cd":
                        cli.cd(command.length > 1 ? command[1] : "");
                        break;
                    case "ls":
                        String[] files;
                        if (command.length == 2) {
                            files = cli.ls(command[1]);
                        } else {
                            files = cli.ls();
                        }
                        System.out.println(String.join("\n", files));
                        break;

                    case "mkdir":
                        cli.mkdir(Arrays.copyOfRange(command, 1, command.length));
                        break;

                    case "rmdir":
                        System.out.println(cli.rmdir(command[1]) ? "Directory removed." : "");
                        break;
                    case "touch":
                        cli.touch(command[1]);
                        break;
                    case "mv":
                        cli.mv(command);
                        break;
                    case "rm":
                        cli.rm(command);
                        break;
                    case "cat":
                        cli.catOrg(command);
                        break;
                    case "help": {
                        if (command.length == 1)
                            cli.help();
                        else if (command.length == 3)
                            cli.writeToFile(command[2], ">>".equals(command[1]));
                    }
                        break;
                    case "exit": {
                        scanner.close();
                        return;
                    }
                    default:
                        System.out.println("Unknown command: " + command[0]);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Missing argument for command: " + command[0]);
            } catch (IOException e) {
                System.out.println("File operation error: " + e.getMessage());
            }
        }
    }
}
