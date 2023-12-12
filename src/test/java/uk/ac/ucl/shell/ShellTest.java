package uk.ac.ucl.shell;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class ShellTest {

    static String sSeperator = System.getProperty("line.separator");
    ByteArrayOutputStream captErr;
    ByteArrayOutputStream captOut;
    PrintStream oldOut;
    PrintStream oldErr;

    @Before
    public void setUp() {
        oldOut = System.out;
        oldErr = System.err;

        captErr = new ByteArrayOutputStream();
        captOut = new ByteArrayOutputStream();

        System.setOut(new PrintStream(captOut));
        System.setErr(new PrintStream(captErr));
    }

    @After
    public void tearDown() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }

    @Test
    public void testWrongNoOfArgs() throws IOException {
        String[] args = {""};

        Shell.main(args);
        String expected = "COMP0010 shell: wrong number of arguments" + sSeperator;
        String output = captOut.toString();

        assertEquals(expected, output);
    }

    @Test
    public void testTwoArgsFirstIsInvalid() throws IOException {
        String[] args = {"invalid", ""};

        Shell.main(args);
        String expected = "COMP0010 shell: invalid: unexpected argument" + sSeperator;
        String output = captOut.toString();

        assertEquals(expected, output);
    }

    @Test
    public void testCorrectArgs() throws IOException {
        String[] args = {"-c", "ls"};

        Shell.main(args);
        String expected = "";
        String output = captErr.toString();

        assertEquals(expected, output);
    }

    @Test
    public void testCorrectArgsExceptionThrownInEval() {
        String[] args = {"-c", "invalidApp"};

        Shell.main(args);
        String expected = "COMP0010 shell: invalidApp: unknown application" + sSeperator;
        String output = captErr.toString();

        assertEquals(expected, output);
    }

}
