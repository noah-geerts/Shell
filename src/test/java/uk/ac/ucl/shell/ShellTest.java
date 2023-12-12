package uk.ac.ucl.shell;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ShellTest {

	static String directoryPath = "testDirectory";
	static String subDirectoryEmpty = directoryPath + System.getProperty("file.separator") + "subDirectoryempty";
	static String subDirectoryPath = directoryPath + System.getProperty("file.separator") + "subDirectory";
	static String testGrepPath = directoryPath + System.getProperty("file.separator") + "testGrep.txt";
	static String abcPath = directoryPath + System.getProperty("file.separator") + "abc.txt";
	static String singleLine = directoryPath + System.getProperty("file.separator") + "singleLine.txt";
	static String multipleLines = directoryPath + System.getProperty("file.separator") + "multipleLines.txt";
	static String mixedContent = directoryPath + System.getProperty("file.separator") + "mixedContent.txt";
	static String emptyFile = directoryPath + System.getProperty("file.separator") + "emptyFile.txt";
	static String subemptyFile = subDirectoryPath + System.getProperty("file.separator") + "subemptyFile.txt";
	static String singleLineFileContent = "This is a single line.";
	static String singleLineFileName = "singleLine.txt";
	static String multipleLinesFileName = "multipleLines.txt";
	static String mixedContentFileName = "mixedContent.txt";
	static String emptyFileName = "emptyFile.txt";
	static ByteArrayOutputStream capture;
	static OutputStreamWriter writer;
	static String sSeperator = System.getProperty("line.separator");

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Before
	public void setUp() throws IOException {
		// set up test directories
		Path directory = Paths.get(directoryPath);
//		Path subDirectory = Paths.get(subDirectoryPath);
//		Path subDirectoryEmptydir = Paths.get(subDirectoryEmpty);
		Files.createDirectory(directory);
//		Files.createDirectory(subDirectory);
//		Files.createDirectory(subDirectoryEmptydir);

//		// set up test files
//		FileWriter fileWriter = new FileWriter(testGrepPath);
//		fileWriter.write("This is a line containing the pattern." + sSeperator + "This is a line that doesn't.");
//		fileWriter.close();
//
//		fileWriter = new FileWriter(abcPath);
//		fileWriter.write("AAA" + sSeperator + "BBB" + sSeperator + "CCC");
//		fileWriter.close();
//
//		fileWriter = new FileWriter(singleLine);
//		fileWriter.write(singleLineFileContent);
//		fileWriter.close();
//
//		fileWriter = new FileWriter(multipleLines);
//		fileWriter.write("Line 1" + sSeperator + "Line 2" + sSeperator + "Line 3");
//		fileWriter.close();
//
//		fileWriter = new FileWriter(mixedContent);
//		fileWriter.write("This is a line." + sSeperator + "Line 2" + sSeperator + "Another line.");
//		fileWriter.close();
//
//		fileWriter = new FileWriter(emptyFile);
//		fileWriter.write("");
//		fileWriter.close();
//
//		fileWriter = new FileWriter(subemptyFile);
//		fileWriter.write("Sample" + sSeperator + "empty" + sSeperator + "file" + sSeperator);
//		fileWriter.close();

		// set shell directory to test directory
		Shell.setCurrentDirectory(directory.toAbsolutePath().toString());

		// set up writer and capture objects to store of apps
		capture = new ByteArrayOutputStream();
		writer = new OutputStreamWriter(capture);
	}

	@After
	public void tearDown() throws IOException {
		// delete test directory and all test files
		try {
			Path directory = Paths.get(directoryPath);
			Files.walk(directory).sorted((p1, p2) -> -p1.compareTo(p2)) // Reverse order for directories first
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

		// close writer and capture
		writer.close();
		capture.close();
	}

	@Test
	public void testWrongNoOfArgs() throws IOException {
		String[] args = { "" };
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));
		Shell.main(args);

		String output = capture.toString();
		String expected = "COMP0010 shell: wrong number of arguments" + sSeperator;

		assertEquals(expected, outContent.toString());
	}

	@Test
	public void testWrongNoOfTwoArgs() throws IOException {
		String[] args = { "invalid", "" };
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));
		Shell.main(args);

		String output = capture.toString();
		String expected = "COMP0010 shell: invalid: unexpected argument" + sSeperator;

		assertEquals(expected, outContent.toString());
	}

	@Test
	public void testNoArgs() throws IOException {
		String[] args = { "-c", "ls" };
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setErr(new PrintStream(outContent));
		Shell.main(args);

		assertEquals("", outContent.toString());
	}

}
