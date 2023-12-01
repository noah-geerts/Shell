package uk.ac.ucl.shell;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
    	
    	assertTrue(output.equals(expected));
    }
    
    @Test
    public void testLsOneArgNoOutput() throws IOException {
    	Application Ls = new Ls();
    	ArrayList<String> args = new ArrayList<>(Arrays.asList("subDirectory"));
    	Ls.exec(args, "", writer);
    	
    	String output = capture.toString();
    	String expected = "";
    	
    	assertTrue(output.equals(expected));
    }
    
    @Test
    public void testLsTwoOrMoreArgs() throws IOException {
    	//test with two args
    	Application Ls = new Ls();
    	ArrayList<String> args = new ArrayList<>(Arrays.asList("subDirectory", "nonexistentDirectory"));
    	RuntimeException e = assertThrows(RuntimeException.class, () -> {
            Ls.exec(args, abcPath, writer);
        });
    	assertTrue(e.getMessage().equals("ls: too many arguments"));
    	
    	//test with four args
    	ArrayList<String> args2 = new ArrayList<>(Arrays.asList("subDirectory", "nonexistentDirectory1",
    			"nonexistentDiretory2", "nonexistentDirectory3"));
    	e = assertThrows(RuntimeException.class, () -> {
            Ls.exec(args2, abcPath, writer);
        });
    	assertTrue(e.getMessage().equals("ls: too many arguments"));
    }
    
    @Test
    public void testLsNonexistentDirectory() throws IOException {
    	Application Ls = new Ls();
    	ArrayList<String> args = new ArrayList<>(Arrays.asList("nonexistentDirectory"));
    	RuntimeException e = assertThrows(RuntimeException.class, () -> {
            Ls.exec(args, abcPath, writer);
        });
    	assertTrue(e.getMessage().equals("ls: no such directory"));
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
        assertTrue(e.getMessage().equals("cd: missing argument"));
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
        assertTrue(e.getMessage().equals("cd: too many arguments"));
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
        assertTrue(e.getMessage().equals("cd: nonexistentDirectory is not an existing directory"));
    }

   
}
