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
        Path newPath = currentDirectory.resolve(path); //Takes the path provided, and appends it to the current directory path
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
        switch (option)
        {
            case "-a":
                filesArray = dir.listFiles();
                break;

            case "-r":
                filesArray = dir.listFiles();
                if (filesArray != null) {
                    Arrays.sort(filesArray, Collections.reverseOrder());
                }
                break;

                // fix the defult case to match that it doesn't show files that starts with '.'
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

                case "rm":

                case "cat":

                case "help":

                case "exit":

                default:
                    System.out.println("Error, False command");
                    break;
            }

        }
    }

}