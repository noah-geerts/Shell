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
import java.util.stream.Stream;

public interface Application {
    void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException;
}

class Cd implements Application{

	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
		if (appArgs.isEmpty()) {
            throw new RuntimeException("cd: missing argument");
        } else if (appArgs.size() > 1) {
            throw new RuntimeException("cd: too many arguments");
        }
        String dirString = appArgs.get(0);
        File dir = new File(Shell.getCurrentDirectory(), dirString);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException("cd: " + dirString + " is not an existing directory");
        }
        Shell.setCurrentDirectory(dir.getCanonicalPath());
	}

}

class Pwd implements Application{
	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
		writer.write(Shell.getCurrentDirectory());
        writer.write(System.getProperty("line.separator"));
        writer.flush();
	}
}


class Ls implements Application{

	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
		File currDir;
        if (appArgs.isEmpty()) {
            currDir = new File(Shell.getCurrentDirectory());
        } else if (appArgs.size() == 1) {
            currDir = new File(appArgs.get(0));
        } else {
            throw new RuntimeException("ls: too many arguments");
        }
        try {
            File[] listOfFiles = currDir.listFiles();
            boolean atLeastOnePrinted = false;
            assert listOfFiles != null;
            for (File file : listOfFiles) {
                if (!file.getName().startsWith(".")) {
                    writer.write(file.getName());
                    writer.write(System.getProperty("line.separator"));
                    writer.flush();
                    atLeastOnePrinted = true;
                }
            }
            if (atLeastOnePrinted) {
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("ls: no such directory");
        }
	}
}

class Cat implements Application{
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.isEmpty() && input.isEmpty()) {
            throw new RuntimeException("cat: missing arguments / empty stdin");
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
                        throw new RuntimeException("cat: cannot open " + arg);
                    }
                } else {
                    throw new RuntimeException("cat: file does not exist");
                }
            }
            if (!input.isEmpty()) {
                writer.write(input);
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }
        }
    }
}

class Echo implements Application{

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

class Head implements Application{
    // in the original shell
    // head can have the form: head -n num file; head file
    // in real life:
    // head -q file..; head -v file...; head -n num file; head -c num file; head file
    private int lineNumber = 10;
	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.isEmpty() && input.isEmpty()) {
            throw new RuntimeException("head: missing arguments");
        }
        if (appArgs.isEmpty()) {
            readFromStdin(input, writer);
        }
        else if (appArgs.size() == 1) {
            String fileName = appArgs.get(0);
            readFromFile(fileName, writer);
        }
        else if(appArgs.size() == 2)
        {
            String option = appArgs.get(0);
            if (!option.equals("-n")) {
                throw new RuntimeException("head: invalid option");
            }
            else {
                try {
                    this.lineNumber = Integer.parseInt(appArgs.get(1));
                    readFromStdin(input, writer);
                }
                catch (NumberFormatException e) {
                    throw new RuntimeException("head: second arg is not an integer");
                }

            }
        }
        else if (appArgs.size() == 3){
            String option = appArgs.get(0);
            if (!option.equals("-n")) {
                throw new RuntimeException("head: invalid option");
            }
            else {
                try {
                    this.lineNumber = Integer.parseInt(appArgs.get(1));
                        String fileName = appArgs.get(2);
                        readFromFile(fileName, writer);
                }
                catch (NumberFormatException e) {
                    throw new RuntimeException("head: second arg is not an integer");
                }
            }
        }
        else {
            throw new RuntimeException("head: invalid number of arguments");
        }
    }
    // Can pass in an array instead of lineNumber to allow more options.
    private void readFromStdin(String input,OutputStreamWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(input))) {
            writeLines(reader, writer);
        }
    }

    private void readFromFile(String fileName, OutputStreamWriter writer) throws IOException {
        Path filePath = Paths.get(Shell.getCurrentDirectory() + File.separator + fileName);
        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                writeLines(reader, writer);
            }
        } else {
            throw new RuntimeException("head: file not found: " + fileName);
        }
    }
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

class Tail implements Application{
    private int lineNumber = 10;
	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        // default number of lines is 10
        if (appArgs.isEmpty() && input.isEmpty()) {
            throw new RuntimeException("tail: missing arguments");
        }
        if (appArgs.isEmpty()) {
            readFromStdin(input, writer);
        }
        else if (appArgs.size() == 1) {
            String fileName = appArgs.get(0);
            readFromFile(fileName, writer);
        }
        else if(appArgs.size() == 2)
        {
            String option = appArgs.get(0);
            if (!option.equals("-n")) {
                throw new RuntimeException("tail: invalid option");
            }
            else {
                try {
                    this.lineNumber = Integer.parseInt(appArgs.get(1));
                    readFromStdin(input, writer);
                }
                catch (NumberFormatException e) {
                    throw new RuntimeException("tail: second arg is not an integer");
                }

            }
        }
        else if (appArgs.size() == 3){
            String option = appArgs.get(0);
            if (!option.equals("-n")) {
                throw new RuntimeException("tail: invalid option");
            }
            else {
                try {
                    this.lineNumber = Integer.parseInt(appArgs.get(1));
                        String fileName = appArgs.get(2);
                        readFromFile(fileName, writer);
                }
                catch (NumberFormatException e) {
                    throw new RuntimeException("tail: second arg is not an integer");
                }
            }
        }
        else {
            throw new RuntimeException("tail: invalid number of arguments");
        }
    }
    private void readFromStdin(String input,OutputStreamWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(input))) {
            writeLines(reader, writer);
        }
    }

    private void readFromFile(String fileName, OutputStreamWriter writer) throws IOException {
        Path filePath = Paths.get(Shell.getCurrentDirectory() + File.separator + fileName);
        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                writeLines(reader, writer);
            }
        } else {
            throw new RuntimeException("tail: file not found: " + fileName);
        }
    }
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

class Grep implements Application{
    private Pattern grepPattern;
    private String filename = "";
    private boolean printFilename = false;
    private static final int MAX_FILES_LIMIT = 99;
	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
		if (appArgs.size() == 0) {
            throw new RuntimeException("grep: wrong number of arguments");
        }
        this.grepPattern = Pattern.compile(appArgs.get(0));
        int numOfFiles = appArgs.size() - 1;
        if (numOfFiles == 0) {
            if (input.isEmpty()) {
                throw new RuntimeException("grep: empty stdin");
            }
            else {
                readFromStdin(input, writer);
            }
        }
        else if (numOfFiles == 1) {
            this.filename = appArgs.get(1);
            readFromFile(writer);
        }
        // limit for performance/ display
        else if (numOfFiles > 1 && numOfFiles < MAX_FILES_LIMIT) {
            int i = 1;
            this.printFilename = true;
            while (i <= numOfFiles) {
                this.filename = appArgs.get(i);
                readFromFile(writer);
                i+=1;
            }
        }
	}
    private void readFromStdin(String input,OutputStreamWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(input))) {
            writeLines(reader, writer);
        }
    }
    private void readFromFile(OutputStreamWriter writer) throws IOException {
        Path filePath = Paths.get(Shell.getCurrentDirectory() + File.separator + this.filename);
        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                writeLines(reader, writer);
            }
        } else {
            throw new RuntimeException("grep: file not found: " + this.filename);
        }
    }
    private void writeLines(BufferedReader reader, OutputStreamWriter writer) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = this.grepPattern.matcher(line);
            if (matcher.find()) {
                if (this.printFilename) {
                    writer.write(this.filename);
                    writer.write(":");
                }
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }
        }
    }
}

class Cut implements Application {
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.size() < 2) {
            throw new RuntimeException("cut: wrong number of arguments");
        }

        // Assuming "-b" is at index 0
        String option = appArgs.get(1);
        String fileName = (appArgs.size() > 2) ? appArgs.get(2) : null;
        String[] ranges = option.split(",");

        for (String range : ranges) {
            processRange(range, fileName, input, writer);
        }
    }

    private void processRange(String range, String fileName, String input, OutputStreamWriter writer) throws IOException {
        String[] bounds = range.split("-");
        int start = parseBound(bounds[0]);
        int end = (bounds.length > 1) ? parseBound(bounds[1]) : Integer.MAX_VALUE;

        if (fileName == null) {
            // No file specified, read from stdin
            processLine(input, start, end, writer);
        } else {
            processFile(fileName, start, end, writer);
        }
    }

    private int parseBound(String bound) {
        return (bound.isEmpty()) ? 1 : Integer.parseInt(bound);
    }

    private void processFile(String fileName, int start, int end, OutputStreamWriter writer) {
        Path filePath = Paths.get(fileName);

        if (!Files.isReadable(filePath) || Files.isDirectory(filePath)) {
            throw new RuntimeException("cut: cannot read " + fileName);
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, start, end, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("cut: cannot open " + fileName);
        }
    }

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
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) {
        if (appArgs.size() < 2) {
            throw new RuntimeException("find: wrong number of arguments");
        }
        String path = (appArgs.size() > 2) ? appArgs.get(2) : Shell.getCurrentDirectory();
        String pattern = appArgs.get(1);
        findFiles(path, pattern, writer);
    }
    private void findFiles(String path, String pattern, OutputStreamWriter writer) {
        try (Stream<Path> filestream = Files.walk(Paths.get(path))){
            filestream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().matches(translatePattern(pattern)))
                    .forEach(file -> writeResult(file, writer));
        }
        catch (IOException e) {
            throw new RuntimeException("find: error while searching for files");
        }
    }
    private void writeResult(Path file, OutputStreamWriter writer) {
        Path currentDirPath = Paths.get(Shell.getCurrentDirectory());
        String relativePath = currentDirPath.relativize(file).toString();
        try {
            writer.write(relativePath);
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("find: error while writing result");
        }
    }
    private String translatePattern(String pattern) {
        return pattern.replaceAll("\\*", ".*");
    }
}

class Uniq implements Application {
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.size() > 2) {
            throw new RuntimeException("uniq: too many arguments");
        }
        // Can expand new options by adding new booleans
        boolean ignoreCase = appArgs.get(0).equals("-i");
        String fileName = null;
        // if the args are option filename
        if (appArgs.size() == 2){
            fileName = appArgs.get(1);
        }
        // need to check for the new booleans here
        else if (appArgs.size() == 1 && !ignoreCase) {
            fileName = appArgs.get(0);
        }
        uniqLines(fileName, ignoreCase, input, writer);
    }

    // exists to check if filename is null then create readers
    private void uniqLines(String filename, boolean ignoreCase, String input, OutputStreamWriter writer) throws IOException {
        if (filename == null) {
            BufferedReader inputReader = new BufferedReader(new StringReader(input));
            uniqLineChecker(ignoreCase, writer, inputReader);
        }
        else
        {
            // Input is from a file
            Path filePath = Paths.get(filename);

            try (BufferedReader fileReader = Files.newBufferedReader(filePath)) {
                uniqLineChecker(ignoreCase, writer, fileReader);
            }
            catch (IOException e)
            {
                throw new RuntimeException("uniq: bad filename");
            }
        }
    }

    // compares lines to see if they are uniq then writes to a reader
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
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        // Parse the options and file name
        boolean reverseOrder = appArgs.contains("-r");
        String fileName = (appArgs.size() > 0) ? appArgs.get(0) : null;

        // Perform the sort operation
        sortLines(fileName, reverseOrder, input, writer);
    }

    private void sortLines(String fileName, boolean reverseOrder, String input, OutputStreamWriter writer) throws IOException {
        List<String> lines = readLines(fileName, input);

        // Perform the sort
        if (reverseOrder) {
            lines.sort(Comparator.reverseOrder());
        } else {
            lines.sort(Comparator.naturalOrder());
        }

        // Write the sorted lines to the output
        for (String line : lines) {
            writer.write(line);
            writer.write(System.getProperty("line.separator"));
        }

        writer.flush();
    }

    private List<String> readLines(String fileName, String input) throws IOException {
        if (fileName == null) {
            // Read from the provided input string
            return new ArrayList<>(Arrays.asList(input.split(System.getProperty("line.separator"))));
        } else {
            // Read from the specified file
            Path filePath = Paths.get(fileName);
            if (Files.notExists(filePath) || Files.isDirectory(filePath) || !Files.isReadable(filePath)) {
                throw new RuntimeException("sort: wrong file argument");
            }
            return Files.readAllLines(filePath, StandardCharsets.UTF_8);
        }
    }
}