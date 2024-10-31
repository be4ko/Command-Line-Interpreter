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

    public boolean mkdir(String dirname) {
        File newDir = new File(currentDirectory.toFile(), dirname);
        if (newDir.exists()) {
            System.out.println("Directory already exists: " + dirname);
            return false;
        }
        if (!newDir.mkdir()) {
            System.out.println("Failed to create directory: " + dirname);
            return false;
        }
        return true;
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
        return listDirectory(false, false);
    }

    public String[] ls(String option) {
        boolean showHidden = "-a".equals(option);
        boolean reverseOrder = "-r".equals(option);
        return listDirectory(showHidden, reverseOrder);
    }

    private String[] listDirectory(boolean showHidden, boolean reverseOrder) {
        File[] filesArray = currentDirectory.toFile().listFiles(file -> showHidden || !file.getName().startsWith("."));
        if (filesArray == null)
            return new String[0];

        if (reverseOrder)
            Arrays.sort(filesArray, Collections.reverseOrder());
        else
            Arrays.sort(filesArray);

        return Arrays.stream(filesArray).map(File::getName).toArray(String[]::new);
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
                    case "cd":
                        cli.cd(command.length > 1 ? command[1] : "");
                        break;
                    case "ls":
                        System.out.println(String.join("\n", cli.ls(command.length > 1 ? command[1] : "")));
                        break;

                    case "mkdir":
                        System.out.println(cli.mkdir(command[1]) ? "Directory created." : "");
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
                        if (command.length > 1)
                            cli.cat(Arrays.copyOfRange(command, 1, command.length));
                        else
                            scanner.hasNextLine();
                        System.out.println(scanner.nextLine());
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
