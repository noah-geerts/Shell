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
	
	@Before
    public void setUp() throws IOException {
        //set up test directories
        Path directory = Paths.get(directoryPath);
        Path subDirectory = Paths.get(subDirectoryPath);
        Files.createDirectory(directory);
        Files.createDirectory(subDirectory);
        
        //set up test files
        FileWriter writer = new FileWriter(testGrepPath);
        writer.write("This is a line containing the pattern.\nThis is a line that doesn't.");
        writer.close();
        
        writer = new FileWriter(abcPath);
        writer.write("AAA\nBBB\nCCC");
        writer.close();
        
        //set shell directory to test directory
        Shell.setCurrentDirectory(directory.toAbsolutePath().toString());
    }

    @After
    public void tearDown() {
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
    }
    
    @Test
    public void testLsNoArgs() throws IOException {
    	Application Ls = new Ls();
    	ByteArrayOutputStream capture = new ByteArrayOutputStream();
    	OutputStreamWriter writer = new OutputStreamWriter(capture);
    	Ls.exec(new ArrayList<String>(), "", writer);
    	
    	String output = capture.toString();
    	String expected = "abc.txt\tsubDirectory\ttestGrep.txt\t" + System.getProperty("line.separator");
    	
    	assertTrue(output.equals(expected));
    }
    
    @Test
    public void testLsOneArgNoOutput() throws IOException {
    	Application Ls = new Ls();
    	ByteArrayOutputStream capture = new ByteArrayOutputStream();
    	OutputStreamWriter writer = new OutputStreamWriter(capture);
    	ArrayList<String> args = new ArrayList<>(Arrays.asList("subDirectory"));
    	Ls.exec(args, "", writer);
    	
    	String output = capture.toString();
    	String expected = "";
    	
    	assertTrue(output.equals(expected));
    }
    
    @Test
    public void testLsTwoOrMoreArgs() throws IOException {
    	Application Ls = new Ls();
    	ByteArrayOutputStream capture = new ByteArrayOutputStream();
    	OutputStreamWriter writer = new OutputStreamWriter(capture);
    	ArrayList<String> args = new ArrayList<>(Arrays.asList("subDirectory", "nonexistentDirectory"));
    	RuntimeException e = assertThrows(RuntimeException.class, () -> {
            Ls.exec(args, abcPath, writer);
        });
    	assertTrue(e.getMessage().equals("ls: too many arguments"));
    	
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
    	ByteArrayOutputStream capture = new ByteArrayOutputStream();
    	OutputStreamWriter writer = new OutputStreamWriter(capture);
    	ArrayList<String> args = new ArrayList<>(Arrays.asList("nonexistentDirectory"));
    	RuntimeException e = assertThrows(RuntimeException.class, () -> {
            Ls.exec(args, abcPath, writer);
        });
    	assertTrue(e.getMessage().equals("ls: no such directory"));
    }
    
   
}
