package uk.ac.ucl.shell;

import java.io.IOException;
import java.util.Scanner;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * This class represents a shell application that interprets and executes user inputs
 */
public class Shell {

    private static String currentDirectory = System.getProperty("user.dir");
    
    public static void setCurrentDirectory(String s) {
    	currentDirectory = s;
    }
    
    public static String getCurrentDirectory() {
    	return currentDirectory;
    }

    /**
     * The main method to run the shell application. Can be run as a single command or in interactive mode.
     * Catches exceptions thrown during command execution and prints them to standard output rather than terminating the program.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) { //pragma: no cover
        if (args.length > 0) {
            if (args.length != 2) {
                System.out.println("COMP0010 shell: wrong number of arguments");
                return;
            }
            if (!args[0].equals("-c")) {
                System.out.println("COMP0010 shell: " + args[0] + ": unexpected argument");
            }
            try {
                eval(args[1]);
            } catch (Exception e) {
                System.err.println("COMP0010 shell: " + e.getMessage());
            }
        } else {
            Scanner input = new Scanner(System.in);
            try {
                while (true) {
                    String prompt = currentDirectory + "> ";
                    System.out.print(prompt);
                    try {
                        String cmdline = input.nextLine();
                        eval(cmdline);
                    } catch (Exception e) {
                        System.err.println("COMP0010 shell: " + e.getMessage());
                    }
                }
            } finally {
                input.close();
            }
        }
    }
    
    /**
     * Evaluates a command line input by parsing it into a tree, converting it into a Command tree, and running the Command tree.
     * @param cmdline The command line input to evaluate.
     * @throws IOException If an I/O error occurs during command evaluation.
     */
    public static void eval(String cmdline) throws IOException {
    	
    	//parse the input into a tree using ANTLR4 generated classes
    	CharStream parserInput = CharStreams.fromString(cmdline); 
        ShellGrammarLexer lexer = new ShellGrammarLexer(parserInput);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);        
        ShellGrammarParser parser = new ShellGrammarParser(tokenStream);
        ParseTree tree = parser.root();
        
        //convert it into a Command tree using CommandConverter
        Command c = tree.accept(new CommandConverter());
        
        //run the Command tree
        c.accept(new Eval());
    }

}
