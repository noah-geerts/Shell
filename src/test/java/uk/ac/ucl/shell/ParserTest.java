package uk.ac.ucl.shell;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Test;

public class ParserTest {
	@Test
	public void testShellEval() throws IOException {
		ByteArrayOutputStream capture = new ByteArrayOutputStream();
		PrintStream newOut = new PrintStream(capture);
		PrintStream oldOut = System.out;
		System.setOut(newOut);

		Shell.eval("echo word1 ; echo word2 | cat | grep w; echo word3");

		System.setOut(oldOut);
		String output = capture.toString();
		String expected = "word1 " + System.getProperty("line.separator")
				+ "word2 " + System.getProperty("line.separator")
				+ "word3 " + System.getProperty("line.separator");
		assertTrue(output.equals(expected));
	}
}
