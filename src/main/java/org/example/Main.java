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
        Path newPath = currentDirectory.resolve(path); // Takes the path provided, and appends it to the current
                                                       // directory path
        if (Files.isDirectory(newPath)) {
            currentDirectory = newPath;
        } else {
            System.out.println("Directory not found: " + path);
        }
    }

    public String pwd() {
        return currentDirectory.toString();
    }

    public String[] ls() {
        File dir = currentDirectory.toFile();
        return dir.list();
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

            // fix the default case to match that it doesn't show files that starts with '.'
            default:
                filesArray = dir.listFiles();
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

    public void mv(List<File> sourceAndTarget) {
        File targetFile = sourceAndTarget.getLast();

        for (int i = 0; i < sourceAndTarget.size() - 1; i++) {
            File sourceFile = sourceAndTarget.get(i);
            if (!sourceFile.exists()) {
                System.out.println("mv: cannot stat '" + sourceFile.getPath() + "': No such file or directory");
                continue;
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
            }
        }
    }

    public void rm(List<File> sourceAndTarget) {
        for (int i = 0; i < sourceAndTarget.size(); i++) {
            File file = sourceAndTarget.get(i);
            if (file.exists()) {
                file.delete();
            } else {
                System.out.println("rm: failed to delete file: " + file.getName());
            }
        }
    }

    public void touch(String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.out.println("Failed to create directory: " + parentDir.getPath());
                return;
            }
        }
        try {
            if (!file.createNewFile()) {
                System.out.println("File already exists: " + file.getPath());
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file: " + e.getMessage());
        }
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

                case "rmdir":

                case "touch":
                    if (command.length > 1) {
                        cli.touch(command[1]);
                    } else {
                        System.out.println("Missing argument for touch.");
                    }
                    break;
                case "mv":
                    if (command.length < 3) {
                        System.out.println("Invalid command. Usage: mv <source> <target>");
                        break;
                    }
                    List<File> sourceAndTarget = new ArrayList<>();
                    for (int i = 1; i < command.length; i++) {
                        File sourceFile = new File(command[i]);
                        sourceAndTarget.add(sourceFile);
                    }
                    cli.mv(sourceAndTarget);
                    break;
                case "rm":
                    if (command.length > 1) {
                        sourceAndTarget = new ArrayList<>();
                        for (int i = 1; i < command.length; i++) {
                            File sourceFile = new File(command[i]);
                            sourceAndTarget.add(sourceFile);
                        }
                        cli.rm(sourceAndTarget);
                    } else {
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