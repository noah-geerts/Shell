package uk.ac.ucl.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Eval implements CommandVisitor {
	//when we visit Call, we parse the String: input into the app and its arguments,
	//then execute the app.
	public void visit(Call call) throws IOException {
        CharStream parserInput = CharStreams.fromString(call.getInput()); 
        ShellGrammarLexer lexer = new ShellGrammarLexer(parserInput);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);        
        ShellGrammarParser parser = new ShellGrammarParser(tokenStream);
        ParseTree tree = parser.command();
        ArrayList<String> rawCommands = new ArrayList<String>();
        String lastSubcommand = "";
        for (int i=0; i<tree.getChildCount(); i++) {
            if (!tree.getChild(i).getText().equals(";")) {
                lastSubcommand += tree.getChild(i).getText();
            } else {
                rawCommands.add(lastSubcommand);
                lastSubcommand = "";
            }
        }
        rawCommands.add(lastSubcommand);
        for (String rawCommand : rawCommands) {
            String spaceRegex = "[^\\s\"']+|\"([^\"]*)\"|'([^']*)'";
            ArrayList<String> tokens = new ArrayList<String>();
            Pattern regex = Pattern.compile(spaceRegex);
            Matcher regexMatcher = regex.matcher(rawCommand);
            String nonQuote;
            while (regexMatcher.find()) {
                if (regexMatcher.group(1) != null || regexMatcher.group(2) != null) {
                    String quoted = regexMatcher.group(0).trim();
                    tokens.add(quoted.substring(1,quoted.length()-1));
                } else {
                    nonQuote = regexMatcher.group().trim();
                    ArrayList<String> globbingResult = new ArrayList<String>();
                    Path dir = Paths.get(Shell.getCurrentDirectory());
                    DirectoryStream<Path> stream = Files.newDirectoryStream(dir, nonQuote);
                    for (Path entry : stream) {
                        globbingResult.add(entry.getFileName().toString());
                    }
                    if (globbingResult.isEmpty()) {
                        globbingResult.add(nonQuote);
                    }
                    tokens.addAll(globbingResult);
                }
            }
            String appName = tokens.get(0);
            ArrayList<String> appArgs = new ArrayList<String>(tokens.subList(1, tokens.size()));
            Application a = AppFactory.generateApp(appName);
            OutputStreamWriter writer = new OutputStreamWriter(call.getOutput());
            a.exec(appArgs, writer);
        }
	}
	//when we visit Pipe, we write the output of the first Call's app to a local variable stream, then we
	//convert that to a string and concatenate it to the String: input of the second Call
	public void visit(Pipe pipe) throws IOException {
		//run the left Command while storing its output in a byte stream
		ByteArrayOutputStream leftOutput = new ByteArrayOutputStream();
		pipe.left.setOutput(leftOutput);
		pipe.left.accept(this);
		
		//convert the byte stream to a string and concatenate it to the input of the right Command
		String leftOutString = leftOutput.toString();
		pipe.right.setInput(pipe.right.getInput() + " " + leftOutString);
		pipe.right.accept(this);
	}
	
	//when we visit Seq, we simply call accept on the left Command then the right Command
	public void visit(Seq seq) throws IOException {
		seq.left.accept(this);
		seq.right.accept(this);
	}
}
