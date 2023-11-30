package uk.ac.ucl.shell;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShellTest {

    @Test
    public void testHead() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        List<String> appArgs = new ArrayList<>(Arrays.asList("5", "testHead.txt"));
        String input = "";
        // it is testing apps through creating an instance,
        // not actually running the shell
        Head head = new Head();
        head.exec(new ArrayList<>(appArgs), input, writer);

        String expectedOutput = "line 1\nline 2\nline 3\nline 4\nline 5\n";
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void testGrep() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        List<String> appArgs = new ArrayList<>(Arrays.asList("pattern", "testGrep.txt"));
        String input = "";

        Grep grep = new Grep();
        grep.exec(new ArrayList<>(appArgs), input, writer);

        String expectedOutput = "This is a line containing the pattern.\n";
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void testCut() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        List<String> appArgs = new ArrayList<>(Arrays.asList("-b", "1-3", "testCut.txt"));
        String input = "";

        Cut cut = new Cut();
        cut.exec(new ArrayList<>(appArgs), input, writer);

        String expectedOutput = "lin\ntex\nt\n";
        assertEquals(expectedOutput, outputStream.toString());
    }


    @Test
    public void testPwd() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        Pwd pwd = new Pwd();
        String input = "";
        pwd.exec(new ArrayList<>(), input, writer);

        String expectedOutput = System.getProperty("user.dir") + System.getProperty("line.separator");
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void testCat() throws IOException {
        //Temporary Directory
        String testFileName = "testCat.txt";
        String testContent = "Line 1\nLine 2\nLine 3\n";
        Path testFilePath = Paths.get(Shell.getCurrentDirectory(), testFileName);
        Files.write(testFilePath, testContent.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        // Match file content with expected output
        Cat cat = new Cat();
        List<String> appArgs = new ArrayList<>(Arrays.asList(testFileName));
        String input = "";
        cat.exec(new ArrayList<>(appArgs), input, writer);

        String expectedOutput = testContent;
        assertEquals(expectedOutput, outputStream.toString());

        //Missing File
        List<String> missingFileArgs = new ArrayList<>(Arrays.asList("nonexistentFile.txt"));
        try {
            cat.exec(new ArrayList<>(missingFileArgs), input, writer);
            throw new AssertionError("No exception thrown");
        } catch (RuntimeException e) {
            // Expected RuntimeException
        }

        // Empty stdin
        List<String> emptyArgs = new ArrayList<>();
        try {
            cat.exec(new ArrayList<>(emptyArgs), input, writer);
            throw new AssertionError("No exception thrown");
        } catch (RuntimeException e) {
            // Expected RuntimeException
        }

        // Testing with stdin content
        String inputContent = "Additional line\nAnother line\n";
        cat.exec(new ArrayList<>(), inputContent, writer);

        expectedOutput = inputContent;
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void testCd() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);

        Cd cd = new Cd();

        //  Valid
        ArrayList<String> validArgs = new ArrayList<>(Arrays.asList("someDirectory"));
        cd.exec(validArgs, "", writer);
        String expectedDirectory = "/path/to/current/directory/someDirectory";
        assertEquals(expectedDirectory, Shell.getCurrentDirectory());

        // Missing argument
        ArrayList<String> missingArgument = new ArrayList<>();
        try {
            cd.exec(missingArgument, "", writer);
            throw new AssertionError("No exception thrown");
        } catch (RuntimeException e) {
            assertEquals("cd: missing argument", e.getMessage());
        }

        // Too many arguments
        ArrayList<String> tooManyArguments = new ArrayList<>(Arrays.asList("dir1", "dir2"));
        try {
            cd.exec(tooManyArguments, "", writer);
            throw new AssertionError("No exception thrown");
        } catch (RuntimeException e) {
            assertEquals("cd: too many arguments", e.getMessage());
        }

        // Change to a non-existent directory
        ArrayList<String> nonExistentDirectory = new ArrayList<>(Arrays.asList("nonExistentDir"));
        try {
            cd.exec(nonExistentDirectory, "", writer);
            throw new AssertionError("Expected RuntimeException but no exception was thrown");
        } catch (RuntimeException e) {
            assertEquals("cd: nonExistentDir is not an existing directory", e.getMessage());
        }
    }

}
