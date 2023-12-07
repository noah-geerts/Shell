package uk.ac.ucl.shell;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShellTest {

	static String directoryPath = "testDirectory";
	static String subDirectoryPath = directoryPath + "/subDirectory";
	static String testGrepPath = directoryPath + "/testGrep.txt";
	static String abcPath = directoryPath + "/abc.txt";
	static ByteArrayOutputStream capture;
	static OutputStreamWriter writer;
	
	@Before
    public void setUp() throws IOException {
        //set up test directories
        Path directory = Paths.get(directoryPath);
        Path subDirectory = Paths.get(subDirectoryPath);
        Files.createDirectory(directory);
        Files.createDirectory(subDirectory);
        
        //set up test files
        FileWriter fileWriter = new FileWriter(testGrepPath);
        fileWriter.write("This is a line containing the pattern.\nThis is a line that doesn't.");
        fileWriter.close();
        
        fileWriter = new FileWriter(abcPath);
        fileWriter.write("AAA\nBBB\nCCC");
        fileWriter.close();
        
        //set shell directory to test directory
        Shell.setCurrentDirectory(directory.toAbsolutePath().toString());
        
        //set up writer and capture objects to store of apps
        capture = new ByteArrayOutputStream();
    	writer = new OutputStreamWriter(capture);
    }

    @After
    public void tearDown() throws IOException {
    	//delete test directory and all test files
        try {
            Path directory = Paths.get(directoryPath);
            Files.walk(directory)
                 .sorted((p1, p2) -> -p1.compareTo(p2)) // Reverse order for directories first
                 .forEach(p -> {
                     try {
                         Files.delete(p);
                     } catch (IOException e) {
                         System.err.println("Failed to delete: " + p + " - " + e.getMessage());
                     }
                 });
        } catch (IOException e) {
            System.err.println("Failed to delete directory: " + e.getMessage());
        }
       
        //close writer and capture
        writer.close();
        capture.close();
    }
    
    @Test
    public void testLsNoArgs() throws IOException {
    	Application Ls = new Ls();
    	Ls.exec(new ArrayList<String>(), "", writer);
    	
    	String output = capture.toString();
    	String expected = "abc.txt\tsubDirectory\ttestGrep.txt\t" + System.getProperty("line.separator");

        assertEquals(output, expected);
    }
    
    @Test
    public void testLsOneArgNoOutput() throws IOException {
    	Application Ls = new Ls();
    	ArrayList<String> args = new ArrayList<>(List.of("subDirectory"));
    	Ls.exec(args, "", writer);
    	
    	String output = capture.toString();
    	String expected = "";

        assertEquals(output, expected);
    }
    
    @Test
    public void testLsTwoOrMoreArgs() throws IOException {
    	//test with two args
    	Application Ls = new Ls();
    	ArrayList<String> args = new ArrayList<>(Arrays.asList("subDirectory", "nonexistentDirectory"));
    	RuntimeException e = assertThrows(RuntimeException.class, () -> {
            Ls.exec(args, abcPath, writer);
        });
        assertEquals("ls: too many arguments", e.getMessage());
    	
    	//test with four args
    	ArrayList<String> args2 = new ArrayList<>(Arrays.asList("subDirectory", "nonexistentDirectory1",
    			"nonexistentDiretory2", "nonexistentDirectory3"));
    	e = assertThrows(RuntimeException.class, () -> {
            Ls.exec(args2, abcPath, writer);
        });
        assertEquals("ls: too many arguments", e.getMessage());
    }
    
    @Test
    public void testLsNonexistentDirectory() throws IOException {
    	Application Ls = new Ls();
    	ArrayList<String> args = new ArrayList<>(Arrays.asList("nonexistentDirectory"));
    	RuntimeException e = assertThrows(RuntimeException.class, () -> {
            Ls.exec(args, abcPath, writer);
        });
        assertEquals("ls: no such directory", e.getMessage());
    }
	
     @Test
    public void testPwdValidExecution() throws IOException {
        Application pwd = new Pwd();
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(capture);

        pwd.exec(new ArrayList<>(), "", writer);
        writer.flush();
        writer.close();

        String output = capture.toString();
        String expected = Shell.getCurrentDirectory() + System.getProperty("line.separator");
        assertEquals(expected, output);
    }

    @Test
    public void testCdValidExecution() throws IOException {
        Application cd = new Cd();
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(capture);

        cd.exec(new ArrayList<>(List.of("subDirectory")), "", writer);
        writer.flush();
        writer.close();

        String output = capture.toString();
        String expected = "";
        assertEquals(expected, output);
    }

    @Test
    public void testCdMissingArgument() throws IOException {
        Application cd = new Cd();
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(capture);

        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            cd.exec(new ArrayList<>(), "", writer);
        });
        assertEquals("cd: missing argument", e.getMessage());
    }

    @Test
    public void testCdTooManyArguments() throws IOException {
        Application cd = new Cd();
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(capture);

        ArrayList<String> args = new ArrayList<>(List.of("dir1", "dir2"));
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            cd.exec(args, "", writer);
        });
        assertEquals("cd: too many arguments", e.getMessage());
    }

    @Test
    public void testCdNonexistentDirectory() throws IOException {
        Application cd = new Cd();
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(capture);

        ArrayList<String> args = new ArrayList<>(List.of("nonexistentDirectory"));
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            cd.exec(args, "", writer);
        });
        assertEquals("cd: nonexistentDirectory is not an existing directory", e.getMessage());
    }

    // for echo also need > text.txt
    @Test
    public void testEchoNoArgsNoInput() throws IOException {
        Application Echo = new Echo();
        Echo.exec(new ArrayList<String>(), "", writer);

        String output = capture.toString();
        String expected = "\n";

        assertEquals(output, expected);
    }
    @Test
    public void testEchoNoArgsSomeInput() throws IOException {
        Application Echo = new Echo();
        Echo.exec(new ArrayList<String>(), "input here\n more input here", writer);

        String output = capture.toString();
        String expected = "\n";

        assertEquals(output, expected);
    }
    @Test
    public void testEchoOneArg() throws IOException {
        Application Echo = new Echo();
        ArrayList<String> args = new ArrayList<>();
        args.add("hello, world");
        Echo.exec(args, "", writer);

        String output = capture.toString();
        String expected = "hello world\n";

        assertEquals(output, expected);
    }
    @Test
    public void testEchoSomeArgs() throws IOException {
        Application Echo = new Echo();
        ArrayList<String> args = new ArrayList<>();
        args.add("Hello,");
        args.add("world!");
        Echo.exec(args, "", writer);

        String output = capture.toString();
        String expected = "Hello, world!\n";

        assertEquals(output, expected);
    }
    @Test
    public void testEchoVeryLargeArgs() throws IOException {
        Application Echo = new Echo();
        ArrayList<String> args = new ArrayList<>();
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i<1000; i++)
        {
            args.add("world!");
            expected.append("world! ");
        }
        args.add("world!");
        expected.append("world!\n");
        Echo.exec(args, "", writer);

        String output = capture.toString();
        assertEquals(output, expected.toString());
    }
	
//for cat we may need multiple txt files?
// singleLine.txt : "This is a single line."
// multipleLines.txt : "Line 1\nLine 2\nLine 3"
// mixedContent.txt : "This is a line.\nLine 2\nAnother line."
    @Test
    public void testCatEmptyFile() throws IOException {
        Application cat = new Cat();
        ArrayList<String> args = new ArrayList<>(Arrays.asList("emptyFile.txt"));
        cat.exec(args, "", writer);

        String output = capture.toString();
        String expected = "";  

        assertEquals(output, expected);
    }

    @Test
    public void testCatSingleLine() throws IOException {
        Application cat = new Cat();
        ArrayList<String> args = new ArrayList<>(Arrays.asList("singleLine.txt"));
        cat.exec(args, "", writer);

        String output = capture.toString();
        String expected = "This is a single line.";
        assertEquals(output, expected);
    }

    @Test
    public void testCatMultipleLines() throws IOException {
        Application cat = new Cat();
        ArrayList<String> args = new ArrayList<>(Arrays.asList("multipleLines.txt"));
        cat.exec(args, "", writer);

        String output = capture.toString();
        String expected = "Line 1\nLine 2\nLine 3";
        assertEquals(output, expected);
    }

    @Test
    public void testCatMixedContent() throws IOException {
        Application cat = new Cat();
        ArrayList<String> args = new ArrayList<>(Arrays.asList("mixedContent.txt"));
        cat.exec(args, "", writer);

        String output = capture.toString();
        String expected = "This is a line.\nLine 2\nAnother line."; 

        assertEquals(output, expected);
    }   
    @Test
    public void testCatLargeFile() throws IOException {
        Application cat = new Cat();
        ArrayList<String> args = new ArrayList<>();
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            String line = "This is line " + (i + 1) + ".";
            args.add(line);
            expected.append(line).append(System.getProperty("line.separator"));
        }
        cat.exec(args, "", writer);

        String output = capture.toString();
        assertEquals(output, expected.toString());
    }

    @Test
    public void testCatFileAndStandardInput() throws IOException {
        String standardInput = "This is input from standard input.";

        ArrayList<String> args = new ArrayList<>(Arrays.asList("singleLine.txt"));
        // Read content from singleLine.txt and append standard input
        String expectedOutput = "This is a single line.\n" + standardInput + System.getProperty("line.separator");

        Application cat = new Cat();
        cat.exec(args, standardInput, writer);

        String output = capture.toString();
        assertEquals(output, expectedOutput);
    }

    @Test
    public void testCatReadFromMultipleFiles() throws IOException {
        ArrayList<String> args = new ArrayList<>(Arrays.asList("singleLine.txt", "multipleLines.txt"));
        
        // Read content from singleLine.txt and multipleLines.txt
        String expectedOutput = "This is a single line.\nLine 1\nLine 2\nLine 3\n";

        Application cat = new Cat();
        cat.exec(args, "", writer);

        String output = capture.toString();
        assertEquals(output, expectedOutput);
    }

    @Test
    public void testCatReadFromStandardInput() throws IOException {
        String standardInput = "This is input from standard input.";
        ArrayList<String> args = new ArrayList<>();
        String expectedOutput = standardInput + System.getProperty("line.separator");

        Application cat = new Cat();
        cat.exec(args, standardInput, writer);

        String output = capture.toString();
        assertEquals(output, expectedOutput);
    }

    @Test
    public void testCatReadFromNonexistentFile() throws IOException {
        // Create arguments for Cat with a nonexistent file
        ArrayList<String> args = new ArrayList<>(Arrays.asList("nonexistentFile.txt"));
        
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            Application cat = new Cat();
            cat.exec(args, "", writer);
        });
        assertEquals("cat: file does not exist", e.getMessage());
    }
}
