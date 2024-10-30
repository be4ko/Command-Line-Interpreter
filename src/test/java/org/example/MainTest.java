package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

class MainTest {

    @Test
    void testCd() {
        var cli = new Main();
        cli.cd("src");
        assertEquals(Paths.get(System.getProperty("user.dir"), "src").toString(), cli.pwd());
    }

    @Test
    void testLs() {
        var cli = new Main();
        cli.cd("src");
        String[] files = cli.ls();
        assertNotNull(files);
        assertTrue(files.length > 0, "The ls command should return the list of files in the directory.");
    }

    @Test
    void testLsA() {
        var cli = new Main();
        cli.cd("/home/user/Documents");
        String[] files = cli.ls("-a");
        boolean hasHiddenFiles = false;
        for (String file : files) {
            if (file.startsWith(".")) {
                hasHiddenFiles = true;
                break;
            }
        }
        assertTrue(hasHiddenFiles, "The ls -a command should list hidden files.");
    }

    @Test
    void testLsR() {
        var cli = new Main();
        cli.cd("/home/user/Documents");
        String[] files = cli.ls();
        String[] reverseFiles = cli.ls("-r");
        // Check if the reverse array is in the opposite order of the regular ls output
        boolean isReversed = true;
        for (int i = 0; i < files.length; i++) {
            if (!files[i].equals(reverseFiles[files.length - i - 1])) {
                isReversed = false;
                break;
            }
        }
        assertTrue(isReversed, "The ls -r command should list files in reverse order.");
    }

    @Test
    void testPWD() {
        var cli = new Main();
        String currentPath = cli.pwd();
        assertEquals(Paths.get(System.getProperty("user.dir")).toString(), currentPath);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(Paths.get("testDir"))
                .map(Path::toFile)
                .forEach(File::delete);
        Files.deleteIfExists(Paths.get("testDir"));
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

}
