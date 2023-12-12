package uk.ac.ucl.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AppsTest {

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
	static String oldDirectory = Shell.getCurrentDirectory();
	static String sSeperator = System.getProperty("line.separator");

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Before
	public void setUp() throws IOException {
		// set up test directories
		Path directory = Paths.get(directoryPath);
		Path subDirectory = Paths.get(subDirectoryPath);
		Path subDirectoryEmptydir = Paths.get(subDirectoryEmpty);
		Files.createDirectory(directory);
		Files.createDirectory(subDirectory);
		Files.createDirectory(subDirectoryEmptydir);

		// set up test files
		FileWriter fileWriter = new FileWriter(testGrepPath);
		fileWriter.write("This is a line containing the pattern." + sSeperator + "This is a line that doesn't.");
		fileWriter.close();

		fileWriter = new FileWriter(abcPath);
		fileWriter.write("AAA" + sSeperator + "BBB" + sSeperator + "CCC");
		fileWriter.close();

		fileWriter = new FileWriter(singleLine);
		fileWriter.write(singleLineFileContent);
		fileWriter.close();

		fileWriter = new FileWriter(multipleLines);
		fileWriter.write("Line 1" + sSeperator + "Line 2" + sSeperator + "Line 3");
		fileWriter.close();

		fileWriter = new FileWriter(mixedContent);
		fileWriter.write("This is a line." + sSeperator + "Line 2" + sSeperator + "Another line.");
		fileWriter.close();

		fileWriter = new FileWriter(emptyFile);
		fileWriter.write("");
		fileWriter.close();

		fileWriter = new FileWriter(subemptyFile);
		fileWriter.write("Sample" + sSeperator + "empty" + sSeperator + "file" + sSeperator);
		fileWriter.close();

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
		Shell.setCurrentDirectory(oldDirectory);
	}

	@Test
	public void testLsNoArgs() throws IOException {
		Application Ls = new Ls();
		Ls.exec(new ArrayList<String>(), "", writer);

		String output = capture.toString();
		String expected = "multipleLines.txt\tsubDirectory\tsingleLine.txt\tabc.txt\temptyFile.txt\ttestGrep.txt\tmixedContent.txt\tsubDirectoryemptyr"
				+ sSeperator;
		String files[] = output.split("\\s+");
		for (String string : files) {
			assertTrue(expected.contains(string));
		}
	}

	@Test
	public void testLsOneArgNoOutput() throws IOException {
		Application Ls = new Ls();
		ArrayList<String> args = new ArrayList<>(List.of("subDirectoryempty/"));
		Ls.exec(args, "", writer);

		String output = capture.toString();
		String expected = "";

		assertEquals(expected, output);
	}

	@Test
	public void testLsTwoOrMoreArgs() throws IOException {
		// test with two args
		Application Ls = new Ls();
		ArrayList<String> args = new ArrayList<>(Arrays.asList("subDirectory", "nonexistentDirectory"));
		exceptionRule.expect(RuntimeException.class);
		Ls.exec(args, abcPath, writer);
		exceptionRule.expectMessage("ls: too many arguments");

		// test with four arguments
		ArrayList<String> args2 = new ArrayList<>(Arrays.asList("subDirectory", "nonexistentDirectory1",
				"nonexistentDiretory2", "nonexistentDirectory3"));
		exceptionRule.expect(RuntimeException.class);
		Ls.exec(args2, abcPath, writer);
		exceptionRule.expectMessage("ls: too many arguments");
	}

//	@Test
//	public void testLsNonexistentDirectory() throws IOException {
//		Application Ls = new Ls();
//		ArrayList<String> args = new ArrayList<>(Arrays.asList("nonexistentDirectory"));
//		exceptionRule.expect(RuntimeException.class);
//		Ls.exec(args, abcPath, writer);
//		exceptionRule.expectMessage("ls: no such directory");
//	}

	@Test
	public void testPwdValidExecution() throws IOException {
		Application pwd = new Pwd();
		ByteArrayOutputStream capture = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(capture);

		pwd.exec(new ArrayList<>(), "", writer);
		writer.flush();
		writer.close();

		String output = capture.toString();
		String expected = Shell.getCurrentDirectory() + sSeperator;
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

		exceptionRule.expect(RuntimeException.class);
		cd.exec(new ArrayList<>(), "", writer);
		exceptionRule.expectMessage("cd: missing argument");
	}

	@Test
	public void testCdTooManyArguments() throws IOException {
		Application cd = new Cd();
		ByteArrayOutputStream capture = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(capture);

		ArrayList<String> args = new ArrayList<>(List.of("dir1", "dir2"));
		exceptionRule.expect(RuntimeException.class);

		cd.exec(args, "", writer);
		exceptionRule.expectMessage("cd: too many arguments");
	}

	@Test
	public void testCdNonexistentDirectory() throws IOException {
		Application cd = new Cd();
		ByteArrayOutputStream capture = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(capture);

		ArrayList<String> args = new ArrayList<>(List.of("nonexistentDirectory"));
		exceptionRule.expect(RuntimeException.class);
		cd.exec(args, "", writer);
		exceptionRule.expectMessage("cd: nonexistentDirectory is not an existing directory");
	}

	@Test
	public void testEchoNoArgsNoInput() throws IOException {
		Application Echo = new Echo();
		Echo.exec(new ArrayList<>(), "", writer);

		String output = capture.toString();
		String expected = sSeperator;
		assertEquals(output, expected);
	}

	@Test
	public void testEchoNoArgsSomeInput() throws IOException {
		Application Echo = new Echo();
		Echo.exec(new ArrayList<String>(), "input here" + sSeperator + " more input here", writer);
    
		String output = capture.toString();
		String expected = sSeperator;

		assertEquals(output, expected);
	}

	@Test
	public void testEchoOneArg() throws IOException {
		Application Echo = new Echo();
		ArrayList<String> args = new ArrayList<>();
		args.add("hello, world");
		Echo.exec(args, "", writer);

		String output = capture.toString();
		String expected = "hello, world " + sSeperator;

		assertEquals(expected, output);
	}

	@Test
	public void testEchoSomeArgs() throws IOException {
		Application Echo = new Echo();
		ArrayList<String> args = new ArrayList<>();
		args.add("Hello,");
		args.add("world!");
		Echo.exec(args, "", writer);

		String output = capture.toString();
		String expected = "Hello, world! " + sSeperator;

		assertEquals(expected, output);
	}

	@Test
	public void testEchoVeryLargeArgs() throws IOException {
		Application Echo = new Echo();
		ArrayList<String> args = new ArrayList<>();
		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			args.add("world!");
			expected.append("world! ");
		}
		args.add("world!");
		expected.append("world! " + sSeperator);
		Echo.exec(args, "", writer);

		String output = capture.toString();
		assertEquals(expected.toString(), output);
	}

	@Test
	public void testCatEmptyFile() throws IOException {
		Application cat = new Cat();
		ArrayList<String> args = new ArrayList<>(Arrays.asList(emptyFileName));
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
		String expected = "This is a single line." + sSeperator;
		assertEquals(output, expected);
	}

	@Test
	public void testCatMultipleLines() throws IOException {
		Application cat = new Cat();
		ArrayList<String> args = new ArrayList<>(Arrays.asList("multipleLines.txt"));
		cat.exec(args, "", writer);

		String output = capture.toString();
		String expected = "Line 1" + sSeperator + "Line 2" + sSeperator + "Line 3" + sSeperator;
		assertEquals(expected, output);
	}

	@Test
	public void testCatMixedContent() throws IOException {
		Application cat = new Cat();
		ArrayList<String> args = new ArrayList<>(Arrays.asList("mixedContent.txt"));
		cat.exec(args, "", writer);

		String output = capture.toString();
		String expected = "This is a line." + sSeperator + "Line 2" + sSeperator + "Another line." + sSeperator;

		assertEquals(expected, output);
	}

	// Current cat implementation needs a to run, this test has no file.
	@Test
	public void testCatLargeFile() throws IOException {
		Application cat = new Cat();
		ArrayList<String> args = new ArrayList<>();
		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			String line = "This is line " + (i + 1) + ".";
			expected.append(line).append(sSeperator);
		}
		cat.exec(args, expected.toString(), writer);
		expected.append(sSeperator);

		String output = capture.toString();
		assertEquals(expected.toString(), output);
	}

	@Test
	public void testCatFileAndStandardInput() throws IOException {
		String standardInput = "This is input from standard input.";

		ArrayList<String> args = new ArrayList<>(Arrays.asList(singleLineFileName));
		// Read content from singleLine.txt and append standard input
		String expectedOutput = "This is a single line." + sSeperator + standardInput + sSeperator;

		Application cat = new Cat();
		cat.exec(args, standardInput, writer);

		String output = capture.toString();
		assertEquals(output, expectedOutput);
	}

	@Test
	public void testCatReadFromMultipleFiles() throws IOException {
		ArrayList<String> args = new ArrayList<>(Arrays.asList("singleLine.txt", "multipleLines.txt"));

		// Read content from singleLine.txt and multipleLines.txt
		String expectedOutput = "This is a single line." + sSeperator + "Line 1" + sSeperator + "Line 2" + sSeperator
				+ "Line 3" + sSeperator;

		Application cat = new Cat();
		cat.exec(args, "", writer);

		String output = capture.toString();
		assertEquals(output, expectedOutput);
	}

	@Test
	public void testCatReadFromStandardInput() throws IOException {
		String standardInput = "This is input from standard input.";
		ArrayList<String> args = new ArrayList<>();
		String expectedOutput = standardInput + sSeperator;

		Application cat = new Cat();
		cat.exec(args, standardInput, writer);

		String output = capture.toString();
		assertEquals(output, expectedOutput);
	}

	@Test
	public void testCatReadFromNonexistentFile() throws IOException {
		// Create arguments for Cat with a nonexistent file
		ArrayList<String> args = new ArrayList<>(Arrays.asList("nonexistentFile.txt"));

		exceptionRule.expect(RuntimeException.class);
		Application cat = new Cat();
		cat.exec(args, "", writer);
		exceptionRule.expectMessage("cat: file does not exist");

	}

	@Test
	public void testCatMissingArguments() throws IOException {
		// Create arguments for Cat with a nonexistent file
		ArrayList<String> args = new ArrayList<>();

		exceptionRule.expect(RuntimeException.class);
		Application cat = new Cat();
		cat.exec(args, "", writer);
		exceptionRule.expectMessage("cat: missing arguments / empty stdin");

	}

	// Head
	@Test
	public void testHeadReadFromNonexistentFile() throws IOException {
		ArrayList<String> args = new ArrayList<>();

		exceptionRule.expect(RuntimeException.class);
		Application head = new Head();
		head.exec(args, "", writer);
		exceptionRule.expectMessage("head: missing arguments");

	}

	@Test
	public void testHeadReadFromInput() throws IOException {
		Application head = new Head();
		ArrayList<String> args = new ArrayList<>();
		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			String line = "This is line " + (i + 1) + ".";
			if (i < 10) {
				expected.append(line).append(sSeperator);
			}
		}
		head.exec(args, expected.toString(), writer);

		String output = capture.toString();
		assertEquals(expected.toString(), output);
	}

	@Test
	public void testHeadReadFromFile() throws IOException {
		Application head = new Head();
		ArrayList<String> args = new ArrayList<>(Arrays.asList(singleLineFileName));
		StringBuilder expected = new StringBuilder();
		head.exec(args, "", writer);

		String output = capture.toString();
		assertEquals(singleLineFileContent + sSeperator, output);
	}

	@Test
	public void testHeadInvalidOperation() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(singleLineFileName, "-n"));

		exceptionRule.expect(RuntimeException.class);
		Application head = new Head();
		head.exec(args, "", writer);
		exceptionRule.expectMessage("head: invalid option");

	}

	@Test
	public void testHeadInvalidFile() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "1", "invalid"));
		String expected = read(multipleLines, 3, true, -1);

		exceptionRule.expect(RuntimeException.class);
		Application head = new Head();
		head.exec(args, "", writer);
		exceptionRule.expectMessage("head: file not found: invalid");

	}

	@Test
	public void testHeadInvalidArg() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "s"));

		exceptionRule.expect(RuntimeException.class);
		Application head = new Head();
		head.exec(args, "", writer);
		exceptionRule.expectMessage("head: second arg is not an integer");

	}

	@Test
	public void testHeadLineNumberOperation() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "2"));
		String input = read(multipleLines, 0, false, -1);
		Application head = new Head();
		head.exec(args, input, writer);
		String output = capture.toString();
		String expected = read(multipleLines, 2, true, -1);
		assertEquals(expected, output);
	}

	@Test
	public void testHeadInvalidArgThree() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-m", "2", ""));

		exceptionRule.expect(RuntimeException.class);
		Application head = new Head();
		head.exec(args, "", writer);
		exceptionRule.expectMessage("head: invalid option");

	}

	@Test
	public void testHeadValidArgThree() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "2", multipleLinesFileName));
		Application head = new Head();
		head.exec(args, "", writer);
		String output = capture.toString();
		String expected = read(multipleLines, 2, true, -1);
		assertEquals(expected, output);
	}

	@Test
	public void testHeadInvalidArgThreeNumber() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "s", ""));

		exceptionRule.expect(RuntimeException.class);
		Application head = new Head();
		head.exec(args, "", writer);
		exceptionRule.expectMessage("head: second arg is not an integer");

	}

	@Test
	public void testHeadValidArgThreePlus() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "2", "a", "b"));
		exceptionRule.expect(RuntimeException.class);
		Application head = new Head();
		head.exec(args, "", writer);
		exceptionRule.expectMessage("head: invalid number of arguments");

	}

	// Tail
	@Test
	public void testTailReadFromNonexistentFile() throws IOException {
		ArrayList<String> args = new ArrayList<>();

		exceptionRule.expect(RuntimeException.class);
		Application tail = new Tail();
		tail.exec(args, "", writer);
		exceptionRule.expectMessage("tail: missing arguments");

	}

	@Test
	public void testTailReadFromInput() throws IOException {
		Application tail = new Tail();
		ArrayList<String> args = new ArrayList<>();
		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			String line = "This is line " + (i + 1) + ".";
			if (i < 10) {
				expected.append(line).append(sSeperator);
			}
		}
		tail.exec(args, expected.toString(), writer);

		String output = capture.toString();
		assertEquals(expected.toString(), output);
	}

	@Test
	public void testTailReadFromFile() throws IOException {
		Application tail = new Tail();
		ArrayList<String> args = new ArrayList<>(Arrays.asList(singleLineFileName));
		StringBuilder expected = new StringBuilder();
		tail.exec(args, "", writer);

		String output = capture.toString();
		assertEquals(singleLineFileContent + sSeperator, output);
	}

	@Test
	public void testTailInvalidOperation() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(singleLineFileName, "-n"));

		exceptionRule.expect(RuntimeException.class);
		Application tail = new Tail();
		tail.exec(args, "", writer);
		exceptionRule.expectMessage("tail: invalid option");

	}

	@Test
	public void testTailInvalidArg() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "s"));

		exceptionRule.expect(RuntimeException.class);
		Application tail = new Tail();
		tail.exec(args, "", writer);
		exceptionRule.expectMessage("tail: second arg is not an integer");

	}

	@Test
	public void testTailLineNumberOperation() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "2"));
		String input = read(multipleLines, 0, false, -1);
		Application tail = new Tail();
		tail.exec(args, input, writer);
		String output = capture.toString();
		String expected = read(multipleLines, 3, true, 1);
		assertEquals(expected, output);
	}

	@Test
	public void testTailInvalidArgThree() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-m", "2", ""));

		exceptionRule.expect(RuntimeException.class);
		Application tail = new Tail();
		tail.exec(args, "", writer);
		exceptionRule.expectMessage("tail: invalid option");

	}

	@Test
	public void testTailInvalidFileNameArgThree() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "2", "demo.txt"));

		exceptionRule.expect(RuntimeException.class);
		Application tail = new Tail();
		tail.exec(args, "", writer);
		exceptionRule.expectMessage("tail: file not found: demo.txt");

	}

	@Test
	public void testTailValidArgThree() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "2", multipleLinesFileName));
		Application tail = new Tail();
		tail.exec(args, "", writer);
		String output = capture.toString();
		String expected = read(multipleLines, 3, true, 1);
		assertEquals(expected, output);
	}

	@Test
	public void testTailInvalidArgThreeNumber() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "s", ""));

		exceptionRule.expect(RuntimeException.class);
		Application tail = new Tail();
		tail.exec(args, "", writer);
		exceptionRule.expectMessage("tail: second arg is not an integer");

	}

	@Test
	public void testTailValidArgThreePlus() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "2", "a", "b"));
		exceptionRule.expect(RuntimeException.class);
		Application tail = new Tail();
		tail.exec(args, "", writer);
		exceptionRule.expectMessage("tail: invalid number of arguments");

	}

	// Grep

	@Test
	public void testGrepInValidArgZero() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList());
		exceptionRule.expect(RuntimeException.class);
		Application grep = new Grep();
		grep.exec(args, "", writer);
		exceptionRule.expectMessage("grep: wrong number of arguments");

	}

	@Test
	public void testGrepInValidArgEmptyInput() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("demo"));
		exceptionRule.expect(RuntimeException.class);
		Application grep = new Grep();
		grep.exec(args, "", writer);
		exceptionRule.expectMessage("grep: empty stdin");

	}

	@Test
	public void testGrepValidArgReadFile() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("Line \\d.*"));
		String input = read(multipleLines, 0, false, -1);
		Application grep = new Grep();
		grep.exec(args, input, writer);
		String output = capture.toString();
		String expected = read(multipleLines, 1, false, -1) + sSeperator;
		assertEquals(expected, output);
	}

	@Test
	public void testGrepInValidArgReadWithFileName() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("Line \\d.*", "invalidfile.txt"));
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));
		Application grep = new Grep();
		grep.exec(args, "", writer);
		assertEquals("grep: file not found: invalidfile.txt" + sSeperator, outContent.toString());
	}
/*	@Test
	public void testGrepInValidArgReadWithFileAccess() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("Line \\d.*", multipleLinesFileName));
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		new File(multipleLines).setReadable(false);
		System.setOut(new PrintStream(outContent));
		Application grep = new Grep();
		grep.exec(args, "", writer);
		assertEquals(
				"grep: access not permitted to file " + multipleLinesFileName + System.getProperty("line.separator"),
				outContent.toString());
	}*/

	// System.out.println("grep: is a directory: " + this.filename);
	// should be moved to top, above file check to validate this flow
	// as if it is a directory but not a file then it will always return file not
	// found
//	@Test
//	public void testGrepInValidArgReadWithFileAccess() throws Exception {
//		ArrayList<String> args = new ArrayList<String>(Arrays.asList("Line \\d.*", multipleLinesFileName));
//		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
//		Path filePath1 = Paths.get(Shell.getCurrentDirectory() + File.separator + multipleLinesFileName);
//
//		if (isWindows()) {
//			Path filePath = Paths.get(Shell.getCurrentDirectory() + File.separator + multipleLinesFileName);
//			Files.setAttribute(filePath, "dos:readonly", false, LinkOption.NOFOLLOW_LINKS);
//		} else {
//			new File(multipleLines).setReadable(false);
//		}
//		System.setOut(new PrintStream(outContent));
//		Application grep = new Grep();
//		grep.exec(args, "", writer);
//		assertEquals("grep: access not permitted to file " + multipleLinesFileName + sSeperator, outContent.toString());
//	}

	@Test
	public void testGrepValidArgReadWithDirectory() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("Line \\d.*", multipleLinesFileName));
		Application grep = new Grep();
		grep.exec(args, "", writer);
		String output = capture.toString();
		String expected = read(multipleLines, 1, false, -1) + sSeperator;
		assertEquals(expected, output);
	}

	@Test
	public void testGrepValidArgReadWithFileName() throws IOException {
		ArrayList<String> args = new ArrayList<String>(
				Arrays.asList("Line \\d.*", multipleLinesFileName, singleLineFileName));
		Application grep = new Grep();
		grep.exec(args, "", writer);
		String output = capture.toString();
		String expected = "multipleLines.txt: Line 1" + sSeperator + "multipleLines.txt: Line 2" + sSeperator
				+ "multipleLines.txt: Line 3" + sSeperator + "";
		assertEquals(expected, output);
	}

	@Test
	public void testGrepInValidArgPattern() throws IOException {
		ArrayList<String> args = new ArrayList<String>(
				Arrays.asList("Lin^(*^%%$%$#%#^%3{]]}e \\d.*", multipleLinesFileName, singleLineFileName));
		exceptionRule.expect(RuntimeException.class);
		Application grep = new Grep();
		grep.exec(args, "", writer);
		exceptionRule.expectMessage("grep: invalid regular expression");

	}

	// Cut
	@Test
	public void testCutInvalidArgsOne() throws IOException {
		ArrayList<String> args = new ArrayList<>(Arrays.asList(""));

		exceptionRule.expect(RuntimeException.class);
		Application cut = new Cut();
		cut.exec(args, "", writer);
		exceptionRule.expectMessage("cut: wrong number of arguments");

	}

	@Test
	public void testCutInvalidArgsFour() throws IOException {
		ArrayList<String> args = new ArrayList<>(Arrays.asList("", "", "", ""));

		exceptionRule.expect(RuntimeException.class);
		Application cut = new Cut();
		cut.exec(args, "", writer);
		exceptionRule.expectMessage("cut: wrong number of arguments");

	}

	@Test
	public void testCutValidArgFile() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "1-4,5-9", multipleLines));
		Application cut = new Cut();
		cut.exec(args, "", writer);
		String output = capture.toString();
		String expected = "Line" + sSeperator + "Line" + sSeperator + "Line" + sSeperator + " 1" + sSeperator + " 2"
				+ sSeperator + " 3" + sSeperator;
		assertEquals(expected, output);
	}

/*	@Test
	public void testCutINValidArgFile() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "1-4,5-9", subDirectoryPath));
		exceptionRule.expect(RuntimeException.class);
		Application cut = new Cut();
		cut.exec(args, "", writer);
		exceptionRule.expectMessage("cut: cannot read testDirectory/multipleLines.txt");

	}*/

/*	@Test
	public void testCutValidArgInput() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "1-4"));
		String input = read(multipleLines, 0, false, -1);
		Application cut = new Cut();
		cut.exec(args, input, writer);
		String output = capture.toString();
		String expected = "Line" + sSeperator;
		assertEquals(expected, output);
	}

	@Test
	public void testCutValidArgNoEndBoundInput() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "2"));
		String input = read(multipleLines, 0, false, -1);
		Application cut = new Cut();
		cut.exec(args, input, writer);
		String output = capture.toString();
		String expected = input.substring(1) + sSeperator;
		assertEquals(expected, output);
	}

	@Test
	public void testCutValidArgNoStartBoundInput() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-n", "-3"));
		String input = read(multipleLines, 0, false, -1);
		Application cut = new Cut();
		cut.exec(args, input, writer);
		String output = capture.toString();
		String expected = input.substring(0, 3) + sSeperator;
		assertEquals(expected, output);
	}*/

	// Find
	@Test
	public void testFindInvalidArg() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(""));

		exceptionRule.expect(RuntimeException.class);
		Application find = new Find();
		find.exec(args, "", writer);
		exceptionRule.expectMessage("find: wrong number of arguments");

	}

	@Test
	public void testFindValidArgThree() throws IOException {
		ArrayList<String> args = new ArrayList<String>(
				Arrays.asList("", ".*", Shell.getCurrentDirectory() + "/subDirectory"));
		Application find = new Find();
		find.exec(args, "", writer);
		String output = capture.toString();
		assertEquals("subDirectory" + System.getProperty("file.separator") + "subemptyFile.txt" + sSeperator, output);
	}

	@Test
	public void testFindValidNoPathArgThree() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("", ".*"));
		Application find = new Find();
		find.exec(args, "", writer);
		String output = capture.toString();

		String files[] = output.split("\\s+");
		String expected = "subDirectory" + System.getProperty("file.separator") + "subemptyFile.txt" + sSeperator
				+ "abc.txt" + sSeperator + "mixedContent.txt" + sSeperator + "multipleLines.txt" + sSeperator
				+ "emptyFile.txt" + sSeperator + "singleLine.txt" + sSeperator + "testGrep.txt" + sSeperator;
		for (String string : files) {
			assertTrue(expected.contains(string));
		}
	}

	@Test
	public void testExecWithTooManyArguments() throws IOException {
		Uniq uniq = new Uniq();
		ArrayList<String> args = new ArrayList<>();
		args.add("-i");
		args.add("file.txt");
		args.add("extraArg");

		exceptionRule.expect(RuntimeException.class);
		uniq.exec(args, "", writer);
		exceptionRule.expectMessage("uniq: too many arguments");

	}

	@Test
	public void testUniqInValidArgFileName() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-i", singleLineFileName));
		Application uniq = new Uniq();
		exceptionRule.expect(RuntimeException.class);
		uniq.exec(args, "", writer);

		exceptionRule.expectMessage("uniq: bad filename");

	}

	@Test
	public void testUniqValidArgThree() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(multipleLines));
		Application uniq = new Uniq();
		uniq.exec(args, "", writer);
		String output = capture.toString();
		String expected = read(multipleLines, 3, true, -1);
		assertEquals(expected, output);
	}

	@Test
	public void testUniqValidArgInput() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-i"));
		String input = read(multipleLines, 3, true, -1);
		Application uniq = new Uniq();
		uniq.exec(args, input, writer);
		String output = capture.toString();
		String expected = read(multipleLines, 3, true, -1);
		assertEquals(expected.toLowerCase(), output);
	}

/*
	@Test
	public void testSortReverse() throws IOException {
		Application sort = new Sort();
		ArrayList<String> args = new ArrayList<>(Arrays.asList("-r", multipleLines));
		StringBuilder expected = new StringBuilder();
		sort.exec(args, "", writer);

		String output = capture.toString();
		assertEquals("Line 3" + sSeperator + "Line 2" + sSeperator + "Line 1" + sSeperator, output);
	}
*/

	@Test
	public void testSortReverseInput() throws IOException {
		Application sort = new Sort();
		String input = read(multipleLines, 3, true, -1);
		ArrayList<String> args = new ArrayList<>();
		sort.exec(args, input, writer);

		String output = capture.toString();
		String expected = "Line 1" + sSeperator + "Line 2" + sSeperator + "Line 3" + sSeperator;
		assertEquals(expected, output);
	}

	@Test
	public void testSortManyArgs() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("arg", "arg", "arg"));
		Application sort = new Sort();
		exceptionRule.expect(SortException.class);
		sort.exec(args, "", writer);
		exceptionRule.expectMessage("too many arguments");
	}

	@Test
	public void testSortInvalidArgs() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("invalid Arg", "arg"));
		Application sort = new Sort();
		exceptionRule.expect(SortException.class);
		sort.exec(args, "", writer);
		exceptionRule.expectMessage("too many arguments");
	}

	@Test
	public void testSortInValidArgFile() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("-r"));
		Application sort = new Sort();
		exceptionRule.expect(RuntimeException.class);
		sort.exec(args, "", writer);
		exceptionRule.expectMessage("sort: wrong file argument");
	}
	@Test
	public void testMkdirNoArgs() throws IOException {
		Application mkdir = new Mkdir();
		exceptionRule.expect(RuntimeException.class);
		mkdir.exec(new ArrayList<>(),"",writer);
		exceptionRule.expectMessage("mkdir: missing argument(s)");
	}
	@Test
	public void testMkdirSameArg() throws IOException {
		Application mkdir = new Mkdir();
		ArrayList<String> args = new ArrayList<>();
		args.add("newDir");
		args.add("newDir");
		exceptionRule.expect(RuntimeException.class);
		mkdir.exec(args,"",writer);
		exceptionRule.expectMessage("mkdir: " + "newDir" + "already exists");
	}
	// not sure if this is correct expected
	@Test
	public void testMkdirOneArg() throws IOException {
		Application mkdir = new Mkdir();
		ArrayList<String> args = new ArrayList<>();
		args.add("newDir");
		mkdir.exec(args,"",writer);
		String output = capture.toString().trim();
		String expected = "";

		Path newDir = Paths.get("newDir");
		assertTrue(Files.exists(newDir));
		assertTrue(Files.isDirectory(newDir));
		assertEquals(expected, output);
	}
	@Test
	public void testMkdirNameNotAllowed() throws IOException {
		Application mkdir = new Mkdir();
		ArrayList<String> args = new ArrayList<>();
		args.add("/\0");
		exceptionRule.expect(RuntimeException.class);
		mkdir.exec(args,"",writer);
		exceptionRule.expectMessage("mkdir: could not create " + "//\\**C:\\||");
	}
	@Test
	public void testMkdirThreeArg() throws IOException {
		Application mkdir = new Mkdir();
		ArrayList<String> args = new ArrayList<>();
		args.add("newDir1");
		args.add("newDir2");
		args.add("newDir3");
		mkdir.exec(args,"",writer);
		String output = capture.toString().trim();
		String expected = "";
		Path newDir1 = Paths.get("newDir1");
		assertTrue(Files.exists(newDir1));
		assertTrue(Files.isDirectory(newDir1));
		Path newDir2 = Paths.get("newDir2");
		assertTrue(Files.exists(newDir2));
		assertTrue(Files.isDirectory(newDir2));
		Path newDir3 = Paths.get("newDir3");
		assertTrue(Files.exists(newDir3));
		assertTrue(Files.isDirectory(newDir3));
		assertEquals(expected, output);
  }
  
	@Test
	public void testSortInValidArgFileDir() throws IOException {
		ArrayList<String> args = new ArrayList<String>(Arrays.asList(subDirectoryPath));
		Application sort = new Sort();
		exceptionRule.expect(RuntimeException.class);
		sort.exec(args, "", writer);
		exceptionRule.expectMessage("sort: wrong file argument");
	}

	private boolean isWindows() {
		String OS = System.getProperty("os.name");
		return OS.startsWith("Windows");
	}

	private String read(String file, int limit, boolean useLimit, int skip) {
		BufferedReader reader;
		String currentLine = "";
		try {
			reader = new BufferedReader(new FileReader(file));
			if (useLimit) {
				while (limit-- > 0) {
					if (skip-- > 0) {
						reader.readLine();
					} else {
						currentLine += reader.readLine() + sSeperator;
					}
				}
			} else {
				currentLine = reader.lines().collect(Collectors.joining(sSeperator));
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return currentLine;
	}
}
