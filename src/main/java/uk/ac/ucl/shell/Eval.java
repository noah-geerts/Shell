package uk.ac.ucl.shell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Eval implements CommandVisitor{

	
	public void visit(Call call) throws IOException {
		//Split atomic command into its app name, arguments, input files, and output files, 
		ArrayList<String> appArgs = new ArrayList<>(), inputFileNames = new ArrayList<>(), outputFileNames = new ArrayList<>();
		String appName = patternMatcher(call.atomicCommand, appArgs, inputFileNames, outputFileNames);
        
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
      	OutputStreamWriter appWriter = new OutputStreamWriter(call.getOutput());
      	String appInput = call.getInput();
       
        //set app's output to the '>' redirection, given only one '>' was present
        if(inputFileNames.size() > 1) {throw new IOException("Only one input redirection permitted");}
        else if(inputFileNames.size() == 1) {appInput = getFileText(inputFileNames.get(0));}
        
        //set app's input to the '<' redirection, given only one '<' was present
        if(outputFileNames.size() > 1) {throw new IOException("Only one output redirection permitted");}
        if(outputFileNames.size() == 1) {appWriter = getOutputWriter(outputFileNames.get(0));}
        
        //execute app
        app.exec(appArgs, appInput, appWriter);
        
	}
	
	
	public void visit(Pipe pipe) throws IOException {
		//run the left Command while storing its output in a byte stream
		ByteArrayOutputStream leftOutput = new ByteArrayOutputStream();
		pipe.left.setOutput(leftOutput);
		pipe.left.accept(this);
		
		//convert the byte stream to a string and set it as input of the right Command
		String leftOutString = leftOutput.toString();
		pipe.right.setInput(leftOutString);
		
		//set the output of the right Command to that of the pipe object, and run the right Command
		pipe.right.setOutput(pipe.getOutput());
		pipe.right.accept(this);
	}
	
	
	public void visit(Seq seq) throws IOException {
		//run the left Command, then the right
		seq.left.accept(this);
		seq.right.accept(this);
	}
	
	
	
	
	////////////////////
	///HELPER METHODS///
	////////////////////
	
	
	

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
                Path dir = Paths.get("C:\\Users\\noahg");
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
}
