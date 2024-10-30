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
        Path newPath = currentDirectory.resolve(path);

        if (Files.isDirectory(newPath)) {
            currentDirectory = newPath;
        } else {
            System.out.println("Directory not found: " + path);
        }
    }

    public String pwd() {
        return currentDirectory.toString();
    }

    public boolean mkdir(String dirname) {
        File newdir = new File(currentDirectory.toFile(), dirname);
        if (newdir.exists()) {
            System.out.println("Directory already exists: " + dirname);
            return false;
        }
        boolean created = newdir.mkdir();
        if (!created) {
            System.out.println("Failed to create directory: " + dirname);
        }
        return created;
    }

    public boolean rmdir(String dirName) {
        File dirToRemove = new File(currentDirectory.toFile(), dirName);

        if (!dirToRemove.exists()) {
            System.out.println("Directory does not exist: " + dirName);
            return false;
        }

        if (!dirToRemove.isDirectory()) {
            System.out.println("Not a directory: " + dirName);
            return false;
        }

        if (dirToRemove.list().length > 0) {
            System.out.println("Directory is not empty: " + dirName);
            return false;
        }

        boolean deleted = dirToRemove.delete(); // .delete() deletes only if it's empty folder
        if (!deleted) {
            System.out.println("Failed to delete directory: " + dirName);
        }
        return deleted;
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
        List<File> sourceAndTarget = new ArrayList<>();
        for (int i = 1; i < command.length; i++) {
            File sourceFile = new File(command[i]);
            sourceAndTarget.add(sourceFile);
        }
        File targetFile = sourceAndTarget.getLast();

        for (int i = 0; i < sourceAndTarget.size() - 1; i++) {
            File sourceFile = sourceAndTarget.get(i);
            if (!sourceFile.exists()) {
                System.out.println("mv: cannot stat '" + sourceFile.getPath() + "': No such file or directory");
                return false;
            }
            try {
                if (targetFile.exists()) {
                    if (targetFile.isDirectory()) {
                        Path destination = Path.of(targetFile.getPath(), sourceFile.getName());
                        Files.move(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Files.move(sourceFile.toPath(), targetFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    Files.move(sourceFile.toPath(), targetFile.toPath());
                }
            } catch (Exception e) {
                System.out.println("An error occurred while moving the file: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public boolean rm(String[] command) {
        List<File> sourceAndTarget = new ArrayList<>();
        for (int i = 1; i < command.length; i++) {
            File sourceFile = new File(command[i]);
            sourceAndTarget.add(sourceFile);
        }
        for (File file : sourceAndTarget) {
            if (file.exists()) {
                file.delete();
            } else {
                System.out.println("rm: failed to delete file: " + file.getName());
                return false;
            }
        }
        return true;
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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        var cli = new Main();
        while (true) {
            System.out.print(cli.pwd() + "> ");
            String input = scanner.nextLine();
            String[] command = input.trim().split(" ");

            switch (command[0]) {
                case "cd":
                    if (command.length > 1) {
                        cli.cd(command[1]);
                    } else {
                        System.out.println("Missing argument for cd.");
                    }
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
                    if (command.length > 1) {
                        if (cli.mkdir(command[1])) {
                            System.out.println("Directory created successfully.");
                        } else {
                            System.out.println("Failed to create directory. It may already exist.");
                        }
                    } else {
                        System.out.println("Missing argument for mkdir.");
                    }
                    break;

                case "rmdir":
                    if (command.length > 1) {
                        if (cli.rmdir(command[1])) {
                            System.out.println("Directory removed successfully.");
                        } else {
                            System.out.println("Failed to remove directory. It may not be empty or does not exist.");
                        }
                    } else {
                        System.out.println("Missing argument for rmdir.");
                    }
                    break;

                case "touch":
                    if (!(command.length > 1 && cli.touch(command[1]))) {
                        System.out.println("Missing argument for touch.");
                    }
                    break;
                case "mv":
                    if (command.length < 3) {
                        System.out.println("Invalid command. Usage: mv <source> <target>");
                        break;
                    }
                    cli.mv(command);
                    break;
                case "rm":
                    if (!(command.length > 1 && cli.rm(command))) {
                        System.out.println("Missing argument for rm.");
                    }
                    break;
                case "cat":

                case "help":

                case "exit":
                    scanner.close();
                    return;
                default:
                    System.out.println("Error, False command");
                    break;
            }

        }
    }

}
