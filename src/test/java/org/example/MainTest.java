package org.example;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
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
    void testLsR(){
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
    void testPWD(){
        var cli = new Main();
        String currentPath = cli.pwd();
        assertEquals(Paths.get(System.getProperty("user.dir")).toString(), currentPath);
    }
}