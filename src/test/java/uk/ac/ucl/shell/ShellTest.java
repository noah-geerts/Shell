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
	static String testGrepPath = directoryPath + "/" + "testGrep.txt";
	static String abcPath = directoryPath + "/abc.txt";
	
	@Before
    public void setUp() throws IOException {
        //set up test directory
        Path path = Paths.get(directoryPath);
        Path subPath = Paths.get(directoryPath + "/subDirectory");
        Files.createDirectory(path);
        Files.createDirectory(subPath);
        
        //set up test files
        FileWriter writer = new FileWriter(testGrepPath);
        writer.write("This is a line containing the pattern.\nThis is a line that doesn't.");
        writer.close();
        
        writer = new FileWriter(abcPath);
        writer.write("AAA\nBBB\nCCC");
        writer.close();
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
    public void testLsFoundFilesAndDirectory() throws IOException {
    	Application Ls = new Ls();
    	ByteArrayOutputStream capture = new ByteArrayOutputStream();
    	OutputStreamWriter writer = new OutputStreamWriter(capture);
    	Ls.exec(new ArrayList<String>(), "", writer);
    	writer.flush();
    	writer.close();
    	
    	String output = writer.toString();
    	String expected = "subDirectory\ntestGrep.txt\nabc.txt\n";
    	
    	assertTrue(output.equals(expected));
    }
    
    
   
}
