package uk.ac.ucl.shell;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}
