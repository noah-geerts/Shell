package uk.ac.ucl.shell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The `Eval` class is the concrete implementation of `CommandConverter`.
 * It defines the functionality of `Command` objects when they are visited.
 */
public class Eval implements CommandVisitor{
	
	/**
     * Visits a Call command, splits the atomic command into its components and runs the app.
     * @param call The Call command to be visited.
     * @throws IOException If an I/O error occurs during execution.
     */
	public void visit(Call call) throws IOException {
		//Split atomic command into its app name, arguments, input files, and output files, 
		ArrayList<String> appArgs = new ArrayList<>(), inputFileNames = new ArrayList<>(), outputFileNames = new ArrayList<>();
		String appName = patternMatcher(call.getAtomicCommand(), appArgs, inputFileNames, outputFileNames);
        
        //Convert the patternMatcher outputs into the app's arguments and run the app
		runApp(appName, appArgs, inputFileNames, outputFileNames, call.getInput(), call.getOutput());
        
	}
	
	/**
     * Visits a Pipe command, executes the left command, passes its output to the right command,
     * executes the right command, and passes its output to the Pipe command's output stream.
     * @param pipe The Pipe command to be visited.
     * @throws IOException If an I/O error occurs during execution.
     */
	public void visit(Pipe pipe) throws IOException {
		//run the left Command while storing its output in a byte stream
		ByteArrayOutputStream leftOutput = new ByteArrayOutputStream();
		pipe.getLeft().setOutput(leftOutput);
		pipe.getLeft().accept(this);
		
		//convert the byte stream to a string and set it as input of the right Command
		String leftOutString = leftOutput.toString();
		pipe.getRight().setInput(leftOutString);
		
		//set the output of the right Command to that of the pipe object, and run the right Command
		pipe.getRight().setOutput(pipe.getOutput());
		pipe.getRight().accept(this);
	}
	
	/**
     * Visits a Seq command, executes the left command, then executes the right command.
     * @param seq The Seq command to be visited.
     * @throws IOException If an I/O error occurs during execution.
     */
	public void visit(Seq seq) throws IOException {
		//run the left Command, then the right
		seq.getLeft().accept(this);
		seq.getRight().accept(this);
	}
	
	
	
	
	////////////////////
	///HELPER METHODS///
	////////////////////
	
	
	
	/**
     * Retrieves the text content from a file.
     * @param fileName The name of the file in the Shell's current directory.
     * @return The text content of the file.
     * @throws IOException If an I/O error occurs while reading the file.
     * @throws FileNotFoundException if the file is a directory or does not exist.
     */
	public static String getFileText(String fileName) throws IOException {
		File f = new File(Shell.getCurrentDirectory() + "\\" + fileName);
		
		//if the file doesn't exist or it's a directory, throw an error
		if(f.isDirectory()) {
			throw new FileNotFoundException("Input file may not be a directory");
		} else if(!f.exists()) {
			throw new FileNotFoundException("File " + fileName + " does not exist");
		}
		
		//otherwise, read the whole file and return it
		String content = Files.readString(f.toPath(), StandardCharsets.UTF_8);
		return content;
	}
	
	/**
     * Retrieves an OutputStreamWriter for writing to a file, creating the file if it does not already exist.
     * @param fileName The name of the file.
     * @return The OutputStreamWriter for the file.
     * @throws IOException If an I/O error occurs while accessing the file.
     * @throws FileNotFoundException if the file is a directory.
     */
	public static OutputStreamWriter getOutputWriter(String fileName) throws IOException {
		File f = new File(Shell.getCurrentDirectory() + "\\" + fileName);
		
		//throw an error if the file is a directory
		if(f.isDirectory()) {
			throw new FileNotFoundException("Output file may not be a directory");
		}
		
		//create the file if it doesn't exist, and return a writer for it
		f.createNewFile();
		FileWriter fileWriter = new FileWriter(f);
		return fileWriter;	
	}
	
	/**
     * Parses the atomic command and extracts application name, arguments, input and output file names.
     * @param atomicCommand The atomic command to parse.
     * @param appArgs The list to store application arguments.
     * @param inputFileNames The list to store input file names.
     * @param outputFileNames The list to store output file names.
     * @return The name of the application.
     * @throws IOException If an I/O error occurs while processing the command.
     * @throws RuntimeException if no tokens are found while processing the command.
     */
	public static String patternMatcher(String atomicCommand, ArrayList<String> appArgs, ArrayList<String> inputFileNames, ArrayList<String> outputFileNames) throws IOException {
		//define patterns to find in atomicCommand and create matcher
		String patterns = "[^\\s\"'><`]+|\"([^\"]*)\"|'([^']*)'|`([^`]*)`|(<)|(>)|(>)";
        Pattern pattern = Pattern.compile(patterns);
        Matcher matcher = pattern.matcher(atomicCommand);
        ArrayList<String> tokens = new ArrayList<String>();
        
        //nextToken = 1 if the next token is an input file, 2 if output file, 0 otherwise
        int nextToken = 0;
        while (matcher.find()) {
        	
        	//group 3 matches backquoted strings and handles command substitution
        	if(matcher.group(3) != null) {
        		//extract subcommand from backquotes
        		String backQuoted = matcher.group(0).trim();
        		String subCommand = backQuoted.substring(1, backQuoted.length() - 1);
        		//run subcommand and store its output
        		ByteArrayOutputStream subOutput = new ByteArrayOutputStream();
        		Call c = new Call(subCommand, "", subOutput);
        		c.accept(new Eval());
        		String subOutputString = subOutput.toString();
        		
        		//substitute the backquoted command with its output to continue matching
        		atomicCommand = subOutputString + " " + atomicCommand.substring(matcher.end(0));
        		matcher = pattern.matcher(atomicCommand);
        		
        		//if group 3 matched, next token isn't input or output
        		nextToken = 0;
        		continue;
        	}
        	
        	//group 4 matches '<' and group 5 matches '>'
        	if(matcher.group(4) != null) {nextToken = 1; continue;}
        	if(matcher.group(5) != null) {nextToken = 2; continue;}
        	
        	//group 1 matches double quoted text and group 2 matches single quoted text
        	if (matcher.group(1) != null || matcher.group(2) != null) {
                String quoted = matcher.group(0).trim();
                if(nextToken == 0) tokens.add(quoted.substring(1,quoted.length()-1));
                else if (nextToken == 1) inputFileNames.add(quoted.substring(1,quoted.length()-1));
                else outputFileNames.add(quoted.substring(1,quoted.length()-1));
                
            //group 0 always matches, so we handle it last, and only if none others found a match
            } else {
            	//get the text and trim it
                String nonQuote = matcher.group(0).trim();
                
                //perform globbing on the text
                ArrayList<String> globbingResult = new ArrayList<String>();
                Path dir = Paths.get(Shell.getCurrentDirectory());
                DirectoryStream<Path> stream = Files.newDirectoryStream(dir, nonQuote);
                for (Path entry : stream) {
                    globbingResult.add(entry.getFileName().toString());
                }
                if (globbingResult.isEmpty()) {
                    globbingResult.add(nonQuote);
                }
                
                //add globbing results to tokens, 
                if(nextToken == 0) tokens.addAll(globbingResult);
                else if(nextToken == 1) inputFileNames.addAll(globbingResult);
                else outputFileNames.addAll(globbingResult);
            }
        	
        	//if none of groups 4,5 matched and called continue, then next token isn't input or output
        	nextToken = 0;
        }
        
        //if there are no tokens, the atomicCommand is invalid
        if(tokens.size() == 0) {
        	throw new RuntimeException("Command: '" + atomicCommand + "' is invalid");
        }
        
        //first token is the app name, and the rest rest are app arguments
        appArgs.addAll(tokens.subList(1, tokens.size())); 
        return tokens.get(0);   
	}
	
	/**
     * Runs an application of the given name with provided arguments, input files and output files.
     * Prioritizes using input and output files as I/O, unless there are none, in which case the Call command's I/O are used instead.
     * @param appName The name of the application to run.
     * @param appArgs The arguments for the application.
     * @param inputFileNames The input file names for the application.
     * @param outputFileNames The output file names for the application.
     * @param callInput The input for the command.
     * @param callOutput The output stream for the command.
     * @throws IOException If an I/O error occurs during application execution.
     * @throws RuntimeException if more than one I/O files are specified.
     */
	public static void runApp(String appName, ArrayList<String> appArgs, ArrayList<String> inputFileNames, ArrayList<String> outputFileNames, String callInput, OutputStream callOutput) throws IOException {
		//generate correct app with unsafe decorator if need be
		Application app;
        AppFactory a = new AppFactory();
        if(appName.charAt(0) != '_') {
        	app = a.generateApp(appName);
        } else {
        	appName = appName.substring(1, appName.length());
        	app = new UnsafeDecorator(a.generateApp(appName));
        }
        
        //initialize app's input and output as those of the Call object
      	OutputStreamWriter appWriter = new OutputStreamWriter(callOutput);
      	String appInput = callInput;
       
        //set app's output to the '>' redirection, given only one '>' was present
        if(inputFileNames.size() > 1) {throw new RuntimeException("Only one input redirection permitted");}
        else if(inputFileNames.size() == 1) {appInput = getFileText(inputFileNames.get(0));}
        
        //set app's input to the '<' redirection, given only one '<' was present
        if(outputFileNames.size() > 1) {throw new RuntimeException("Only one output redirection permitted");}
        if(outputFileNames.size() == 1) {appWriter = getOutputWriter(outputFileNames.get(0));}
        
        //execute app
        app.exec(appArgs, appInput, appWriter);
	}
}
