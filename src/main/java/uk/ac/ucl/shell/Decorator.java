package uk.ac.ucl.shell;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * This abstract class represents a decorator for an Application.
 * It implements the Application interface and serves as a base class for specific decorators.
 */
public abstract class Decorator implements Application {
	
    private Application decoratedApp;

    public Decorator(Application decoratedApp) {
        this.decoratedApp = decoratedApp;
    }

    /**
     * Executes the decorated application with provided arguments, input, and output writer.
     * @param appArgs The arguments for the application.
     * @param input The input string for the application.
     * @param writer The OutputStreamWriter for the application output.
     * @throws IOException If an I/O error occurs during application execution.
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
        decoratedApp.exec(appArgs, input, writer);
    }
}

/**
 * This class represents an unsafe decorator for an Application.
 * It extends the Decorator class and catches exceptions thrown during execution,
 * writing the exception message to the output stream.
 */
class UnsafeDecorator extends Decorator {
	
    public UnsafeDecorator(Application decoratedApp) {
        super(decoratedApp);
    }
    
    /**
     * Executes the decorated application with provided arguments, input, and output writer,
     * catching any exceptions that occur and writing their messages to the output writer.
     * @param appArgs The arguments for the application.
     * @param input The input for the application.
     * @param writer The OutputStreamWriter for the application output.
     * @throws IOException If an I/O error occurs during application execution.
     */
    public void exec(ArrayList<String> appArgs, String input, OutputStreamWriter writer) throws IOException {
       
    	try {
            super.exec(appArgs, input, writer);
        } catch (Exception e) {
            writer.write(e.getMessage());
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }
    	
    }
}