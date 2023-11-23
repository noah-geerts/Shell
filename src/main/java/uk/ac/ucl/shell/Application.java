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

public interface Application {
	public abstract void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException;
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
            throw new RuntimeException("ls: no such directory");
        }
	}
}

class Cat implements Application{

	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
		 if (appArgs.isEmpty()) {
             throw new RuntimeException("cat: missing arguments");
         } else {
             for (String arg : appArgs) {
                 Charset encoding = StandardCharsets.UTF_8;
                 File currFile = new File(Shell.getCurrentDirectory() + File.separator + arg);
                 if (currFile.exists()) {
                     Path filePath = Paths.get(Shell.getCurrentDirectory() + File.separator + arg);
                     try (BufferedReader reader = Files.newBufferedReader(filePath, encoding)) {
                         String line = null;
                         while ((line = reader.readLine()) != null) {
                             writer.write(String.valueOf(line));
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
         }
		
	}
}

class Echo implements Application{

	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
		boolean atLeastOnePrinted = false;
        for (String arg : appArgs) {
            writer.write(arg);
            writer.write(" ");
            writer.flush();
            atLeastOnePrinted = true;
        }
        if (atLeastOnePrinted) {
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }
		
	}
	
}

class Head implements Application{

	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
		if (appArgs.isEmpty()) {
            throw new RuntimeException("head: missing arguments");
        }
        if (appArgs.size() != 1 && appArgs.size() != 3) {
            throw new RuntimeException("head: wrong arguments");
        }
        if (appArgs.size() == 3 && !appArgs.get(0).equals("-n")) {
            throw new RuntimeException("head: wrong argument " + appArgs.get(0));
        }
        int headLines = 10;
        String headArg;
        if (appArgs.size() == 3) {
            try {
                headLines = Integer.parseInt(appArgs.get(1));
            } catch (Exception e) {
                throw new RuntimeException("head: wrong argument " + appArgs.get(1));
            }
            headArg = appArgs.get(2);
        } else {
            headArg = appArgs.get(0);
        }
        File headFile = new File(Shell.getCurrentDirectory() + File.separator + headArg);
        if (headFile.exists()) {
            Charset encoding = StandardCharsets.UTF_8;
            Path filePath = Paths.get((String) Shell.getCurrentDirectory() + File.separator + headArg);
            try (BufferedReader reader = Files.newBufferedReader(filePath, encoding)) {
                for (int i = 0; i < headLines; i++) {
                    String line = null;
                    if ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.write(System.getProperty("line.separator"));
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("head: cannot open " + headArg);
            }
        } else {
            throw new RuntimeException("head: " + headArg + " does not exist");
        }
		
	}
	
}

class Tail implements Application{

	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
		if (appArgs.isEmpty()) {
            throw new RuntimeException("tail: missing arguments");
        }
        if (appArgs.size() != 1 && appArgs.size() != 3) {
            throw new RuntimeException("tail: wrong arguments");
        }
        if (appArgs.size() == 3 && !appArgs.get(0).equals("-n")) {
            throw new RuntimeException("tail: wrong argument " + appArgs.get(0));
        }
        int tailLines = 10;
        String tailArg;
        if (appArgs.size() == 3) {
            try {
                tailLines = Integer.parseInt(appArgs.get(1));
            } catch (Exception e) {
                throw new RuntimeException("tail: wrong argument " + appArgs.get(1));
            }
            tailArg = appArgs.get(2);
        } else {
            tailArg = appArgs.get(0);
        }
        File tailFile = new File(Shell.getCurrentDirectory() + File.separator + tailArg);
        if (tailFile.exists()) {
            Charset encoding = StandardCharsets.UTF_8;
            Path filePath = Paths.get((String) Shell.getCurrentDirectory() + File.separator + tailArg);
            ArrayList<String> storage = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(filePath, encoding)) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    storage.add(line);
                }
                int index = 0;
                if (tailLines > storage.size()) {
                    index = 0;
                } else {
                    index = storage.size() - tailLines;
                }
                for (int i = index; i < storage.size(); i++) {
                    writer.write(storage.get(i) + System.getProperty("line.separator"));
                    writer.flush();
                }            
            } catch (IOException e) {
                throw new RuntimeException("tail: cannot open " + tailArg);
            }
        } else {
            throw new RuntimeException("tail: " + tailArg + " does not exist");
        }
		
	}
	
}

class Grep implements Application{

	public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
		if (appArgs.size() < 2) {
            throw new RuntimeException("grep: wrong number of arguments");
        }
        Pattern grepPattern = Pattern.compile(appArgs.get(0));
        int numOfFiles = appArgs.size() - 1;
        Path filePath;
        Path[] filePathArray = new Path[numOfFiles];
        Path currentDir = Paths.get(Shell.getCurrentDirectory());
        for (int i = 0; i < numOfFiles; i++) {
            filePath = currentDir.resolve(appArgs.get(i + 1));
            if (Files.notExists(filePath) || Files.isDirectory(filePath) || 
                !Files.exists(filePath) || !Files.isReadable(filePath)) {
                throw new RuntimeException("grep: wrong file argument");
            }
            filePathArray[i] = filePath;
        }
        for (int j = 0; j < filePathArray.length; j++) {
            Charset encoding = StandardCharsets.UTF_8;
            try (BufferedReader reader = Files.newBufferedReader(filePathArray[j], encoding)) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = grepPattern.matcher(line);
                    if (matcher.find()) {
                        if (numOfFiles > 1) {
                            writer.write(appArgs.get(j+1));
                            writer.write(":");
                        }
                        writer.write(line);
                        writer.write(System.getProperty("line.separator"));
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("grep: cannot open " + appArgs.get(j + 1));
            }
        }
		
	}
	
}

class Cut implements Application {
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.size() < 2) {
            throw new RuntimeException("cut: wrong number of arguments");
        }
        String option = appArgs.get(0);
        String[] ranges = option.split(",");
        for (String range : ranges) {
            processRange(range, appArgs.subList(1, appArgs.size()), writer);
        }
    }
    private void processRange(String range, List<String> files, OutputStreamWriter writer) throws IOException {
        String[] bounds = range.split("-");
        int start = parseBound(bounds[0]);
        int end;
        if (bounds.length > 1){
            end = parseBound(bounds[1]);
        }
        else {
            end = Integer.MAX_VALUE;
        }
        for (String fileName : files) {
            processFile(fileName, start, end, writer);
        }
    }
    private int parseBound(String bound) {
        return (bound.isEmpty()) ? 1 : Integer.parseInt(bound);
    }

    private void processFile(String fileName, int start, int end, OutputStreamWriter writer) throws IOException {
        Path filePath = Paths.get(fileName);

        if (Files.notExists(filePath) || Files.isDirectory(filePath) || !Files.isReadable(filePath)) {
            throw new RuntimeException("cut: wrong file argument");
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

    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        if (appArgs.size() < 2) {
            throw new RuntimeException("find: wrong number of arguments");
        }
        String path = (appArgs.size() > 2) ? appArgs.get(2) : Shell.getCurrentDirectory();
        String pattern = appArgs.get(1);
        findFiles(path, pattern, writer);
    }

    private void findFiles(String path, String pattern, OutputStreamWriter writer) throws IOException {
        try {
            Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().matches(translatePattern(pattern)))
                    .forEach(file -> writeResult(file, writer));
        } catch (IOException e) {
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
            Collections.sort(lines, Collections.reverseOrder());
        } else {
            Collections.sort(lines);
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
            return Arrays.asList(input.split(System.getProperty("line.separator")));
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