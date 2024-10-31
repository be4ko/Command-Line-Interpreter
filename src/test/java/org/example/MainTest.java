package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.Arrays;

class MainTest {
    private static final Path TEST_DIR = Paths.get("testDir");
    private static final Path TEST_FILE1 = TEST_DIR.resolve("testFile1.txt");
    private static final Path TEST_FILE2 = TEST_DIR.resolve("testFile2.txt");
    private static final Path NON_EXISTENT_FILE = TEST_DIR.resolve("nonExistent.txt");

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
        assertEquals(Arrays.toString(new String[] { "pom.xml", "src", "target" }), Arrays.toString(files),
                "The files should match the expected output.");
    }

    @Test
    void testLsA() {
        var cli = new Main();
        String[] files = cli.ls("-a");
        assertTrue(files.length > 0, "The ls command should return the list of files in the directory.");
        assertEquals(Arrays.toString(new String[] { ".git", ".gitignore", ".idea", "pom.xml", "src", "target" }),
                Arrays.toString(files), "The files should match the expected output.");
    }

    @Test
    void testLsR() {
        var cli = new Main();
        String[] files = cli.ls("-r");
        assertTrue(files.length > 0, "The ls command should return the list of files in the directory.");
        assertEquals(Arrays.toString(new String[] { "target", "src", "pom.xml" }),
                Arrays.toString(files), "The files should match the expected output.");
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

    // @Test
    // void testPWD() {
    // var cli = new Main();
    // String currentPath = cli.pwd();
    // assertEquals("C:\\Users\\Beeko\\Desktop\\Command-Line-Interpreter",
    // currentPath);
    // }

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

    @Test
    public void testCatMultipleFiles() throws IOException {
        var cli = new Main();
        Files.createDirectories(TEST_DIR);

        // Create test files with content
        try (BufferedWriter writer = Files.newBufferedWriter(TEST_FILE1)) {
            writer.write("Content of testFile1");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(TEST_FILE2)) {
            writer.write("Content of testFile2");
        }
        cli.cd(TEST_DIR.toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        cli.cat(TEST_FILE1.getFileName().toString(), TEST_FILE2.getFileName().toString());

        // Verify output contains contents of both files
        String expectedOutput = "Content of testFile1\r\nContent of testFile2\r\n";
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void testCatWithNonExistentFile() throws IOException {
        var cli = new Main();
        Files.createDirectories(TEST_DIR);

        // Create test files with content
        try (BufferedWriter writer = Files.newBufferedWriter(TEST_FILE1)) {
            writer.write("Content of testFile1");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(TEST_FILE2)) {
            writer.write("Content of testFile2");
        }
        cli.cd(TEST_DIR.toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        cli.cat(NON_EXISTENT_FILE.getFileName().toString());

        // Verify output shows "No such file" for the non-existent file
        String expectedOutput = "cat: nonExistent.txt: No such file\r\n";
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void testCatWithDirectory() throws IOException {
        var cli = new Main();
        Files.createDirectories(TEST_DIR);

        // Create test files with content
        try (BufferedWriter writer = Files.newBufferedWriter(TEST_FILE1)) {
            writer.write("Content of testFile1");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(TEST_FILE2)) {
            writer.write("Content of testFile2");
        }
        Path subDir = Files.createDirectory(TEST_DIR.resolve("subDir"));

        cli.cd(TEST_DIR.toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        cli.cat(subDir.getFileName().toString());

        // Verify output shows "Is a directory" for the directory
        String expectedOutput = "cat: subDir: Is a directory\r\n";
        assertEquals(expectedOutput, outputStream.toString());
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
