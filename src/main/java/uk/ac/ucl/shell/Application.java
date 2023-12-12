package uk.ac.ucl.shell;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The `Application` interface defines the shared functionality needed to execute shell applications.
 * Each shell application supported by this shell program will be a class implementing `Application`.
 * All shell applications are present in this file as default visibility classes.
 */
public interface Application {
    /**
     * Executes a command
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException;
}

class Cd implements Application {
    /**
     * Executes cd command
     * Does some argument input checking
     * Takes in the directory name, changes current directory to the new name
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.isEmpty()) {
            throw new CdException("missing argument");
        } else if (appArgs.size() > 1) {
            throw new CdException("too many arguments");
        }
        String dirString = appArgs.get(0);
        File dir = new File(Shell.getCurrentDirectory(), dirString);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new CdException(dirString + " is not an existing directory");
        }
        Shell.setCurrentDirectory(dir.getCanonicalPath());
    }

}

class Pwd implements Application {
    /**
     * Executes pwd command
     * Writes the name of the current directory to stdout
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        writer.write(Shell.getCurrentDirectory());
        writer.write(System.getProperty("line.separator"));
        writer.flush();
    }
}


class Ls implements Application {
    /**
     * Executes ls command
     * Checks the number of arguments for validity
     * If a directory name is given, changes current directory to the new name
     * Writes the list of files in the current directory to stdout
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        File currDir;
        if (appArgs.isEmpty()) {
            currDir = new File(Shell.getCurrentDirectory());
        } else if (appArgs.size() == 1) {
            currDir = new File(Shell.getCurrentDirectory() + System.getProperty("file.separator") + appArgs.get(0));
        } else {
            throw new LsException("too many arguments");
        }
        try {
            File[] listOfFiles = currDir.listFiles();
            boolean atLeastOnePrinted = false;
            for (File file : listOfFiles) {
                if (!file.getName().startsWith(".")) {
                    writer.write(file.getName());
                    writer.write("\t");
                    writer.flush();
                    atLeastOnePrinted = true;
                }
            }
            if (atLeastOnePrinted) {
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }
        } catch (NullPointerException e) {
            throw new LsException("directory " + currDir.getName() + " does not exist.");
        }
    }
}

class Cat implements Application {
    /**
     * Executes cat command
     * Does input checking
     * Arguments are a list of filenames
     * Iterates through that list reading the files
     * Then writes their contents to stdout
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.isEmpty() && input.isEmpty()) {
            throw new CatException("missing arguments / empty stdin");
        } else {
            for (String arg : appArgs) {
                Charset encoding = StandardCharsets.UTF_8;
                File currFile = new File(Shell.getCurrentDirectory() + File.separator + arg);
                if (currFile.exists()) {
                    Path filePath = Paths.get(Shell.getCurrentDirectory() + File.separator + arg);
                    try (BufferedReader reader = Files.newBufferedReader(filePath, encoding)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.write(System.getProperty("line.separator"));
                            writer.flush();
                        }
                    } catch (IOException e) {
                        throw new CatException("cannot open " + arg);
                    }
                } else {
                    throw new CatException("file does not exist");
                }
            }
            if (!input.isEmpty()) {
                writer.write(input);
                writer.write(System.getProperty("line.separator"));
                writer.flush();
                writer.close();
            }
        }
    }
}

class Echo implements Application {
    /**
     * Executes echo command
     * Writes the arguments to stdout separated by spaces (" ")
     * Then writes a "\n"
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        for (String arg : appArgs) {
            writer.write(arg);
            writer.write(" ");
            writer.flush();
        }
        // echo with no arguments should still print a newline
        writer.write(System.getProperty("line.separator"));
        writer.flush();
    }

}

class Head implements Application {
    /**
     * default number of lines to write
     */
    private int lineNumber = 10;

    /**
     * Executes head command
     * Checks for bad input (wrong number of args), throws a HeadException
     * checks for stdin or filenames used
     * checks for options used, and changes the line number if so
     * calls the relevant reader method
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.isEmpty() && input.isEmpty()) {
            throw new HeadException("missing arguments");
        }
        if (appArgs.isEmpty()) {
            readFromStdin(input, writer);
        } else if (appArgs.size() == 1) {
            String fileName = appArgs.get(0);
            readFromFile(fileName, writer);
        } else if (appArgs.size() == 2) {
            String option = appArgs.get(0);
            if (!option.equals("-n")) {
                throw new HeadException("invalid option");
            } else {
                try {
                    this.lineNumber = Integer.parseInt(appArgs.get(1));
                    readFromStdin(input, writer);
                } catch (NumberFormatException e) {
                    throw new HeadException("second arg is not an integer");
                }

            }
        } else if (appArgs.size() == 3) {
            String option = appArgs.get(0);
            if (!option.equals("-n")) {
                throw new HeadException("invalid option");
            } else {
                try {
                    this.lineNumber = Integer.parseInt(appArgs.get(1));
                    String fileName = appArgs.get(2);
                    readFromFile(fileName, writer);
                } catch (NumberFormatException e) {
                    throw new HeadException("second arg is not an integer");
                }
            }
        } else {
            throw new HeadException("invalid number of arguments");
        }
    }

    /**
     * Reads from stdin, wraps the string in a reader
     * passes to the writer method
     *
     * @param input  string for Application
     * @param writer that Application output is written to
     * @throws IOException if reader throws an error
     */
    private void readFromStdin(String input, OutputStreamWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(input))) {
            writeLines(reader, writer);
        }
    }

    /**
     * Checks the file exists
     * if true: wraps the file in a reader and passes to the writer method
     * if false: throws an HeadException
     *
     * @param fileName name of the file we are reading from
     * @param writer   that Application output is written to
     * @throws IOException if reader throws an error
     */
    private void readFromFile(String fileName, OutputStreamWriter writer) throws IOException {
        Path filePath = Paths.get(Shell.getCurrentDirectory() + File.separator + fileName);
        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                writeLines(reader, writer);
            }
        } else {
            throw new HeadException("file not found: " + fileName);
        }
    }

    /**
     * Writes the first lineNumber of lines from the reader to stdout
     *
     * @param reader reader of info to write
     * @param writer that Application output is written to
     * @throws IOException if writer throws an error
     */
    private void writeLines(BufferedReader reader, OutputStreamWriter writer) throws IOException {
        String line;
        int counter = 0;
        while ((line = reader.readLine()) != null && counter < this.lineNumber) {
            writer.write(line);
            writer.write(System.getProperty("line.separator"));
            writer.flush();
            counter++;
        }
    }
}

class Tail implements Application {
    /**
     * default number of lines to write
     */
    private int lineNumber = 10;

    /**
     * Executes tail command
     * Checks for bad input (wrong number of args), throws a TailException
     * checks for stdin or filenames used
     * checks for options used, and changes the line number if so
     * calls the relevant reader method
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        // default number of lines is 10
        if (appArgs.isEmpty() && input.isEmpty()) {
            throw new TailException("missing arguments");
        }
        if (appArgs.isEmpty()) {
            readFromStdin(input, writer);
        } else if (appArgs.size() == 1) {
            String fileName = appArgs.get(0);
            readFromFile(fileName, writer);
        } else if (appArgs.size() == 2) {
            String option = appArgs.get(0);
            if (!option.equals("-n")) {
                throw new TailException("invalid option");
            } else {
                try {
                    this.lineNumber = Integer.parseInt(appArgs.get(1));
                    readFromStdin(input, writer);
                } catch (NumberFormatException e) {
                    throw new TailException("second arg is not an integer");
                }

            }
        } else if (appArgs.size() == 3) {
            String option = appArgs.get(0);
            if (!option.equals("-n")) {
                throw new TailException("invalid option");
            } else {
                try {
                    this.lineNumber = Integer.parseInt(appArgs.get(1));
                    String fileName = appArgs.get(2);
                    readFromFile(fileName, writer);
                } catch (NumberFormatException e) {
                    throw new TailException("second arg is not an integer");
                }
            }
        } else {
            throw new TailException("invalid number of arguments");
        }
    }

    /**
     * Reads from stdin, wraps the string in a reader
     * passes to the writer method
     *
     * @param input  string for Application
     * @param writer that Application output is written to
     * @throws IOException if reader throws an error
     */
    private void readFromStdin(String input, OutputStreamWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(input))) {
            writeLines(reader, writer);
        }
    }

    /**
     * Checks the file exists
     * if true: wraps the file in a reader and passes to the writer method
     * if false: throws an TailException
     *
     * @param fileName name of the file we are reading from
     * @param writer   that Application output is written to
     * @throws IOException if reader throws an error
     */
    private void readFromFile(String fileName, OutputStreamWriter writer) throws IOException {
        Path filePath = Paths.get(Shell.getCurrentDirectory() + File.separator + fileName);
        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                writeLines(reader, writer);
            }
        } else {
            throw new TailException("file not found: " + fileName);
        }
    }

    /**
     * Stores all the lines from the reader
     * Writes the lines from index = (length - lineNumber) to stdout
     *
     * @param reader reader of info to write
     * @param writer that Application output is written to
     * @throws IOException if reader or writer throw an error
     */
    private void writeLines(BufferedReader reader, OutputStreamWriter writer) throws IOException {
        String line;
        ArrayList<String> storage = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            storage.add(line);
        }
        int index;
        if (this.lineNumber > storage.size()) {
            index = 0;
        } else {
            index = storage.size() - this.lineNumber;
        }
        for (int i = index; i < storage.size(); i++) {
            writer.write(storage.get(i));
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }
    }
}

class Grep implements Application {
    /**
     * storing the pattern, filename and whether the filename should be printed
     */
    private Pattern grepPattern;
    private String filename = "";
    private boolean printFilename = false;

    /**
     * Executes grep command
     * Handles checking error cases
     * Sets grepPattern var and throws an exception if not a valid pattern
     * Reads from stdin or from file when appropriate
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.size() == 0) {
            throw new GrepException("wrong number of arguments");
        }
        try {
            this.grepPattern = Pattern.compile(appArgs.get(0));
            int numOfFiles = appArgs.size() - 1;
            if (numOfFiles == 0) {
                if (input.isEmpty()) {
                    throw new GrepException("empty stdin");
                } else {
                    readFromStdin(input, writer);
                }
            } else if (numOfFiles == 1) {
                this.filename = appArgs.get(1);
                readFromFile(writer);
            } else if (numOfFiles > 1) {
                int i = 1;
                this.printFilename = true;
                while (i <= numOfFiles) {
                    this.filename = appArgs.get(i);
                    readFromFile(writer);
                    i += 1;
                }
            }
        } catch (PatternSyntaxException e) {
            throw new GrepException("invalid regular expression");
        }
    }

    /**
     * Reads from stdin, wraps the string in a reader
     * passes to the writer method
     *
     * @param input  string for Application
     * @param writer that Application output is written to
     * @throws IOException if reader throws an error
     */
    private void readFromStdin(String input, OutputStreamWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(input))) {
            writeLines(reader, writer);
        }
    }

    /**
     * Checks the file exists, gets fileName from private var
     * if true: wraps the file in a reader and passes to the writer method
     * if false: throws an GrepException
     *
     * @param writer that Application output is written to
     * @throws IOException if writer throws an error
     */
    private void readFromFile(OutputStreamWriter writer) throws IOException {
        Path filePath = Paths.get(Shell.getCurrentDirectory() + File.separator + this.filename);
        if (!Files.exists(filePath)) {
            System.out.println("grep: file not found: " + this.filename);
        } else if (!Files.isReadable(filePath)) {
            System.out.println("grep: access not permitted to file " + this.filename);
        } else if (!Files.isDirectory(filePath)) {
            BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
            writeLines(reader, writer);
        } else {
            System.out.println("grep: is a directory: " + this.filename);
        }
    }

    /**
     * Goes line by line from the reader checking if it matches the pattern
     * if true: writes to stdout
     * if false: does not write to it
     *
     * @param reader reader of info to write
     * @param writer that Application output is written to
     * @throws IOException if reader or writer throws an error
     */
    private void writeLines(BufferedReader reader, OutputStreamWriter writer) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = this.grepPattern.matcher(line);
            if (matcher.find()) {
                if (this.printFilename) {
                    writer.write(this.filename);
                    writer.write(": ");
                }
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }
        }
    }
}

class Cut implements Application {
    /**
     * Executes cut command
     * Checks for wrong argument size, can only be 2 or 3
     * Checks if there is a file, if so stores the filename
     * Splits up the ranges specified and processes them
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.size() < 2 || appArgs.size() > 3) {
            throw new CutException("wrong number of arguments");
        }

        String option = appArgs.get(1);
        String fileName = appArgs.size() > 2 ? appArgs.get(2) : null;
        String[] ranges = option.split(",");

        for (String range : ranges) {
            processRange(range, fileName, input, writer);
        }
    }

    /**
     * Splits up the range by "-"
     * If it is an open range (n-), end value is Integer.MAX_VALUE
     * Else (n-m), end is m
     *
     * @param range    the range of bytes to extract
     * @param fileName the filename, can be null so stdin
     * @param input    string for Application
     * @param writer   that Application output is written to
     * @throws IOException if processLine throws an error
     */
    private void processRange(String range, String fileName, String input, OutputStreamWriter writer) throws IOException {
        String[] bounds = range.split("-");
        int start = parseBound(bounds[0]);
        int end = bounds.length > 1 ? parseBound(bounds[1]) : Integer.MAX_VALUE;

        if (fileName == null) {
            processLine(input, start, end, writer);
        } else {
            processFile(fileName, start, end, writer);
        }
    }

    /**
     * Helper method for processRange
     *
     * @param bound either "" or "int"
     * @return returns 1 if it is empty, else parses the "int" to int
     */
    private int parseBound(String bound) {
        return bound.isEmpty() ? 1 : Integer.parseInt(bound);
    }

    /**
     * Gets the file and reads it in, calling the processLine method on each line
     *
     * @param fileName filename
     * @param start    start of range of bytes included
     * @param end      end of range of bytes included
     * @param writer   that Application output is written to
     * @throws IOException if reader throws an error
     */
    private void processFile(String fileName, int start, int end, OutputStreamWriter writer) throws IOException {
        Path filePath = Paths.get(fileName);

        if (!Files.isReadable(filePath) || Files.isDirectory(filePath)) {
            throw new CutException("cannot read " + fileName);
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, start, end, writer);
            }
        }
    }

    /**
     * Checks startIndex and endIndex are lower than lineLength
     * Makes a substring from the range
     * Writes the substring
     *
     * @param line   either a line from the file or the shell stdin
     * @param start  start of range of bytes to include
     * @param end    end of range of bytes to include
     * @param writer that Application output is written to
     * @throws IOException if writer throws an error
     */
    private void processLine(String line, int start, int end, OutputStreamWriter writer) throws IOException {
        int lineLength = line.length();
        int startIndex = Math.min(start, lineLength);
        int endIndex = Math.min(end, lineLength);

        if (startIndex <= endIndex) {
            String substring = line.substring(startIndex - 1, endIndex);
            writer.write(substring);
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }
    }
}

class Find implements Application {
    /**
     * Executes find command
     * Throws a FindException for wrong args
     * If path specified then use that Path, else current directory
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if findFiles causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.size() < 2) {
            throw new FindException("wrong number of arguments");
        }
        String path = appArgs.size() > 2 ? appArgs.get(2) : Shell.getCurrentDirectory();
        String pattern = appArgs.get(1);
        findFiles(path, pattern, writer);
    }

    /**
     * Finds the files
     *
     * @param path    the path where to look for files
     * @param pattern the pattern to find in the filenames
     * @param writer  that Application output is written to
     * @throws IOException if Files. walk throws an IOException
     */
    private void findFiles(String path, String pattern, OutputStreamWriter writer) throws IOException {
        try {
            Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().matches(translatePattern(pattern)))
                    .forEach(file -> writeResult(file, writer));
        } catch (SecurityException e) {
            throw new FindException("not allowed access to starting file");
        }
    }

    /**
     * Takes in a Path to a file and the shell stdout.
     * Finds the relative path of the file to the current directory and writes to output,
     * followed by a line separator.
     *
     * @param file   the Path object representing the file
     * @param writer the OutputStreamWriter object representing the shell's standard output
     */
    private void writeResult(Path file, OutputStreamWriter writer) {
        Path currentDirPath = Paths.get(Shell.getCurrentDirectory());
        String relativePath = currentDirPath.relativize(file).toString();
        try {
            writer.write(relativePath);
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        } catch (IOException e) {
            throw new FindException("error while writing result");
        }
    }

    /**
     * Takes a string representing a wildcard file pattern and replaces all instances of the '*' wildcard with the regular expression .*
     * Allows the findFiles method to use the matches method to efficiently find files that match the given pattern.
     *
     * @param pattern the wildcard file pattern to translate
     * @return the translated pattern with wildcards replaced by regular expressions
     */
    private String translatePattern(String pattern) {
        return pattern.replaceAll("\\*", ".*");
    }
}

class Uniq implements Application {
    /**
     * Executes uniq command
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.size() > 2) {
            throw new UniqException("too many arguments");
        }
        // Can expand new options by adding new booleans
        boolean ignoreCase = appArgs.get(0).equals("-i");
        String fileName = null;
        // if the args are option filename
        if (appArgs.size() == 2) {
            fileName = appArgs.get(1);
        }
        // need to check for the new booleans here
        else if (!ignoreCase) {
            fileName = appArgs.get(0);
        }
        uniqLines(fileName, ignoreCase, input, writer);
    }

    /**
     * Checks if filename is null
     * If true, gets stdin
     * Else, gets the filename
     * Creates readers
     *
     * @param filename   filename or null if no filename in command
     * @param ignoreCase bool, True if command contained option "-i"
     * @param input      string for Application
     * @param writer     that Application output is written to
     * @throws IOException if reader throws an error
     */
    private void uniqLines(String filename, boolean ignoreCase, String input, OutputStreamWriter writer) throws IOException {
        if (filename == null) {
            BufferedReader inputReader = new BufferedReader(new StringReader(input));
            uniqLineChecker(ignoreCase, writer, inputReader);
        } else {
            // Input is from a file
            Path filePath = Paths.get(filename);

            try (BufferedReader fileReader = Files.newBufferedReader(filePath)) {
                uniqLineChecker(ignoreCase, writer, fileReader);
            } catch (IOException e) {
                throw new UniqException("bad filename");
            }
        }
    }


    /**
     * Compares lines to see if they are unique then writes to a reader
     *
     * @param ignoreCase if "-i" option in command
     * @param writer     stdout
     * @param reader     stdin or file contents
     * @throws IOException if writer throws an error
     */
    private void uniqLineChecker(boolean ignoreCase, OutputStreamWriter writer, BufferedReader reader) throws IOException {
        String currentLine;
        String previousLine = null;
        while ((currentLine = reader.readLine()) != null) {
            if (ignoreCase) {
                currentLine = currentLine.toLowerCase();
                previousLine = (previousLine != null) ? previousLine.toLowerCase() : null;
            }
            if (!Objects.equals(currentLine, previousLine)) {
                writer.write(currentLine);
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }
            previousLine = currentLine;
        }
    }
}


class Sort implements Application {
    /**
     * Executes sort command
     *
     * @param appArgs the arguments input after a command separated by spaces into a list
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException throws an error if writer causes an error
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        // Parse the options and file name
        if (appArgs.size() > 2) {
            throw new SortException("too many arguments");
        }
        String fileName = null;
        boolean reverseOrder = false;

        if (appArgs.size() == 2) {
            if (appArgs.get(0).equals("-r")) {
                reverseOrder = true;
            } else {
                throw new SortException("option not supported");
            }
            fileName = appArgs.get(1);
        } else if (appArgs.size() == 1) {
            fileName = appArgs.get(0);
        }

        // Perform the sort operation
        sortLines(fileName, reverseOrder, input, writer);
    }

    /**
     * @param fileName     filename or null to call readLines
     * @param reverseOrder if it needs to be reversed (-r)
     * @param input        string for Application
     * @param writer       that Application output is written to
     * @throws IOException if writer throws an error
     */
    private void sortLines(String fileName, boolean reverseOrder, String input, OutputStreamWriter writer) throws IOException {
        List<String> lines = readLines(fileName, input);

        if (reverseOrder) {
            lines.sort(Collections.reverseOrder());
        } else {
            Collections.sort(lines);
        }
        for (String line : lines) {
            writer.write(line);
            writer.write(System.getProperty("line.separator"));
        }
        writer.flush();
    }

    /**
     * Reads lines from stdin or file and returns a list of lines
     *
     * @param fileName filename or null if stdin
     * @param input    string for Application
     * @return returns a list of lines
     * @throws IOException if reader throws an error
     */
    private List<String> readLines(String fileName, String input) throws IOException {
        if (fileName == null) {
            // Read from the provided input string
            return new ArrayList<>(Arrays.asList(input.split(System.getProperty("line.separator"))));
        } else {
            // Read from the specified file
            File file = new File(Shell.getCurrentDirectory(), fileName);
            Path filePath = file.toPath();
            if (Files.notExists(filePath) || Files.isDirectory(filePath) || !Files.isReadable(filePath)) {
                throw new SortException("wrong file argument");
            }
            return Files.readAllLines(filePath, StandardCharsets.UTF_8);
        }
    }
}

class Mkdir implements Application {
    /**
     * Executes mkdir command
     * Makes a new folder with the name specified in args
     *
     * @param appArgs name(s) of new directories
     * @param input   string for Application
     * @param writer  that Application output is written to
     * @throws IOException if args is empty
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.isEmpty()) {
            throw new MkdirException("missing argument(s)");
        }

        for (String dirName : appArgs) {
            createDirectory(dirName);
        }
    }

    /**
     * Creates a new directory with the specified name.
     * throws Mkdir Exception if the directory already exists, security error or if the creation fails
     *
     * @param dirName the name of the new directory to be created
     * @throws MkdirException if there's an error
     */
    private void createDirectory(String dirName) throws MkdirException {
        File newDir = new File(Shell.getCurrentDirectory() + System.getProperty("file.separator") + dirName);
        if (newDir.exists()) {
            throw new MkdirException(dirName + " already exists");
        }

        if (!newDir.mkdir()) {
            throw new MkdirException("could not create " + dirName);
        }
    }
}

class Touch implements Application {
    /**
     * Executes touch command.
     * Creates empty files with the names specified in args.
     *
     * @param appArgs names of new files
     * @param input   string for Application (not used for touch)
     * @param writer  that Application output is written to (not used for touch)
     * @throws IOException if args is empty or if file creation fails
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.isEmpty()) {
            throw new TouchException("missing argument(s)");
        }

        for (String fileName : appArgs) {
            createFile(fileName);
        }
    }

    /**
     * Creates a new empty file with the specified name.
     * Throws TouchException if the file already exists or if the creation fails.
     *
     * @param fileName the name of the new file to be created
     * @throws TouchException if there's an error
     */
    private void createFile(String fileName) throws TouchException {
        File newFile = new File(Shell.getCurrentDirectory() + System.getProperty("file.separator") + fileName);
        try {
            if (!newFile.createNewFile()) {
                throw new TouchException(fileName + " already exists");
            }
        } catch (IOException e) {
            throw new TouchException("could not create " + fileName);
        }
    }
}