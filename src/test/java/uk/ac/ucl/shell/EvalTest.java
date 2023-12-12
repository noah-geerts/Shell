package uk.ac.ucl.shell;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvalTest {
	
	static String directoryPath = "testDirectory";
	static String subDirectoryPath = directoryPath + System.getProperty("file.separator") + "subDirectory";
	static String testTxtPath = directoryPath + System.getProperty("file.separator") + "test.txt";
	static String newTxtPath = directoryPath + System.getProperty("file.separator") + "new.txt";
	static String oldDirectory = Shell.getCurrentDirectory();
	@Before
    public void setUp() throws IOException {
        // set up test directories
        Path directory = Paths.get(directoryPath);
        Files.createDirectory(directory);
        Path subDirectory = Paths.get(subDirectoryPath);
        Files.createDirectory(subDirectory);
        
        //set up test files
        FileWriter fileWriter = new FileWriter(testTxtPath);
        fileWriter.write("foo");
        fileWriter.close();
        
        fileWriter = new FileWriter(newTxtPath);
        fileWriter.write("");
        fileWriter.close();
        
        // set shell directory to test directory
        Shell.setCurrentDirectory(directory.toAbsolutePath().toString());
    }
	
    @After
    public void tearDown() throws IOException {
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
    	
    	//set shell directory back to original
    	//Shell.setCurrentDirectory(System.getProperty("user.dir"));
		Shell.setCurrentDirectory(oldDirectory);
    }
    
    
    ///////////////////////////
    //IO HELPER METHODS TESTS//
    ///////////////////////////
    
    @Test
    public void testGetFileTextIsDirectory() throws IOException {
    	try {
			Eval.getFileText("subDirectory");
			fail("No exception thrown");
        } catch(FileNotFoundException e) {
        	assertTrue(e.getMessage().equals("Input file may not be a directory"));
        }
    }
    
    @Test
    public void testGetFileTextFileNotExist() throws IOException {
    	try {
    		Eval.getFileText("nonexistent.txt");
			fail("No exception thrown");
        } catch(FileNotFoundException e) {
        	assertTrue(e.getMessage().equals("File nonexistent.txt does not exist"));
        }
    }
    
    @Test
    public void testGetOutputWriterIsDirectory() throws IOException {
    	try {
    		Eval.getOutputWriter("subDirectory");
			fail("No exception thrown");
        } catch(FileNotFoundException e) {
        	assertTrue(e.getMessage().equals("Output file may not be a directory"));
        }
    	
    }
	
	
	/////////////////////////
	//PATTERN MATCHER TESTS//
	/////////////////////////
	
	
	@Test
	public void testPatternMatcherNoCommandSubstitutionArgsBeforeIORedirection() throws IOException {
		String atomicCommand = "appName arg1 arg2 'arg 3 spaces singlequotes' \"arg 4 spaces doublequotes\" "
										+ "< in1 < in2 > out1 > out2 ";
		ArrayList<String> appArgs = new ArrayList<String>();
		ArrayList<String> inputFileNames = new ArrayList<String>();
		ArrayList<String> outputFileNames = new ArrayList<String>();
		
		String appName = Eval.patternMatcher(atomicCommand, appArgs, inputFileNames, outputFileNames);
		
		String expectedAppName = "appName";
		ArrayList<String> expectedAppArgs = new ArrayList<>(Arrays.asList("arg1", "arg2", "arg 3 spaces singlequotes", "arg 4 spaces doublequotes"));
		ArrayList<String> expectedinputFiles = new ArrayList<>(Arrays.asList("in1", "in2"));
		ArrayList<String> expectedoutputFiles = new ArrayList<>(Arrays.asList("out1", "out2"));
		
		assertTrue(appName.equals(expectedAppName) && appArgs.equals(expectedAppArgs) 
				&& inputFileNames.equals(expectedinputFiles) && outputFileNames.equals(expectedoutputFiles));
	}
	
	@Test
	public void testPatternMatcherNoCommandSubstitutionArgsAfterIORedirection() throws IOException {
		String atomicCommand = "_appName < in > out arg1 arg2";
		ArrayList<String> appArgs = new ArrayList<String>();
		ArrayList<String> inputFileNames = new ArrayList<String>();
		ArrayList<String> outputFileNames = new ArrayList<String>();
		
		String appName = Eval.patternMatcher(atomicCommand, appArgs, inputFileNames, outputFileNames);
		
		String expectedAppName = "_appName";
		ArrayList<String> expectedAppArgs = new ArrayList<>(Arrays.asList("arg1", "arg2"));
		ArrayList<String> expectedinputFiles = new ArrayList<>(Arrays.asList("in"));
		ArrayList<String> expectedoutputFiles = new ArrayList<>(Arrays.asList("out"));
		
		assertTrue(appName.equals(expectedAppName) && appArgs.equals(expectedAppArgs) 
				&& inputFileNames.equals(expectedinputFiles) && outputFileNames.equals(expectedoutputFiles));
	}
	
	@Test
	public void testPatternMatcherSingleCommandSubstitution() throws IOException {
		String atomicCommand = "appName `echo arg1 arg2`";
		ArrayList<String> appArgs = new ArrayList<String>();
		ArrayList<String> inputFileNames = new ArrayList<String>();
		ArrayList<String> outputFileNames = new ArrayList<String>();
		
		String appName = Eval.patternMatcher(atomicCommand, appArgs, inputFileNames, outputFileNames);
		
		String expectedAppName = "appName";
		ArrayList<String> expectedAppArgs = new ArrayList<>(Arrays.asList("arg1", "arg2"));
		ArrayList<String> expectedinputFiles = new ArrayList<>();
		ArrayList<String> expectedoutputFiles = new ArrayList<>();
		
		assertTrue(appName.equals(expectedAppName) && appArgs.equals(expectedAppArgs) 
				&& inputFileNames.equals(expectedinputFiles) && outputFileNames.equals(expectedoutputFiles));
		
	}
	
	@Test
	public void testPatternMatcherMultipleCommandSubstitutions() throws IOException {
		String atomicCommand = "appName `echo arg1` `echo '< in'`";
		ArrayList<String> appArgs = new ArrayList<String>();
		ArrayList<String> inputFileNames = new ArrayList<String>();
		ArrayList<String> outputFileNames = new ArrayList<String>();
		
		String appName = Eval.patternMatcher(atomicCommand, appArgs, inputFileNames, outputFileNames);
		
		String expectedAppName = "appName";
		ArrayList<String> expectedAppArgs = new ArrayList<>(Arrays.asList("arg1"));
		ArrayList<String> expectedinputFiles = new ArrayList<>(Arrays.asList("in"));
		ArrayList<String> expectedoutputFiles = new ArrayList<>();
		
		assertTrue(appName.equals(expectedAppName) && appArgs.equals(expectedAppArgs) 
				&& inputFileNames.equals(expectedinputFiles) && outputFileNames.equals(expectedoutputFiles));
		
	}
	
	@Test
	public void testPatternMatcherInvalidAtomicCommand() throws IOException {
		String atomicCommand = "";
		ArrayList<String> appArgs = new ArrayList<String>();
		ArrayList<String> inputFileNames = new ArrayList<String>();
		ArrayList<String> outputFileNames = new ArrayList<String>();
		
		
		try {
			Eval.patternMatcher(atomicCommand, appArgs, inputFileNames, outputFileNames);
			fail("No exception thrown");
        } catch(RuntimeException e) {
        	assertTrue("Command: '' is invalid".equals(e.getMessage()));
        }
	}
	
	@Test
	public void testPatternMatcherGlobbing() throws IOException {
		String atomicCommand = "appName *.txt";
		ArrayList<String> appArgs = new ArrayList<String>();
		ArrayList<String> inputFileNames = new ArrayList<String>();
		ArrayList<String> outputFileNames = new ArrayList<String>();
		
		String appName = Eval.patternMatcher(atomicCommand, appArgs, inputFileNames, outputFileNames);
		Collections.sort(appArgs);
		
		String expectedAppName = "appName";
		ArrayList<String> expectedAppArgs = new ArrayList<>(Arrays.asList("new.txt", "test.txt"));
		Collections.sort(expectedAppArgs);
		ArrayList<String> expectedinputFiles = new ArrayList<>();
		ArrayList<String> expectedoutputFiles = new ArrayList<>();
		
		assertTrue(appName.equals(expectedAppName) && appArgs.equals(expectedAppArgs) 
				&& inputFileNames.equals(expectedinputFiles) && outputFileNames.equals(expectedoutputFiles));
	}
	
	@Test
	public void testPatternMatcherQuotedIORedirection() throws IOException {
		String atomicCommand = "appName < 'quoted input' > \"quoted output\"";
		ArrayList<String> appArgs = new ArrayList<String>();
		ArrayList<String> inputFileNames = new ArrayList<String>();
		ArrayList<String> outputFileNames = new ArrayList<String>();
		
		String appName = Eval.patternMatcher(atomicCommand, appArgs, inputFileNames, outputFileNames);
		
		String expectedAppName = "appName";
		ArrayList<String> expectedAppArgs = new ArrayList<>();
		ArrayList<String> expectedinputFiles = new ArrayList<>(Arrays.asList("quoted input"));
		ArrayList<String> expectedoutputFiles = new ArrayList<>(Arrays.asList("quoted output"));
		
		assertTrue(appName.equals(expectedAppName) && appArgs.equals(expectedAppArgs) 
				&& inputFileNames.equals(expectedinputFiles) && outputFileNames.equals(expectedoutputFiles));
	}

	
	//////////////////////////////////////
	//RUN APP AND UNSAFE DECORATOR TESTS//
	//////////////////////////////////////
	
	
	
	@Test
	public void testRunAppNotUnsafeNoIORedirection() throws IOException {
		String appName = "echo";
		ArrayList<String> AppArgs = new ArrayList<>(Arrays.asList("foo"));
		ArrayList<String> inputFileNames = new ArrayList<>();
		ArrayList<String> outputFileNames = new ArrayList<>();
		String appInput = "";
		ByteArrayOutputStream appOutput = new ByteArrayOutputStream();
		
		Eval.runApp(appName, AppArgs, inputFileNames, outputFileNames, appInput, appOutput);
		
		String output = appOutput.toString();
		String expected = "foo " + System.getProperty("line.separator");
		
		assertTrue(output.equals(expected));
		
	}
	
	@Test
	public void testRunAppUnsafeNoIORedirection() throws IOException {
		String appName = "_cat";
		ArrayList<String> AppArgs = new ArrayList<>();
		ArrayList<String> inputFileNames = new ArrayList<>();
		ArrayList<String> outputFileNames = new ArrayList<>();
		String appInput = "";
		ByteArrayOutputStream appOutput = new ByteArrayOutputStream();
		
		Eval.runApp(appName, AppArgs, inputFileNames, outputFileNames, appInput, appOutput);
		
		String output = appOutput.toString();
		String expected = "cat: missing arguments / empty stdin" + System.getProperty("line.separator");
		
		assertTrue(output.equals(expected));
		
	}
	
	@Test
	public void testRunAppNotUnsafeWithOneInputOneOutput() throws IOException {
		String appName = "cat";
		ArrayList<String> AppArgs = new ArrayList<>();
		ArrayList<String> inputFileNames = new ArrayList<>(Arrays.asList("test.txt"));
		ArrayList<String> outputFileNames = new ArrayList<>(Arrays.asList("new.txt"));
		String appInput = "";
		ByteArrayOutputStream appOutput = new ByteArrayOutputStream();
		
		Eval.runApp(appName, AppArgs, inputFileNames, outputFileNames, appInput, appOutput);
		
		String content = Files.readString(Paths.get(newTxtPath), StandardCharsets.UTF_8);
		String expected = "foo" + System.getProperty("line.separator");
		
		assertTrue(content.equals(expected));
	}
	
	@Test
	public void testRunAppMultipleInputRedirections() throws IOException {
		String appName = "cat";
		ArrayList<String> AppArgs = new ArrayList<>();
		ArrayList<String> inputFileNames = new ArrayList<>(Arrays.asList("in1", "in2"));
		ArrayList<String> outputFileNames = new ArrayList<>();
		String appInput = "";
		ByteArrayOutputStream appOutput = new ByteArrayOutputStream();
		
		try {
			Eval.runApp(appName, AppArgs, inputFileNames, outputFileNames, appInput, appOutput);
			fail("No exception thrown");
        } catch(RuntimeException e) {
        	assertTrue(e.getMessage().equals("Only one input redirection permitted"));
        }
	}
	
	@Test
	public void testRunAppMultipleOutputRedirections() throws IOException {
		String appName = "cat";
		ArrayList<String> AppArgs = new ArrayList<>();
		ArrayList<String> inputFileNames = new ArrayList<>();
		ArrayList<String> outputFileNames = new ArrayList<>(Arrays.asList("out1", "out2"));
		String appInput = "";
		ByteArrayOutputStream appOutput = new ByteArrayOutputStream();
		
		try {
			Eval.runApp(appName, AppArgs, inputFileNames, outputFileNames, appInput, appOutput);
			fail("No exception thrown");
        } catch(RuntimeException e) {
        	assertTrue(e.getMessage().equals("Only one output redirection permitted"));
        }
	}
	
	
	/////////////////////////
	//PIPE, SEQ, CALL TESTS//
	/////////////////////////
	
	
	@Test
	public void testCall() throws IOException {
		ByteArrayOutputStream capture = new ByteArrayOutputStream();
		Call c = new Call("echo hi", "", capture);
		CommandVisitor e = new Eval();
		c.accept(e);
		
		String output = capture.toString();
		String expected = "hi " + System.getProperty("line.separator");
		
		assertTrue(output.equals(expected));
	}
	
	@Test
	public void testSeqExecutionOrder() throws IOException {
		ByteArrayOutputStream capture = new ByteArrayOutputStream();
		
		Command left = new Call("echo 1", "", capture);
		Command right = new Call("echo 2", "", capture);
		Command seq = new Seq(left, right, "", System.out);
		
		CommandVisitor e = new Eval();
		seq.accept(e);
		
		String output = capture.toString();
		String expected = "1 " + System.getProperty("line.separator") + "2 " + System.getProperty("line.separator");

		assertTrue(output.equals(expected));
	}
	
	@Test
	public void testPipeRightInputIsLeftOutputAndRightOutputIsPipeOutput() throws IOException {
		ByteArrayOutputStream capture = new ByteArrayOutputStream();
		
		Command left = new Call("echo 'Hello World'", "", System.out);
		Command right = new Call("cat", "", System.out);
		Command pipe = new Pipe(left, right, "", capture);
		
		CommandVisitor e = new Eval();
		pipe.accept(e);
		
		String output = capture.toString();
		String expected = "Hello World " + System.getProperty("line.separator") + System.getProperty("line.separator");
		
		assertTrue(output.equals(expected));
	}

	
	
}
