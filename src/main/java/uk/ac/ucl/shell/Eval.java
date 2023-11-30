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
	//when we visit Call, we split the atomicCommand into the app and its arguments,
	//then execute the app.
	public void visit(Call call) throws IOException {
		String atomicCommand = call.atomicCommand;
		//by default, appOutputWriter should be a writer to call's output, and appInput should be the Call's input String
		OutputStreamWriter appWriter = new OutputStreamWriter(call.getOutput());
		String appInput = call.getInput();
		
		//define patterns to find in atomicCommand and create matcher
		String spaceRegex = "[^\\s\"'><`]+|\"([^\"]*)\"|'([^']*)'|`([^`]*)`|(<)|(>)|(>)";
        ArrayList<String> tokens = new ArrayList<String>();
        Pattern regex = Pattern.compile(spaceRegex);
        Matcher regexMatcher = regex.matcher(atomicCommand);
        String nonQuote;
        //these arraylists will store any file names preceded by < or > , respectively
        ArrayList<String> inputFileNames = new ArrayList<>();
        ArrayList<String> outputFileNames = new ArrayList<>();
        //variable used to check whether the next token is an input file, output file, or normal argument
        int nextToken = 0;
        //find the next token
        while (regexMatcher.find()) {
        	if(regexMatcher.group(3) != null) {
        		String backQuoted = regexMatcher.group(0).trim();
        		String subCommand = backQuoted.substring(1, backQuoted.length() - 1);
        		ByteArrayOutputStream subOutput = new ByteArrayOutputStream();
        		Eval e = new Eval();
        		Call c = new Call(subCommand, "", subOutput);
        		c.accept(e);
        		String subOutputString = subOutput.toString();
        		String furtherProcessing = atomicCommand.substring(regexMatcher.end(3)) + subOutputString;
        		regexMatcher = regex.matcher(furtherProcessing);
        		continue;
        	}
        	//if it matches group 3, it is a "<", meaning that the next token is an input file
        	if(regexMatcher.group(4) != null) {nextToken = 1; continue;}
        	//if it matches group 4, it is a ">", meaning that the next token is an output file
        	if(regexMatcher.group(5) != null) {nextToken = 2; continue;}
        	/*if it matches group 1 or 2, it is a piece of quoted text, meaning it may not involve globbing,
        	 * and simply needs to have its quotes removed, then be added to the args, input files, or output files
        	 */
        	if (regexMatcher.group(1) != null || regexMatcher.group(2) != null) {
                String quoted = regexMatcher.group(0).trim();
                if(nextToken == 0) tokens.add(quoted.substring(1,quoted.length()-1));
                else if (nextToken == 1) inputFileNames.add(quoted.substring(1,quoted.length()-1));
                else outputFileNames.add(quoted.substring(1,quoted.length()-1));
            /*if it matches group 0, it is a nonquoted piece of text, meaning that it needs to be
             * checked for globbing, then all globbing results are added to args, input files, or output files
             */
            } else {
                nonQuote = regexMatcher.group().trim();
                ArrayList<String> globbingResult = new ArrayList<String>();
                Path dir = Paths.get("C:\\Users\\noahg");
                DirectoryStream<Path> stream = Files.newDirectoryStream(dir, nonQuote);
                for (Path entry : stream) {
                    globbingResult.add(entry.getFileName().toString());
                }
                if (globbingResult.isEmpty()) {
                    globbingResult.add(nonQuote);
                }
                if(nextToken == 0) tokens.addAll(globbingResult);
                else if(nextToken == 1) inputFileNames.addAll(globbingResult);
                else outputFileNames.addAll(globbingResult);
            }
        	//if the token was of groups 0-2 inclusive, then the next token is a normal argument
        	nextToken = 0;
        }
        //we can only have one input file or output file
        if(outputFileNames.size() > 1 || inputFileNames.size() > 1) {
        	throw new IOException("too many < or >");
        }
        
        //separate appArgs from appName, generate the correct app, making it unsafe if it starts with _
        //also set the Call's appArgs and app variables accordingly
        String appName = tokens.get(0);
        Application app;
        if(appName.charAt(0) != '_') {
        	app = AppFactory.generateApp(appName);
        } else {
        	appName = appName.substring(1, appName.length());
        	app = new UnsafeDecorator(AppFactory.generateApp(appName));
        	
        }
        ArrayList<String> appArgs = new ArrayList<String>(tokens.subList(1, tokens.size())); 
       
        //get input string for app (if there is a single < operator),
        //and return it so the Call's input string can be set to it
        if(inputFileNames.size() == 1) {
        	appInput = getFileText(inputFileNames.get(0));
        }
        
        //get the output writer for the app (if there is a single > operator),
        //and set the Call's output writer to it
        if(outputFileNames.size() == 1) {
        	appWriter = getOutputWriter(outputFileNames.get(0));
        }
        
        app.exec(appArgs, appInput, appWriter);
        
	}
	
	//when we visit Pipe, we write the output of the first Call's app to a local variable stream, then we
	//convert that to a string and set it as the input of the right Command
	public void visit(Pipe pipe) throws IOException {
		//run the left Command while storing its output in a byte stream
		ByteArrayOutputStream leftOutput = new ByteArrayOutputStream();
		pipe.left.setOutput(leftOutput);
		pipe.left.accept(this);
		
		//convert the byte stream to a string and set it as input of the right Command
		String leftOutString = leftOutput.toString();
		pipe.right.setInput(leftOutString);
		pipe.right.setOutput(pipe.getOutput());
		pipe.right.accept(this);
	}
	
	//when we visit Seq, we simply call accept on the left Command then the right Command
	public void visit(Seq seq) throws IOException {
		seq.left.accept(this);
		seq.right.accept(this);
	}
	
	public static String getFileText(String fileName) throws IOException {
		File f = new File(Shell.getCurrentDirectory() + "\\" + fileName);
		//if the file doesn't exist or it's a directory, throw an error
		if(!f.exists() || f.isDirectory()) {
			throw new FileNotFoundException();
		}
		//otherwise, read the whole file and return it
		String content = Files.readString(f.toPath(), StandardCharsets.UTF_8);
		return content;
	}
	
	public static OutputStreamWriter getOutputWriter(String fileName) throws IOException {
		File f = new File(Shell.getCurrentDirectory() + "\\" + fileName);
		//throw an error if the file is a directory
		if(f.isDirectory()) {
			throw new FileNotFoundException();
		}
		//create the file if it doesn't exist, create a writer for it
		f.createNewFile();
		FileWriter fileWriter = new FileWriter(f);
		return fileWriter;	
	}
}
