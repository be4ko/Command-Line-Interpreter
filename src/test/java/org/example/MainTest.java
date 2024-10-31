package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;



class MainTest {

    @BeforeEach
    void setUp() {
        var cli = new Main();
        // Set the current directory to the test directory where files are located
        cli.cd(Paths.get("path/to/test/directory").toString()); // Convert Path to String
    }
    @Test
    void testCd() {
        var cli = new Main();
        cli.cd("src");
        assertEquals(Paths.get(System.getProperty("user.dir"), "src").toString(), cli.pwd());
    }

    @Test
    void testLs() {
        var cli = new Main();
        String[] files = cli.ls();
        assertTrue(files.length > 0, "The ls command should return the list of files in the directory.");
        assertEquals(Arrays.toString(new String[]{"pom.xml", "src","target"}), Arrays.toString(files), "The files should match the expected output.");
    }

    @Test
    void testLsA() {
        var cli = new Main();
        String[] files = cli.ls("-a");
        assertTrue(files.length > 0, "The ls command should return the list of files in the directory.");
        assertEquals(Arrays.toString(new String[]{".git", ".gitignore",".idea","pom.xml", "src","target"}), Arrays.toString(files), "The files should match the expected output.");
    }

    @Test
    void testLsR() {
        var cli = new Main();
        String[] files = cli.ls("-r");
        assertTrue(files.length > 0, "The ls command should return the list of files in the directory.");
        assertEquals(Arrays.toString(new String[]{"target","src", "pom.xml", ".idea",".gitignore", ".git" }), Arrays.toString(files), "The files should match the expected output.");
    }

    @Test
    void testMkdir() throws IOException {
        var cli = new Main();

        String dirName = "testDir";
        Path dirPath = Paths.get(dirName);

        assertFalse(Files.exists(dirPath), "Directory should not exist before mkdir.");

        assertTrue(cli.mkdir("testDir"), "mkdir should return true for a new directory.");

        assertTrue(Files.exists(dirPath), "Directory should exist after mkdir.");
    }

    @Test
    void testRmdir() throws IOException {
        var cli = new Main();
        cli.mkdir("testDir");
        boolean result = cli.rmdir("testDir");

        assertTrue(result, "rmdir should return true for an empty directory");
        assertFalse(Files.exists(Paths.get("testDir")), "Directory should not exist after rmdir");
    }

    @Test
    void testPWD() {
        var cli = new Main();
        String currentPath = cli.pwd();
        assertEquals("C:\\Users\\Beeko\\Desktop\\Command-Line-Interpreter", currentPath);
    }
    
    @Test
    void testMv() throws IOException {
        var cli = new Main();

        Files.createDirectories(Paths.get("testDir"));
        Path sourceFile = Files.createFile(Paths.get("testDir", "source.txt"));
        Path targetFile = Paths.get("testDir", "target.txt");

        String[] command = { "mv", sourceFile.toString(), targetFile.toString() };
        assertTrue(cli.mv(command));
        assertTrue(Files.exists(targetFile));
        assertFalse(Files.exists(sourceFile));

        Path targetDir = Files.createDirectory(Paths.get("testDir", "targetDir"));
        command = new String[] { "mv", targetFile.toString(), targetDir.toString() };
        assertTrue(cli.mv(command));
        assertTrue(Files.exists(targetDir.resolve("target.txt")));

    }

    @Test
    void testMvNonExistentFile() {
        var cli = new Main();

        String[] command = { "mv", "nonexistent.txt", "target.txt" };
        assertFalse(cli.mv(command));

    }

    @Test
    void testRm() throws IOException {
        var cli = new Main();

        String filePath = "testDir/nonExistentDir/newFile.txt";
        assertTrue(cli.touch(filePath));
        assertTrue(Files.exists(Paths.get(filePath)));

        String[] command = { "rm", filePath };
        assertTrue(cli.rm(command));
        assertFalse(Files.exists(Paths.get(filePath)));
    }

    @Test

    void testRmNonExistentFile() {
        var cli = new Main();

        String[] command = { "rm", "nonexistent.txt" };
        assertFalse(cli.rm(command));
    }

    @Test
    void testTouch() throws IOException {
        var cli = new Main();

        String filePath = "testDir/newFile.txt";
        assertTrue(cli.touch(filePath));
        assertTrue(Files.exists(Paths.get(filePath)));

        assertTrue(cli.touch(filePath));
        assertTrue(Files.exists(Paths.get(filePath)));
    }

    @Test
    void testTouchWithNonExistentDirectory() {
        var cli = new Main();

        String filePath = "testDir/nonExistentDir/newFile.txt";
        assertTrue(cli.touch(filePath));
        assertTrue(Files.exists(Paths.get(filePath)));
    }
    
    @AfterEach
    void tearDown() throws IOException {
        Path testDir = Paths.get("testDir");

        if (Files.exists(testDir)) {
            Files.walk(testDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (file.exists()) {
                            System.out.println("Deleting: " + file.getPath());
                            file.delete();
                        }
                    });

            if (Files.exists(testDir)) {
                Files.delete(testDir);
                System.out.println("Deleted testDir");
            }
        } else {
            System.out.println("No testDir found to delete.");
        }
    }

}
