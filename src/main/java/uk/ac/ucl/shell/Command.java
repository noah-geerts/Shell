package uk.ac.ucl.shell;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The abstract class `Command` represents a shell command and defines its basic properties and behavior.
 * Subclasses of `Command` implement specific types of commands like Call, Pipe, or Seq.
 */
public abstract class Command {
    
    private OutputStream output;
    private String input;

    /**
     * Constructs a `Command` object with specified input string and output stream.
     * @param input The input string for the command.
     * @param output The output stream where command results will be directed.
     */
    protected Command(String input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Accepts a visitor that performs operations specific to the implementing command type.
     * @param visitor The CommandVisitor used to visit this command.
     * @throws IOException If an I/O error occurs while visiting the command.
     */
    public abstract void accept(CommandVisitor visitor) throws IOException;

    public OutputStream getOutput() {
        return this.output;
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public String getInput() {
        return this.input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}

/**
 * The `Call` class represents a command that calls an atomic command.
 */
class Call extends Command {

    private String atomicCommand;

    /**
     * Constructs a `Call` object that calls an atomic command.
     * @param atomicCommand The raw string for the atomic command to be called.
     * @param input The input string for the command.
     * @param output The output stream where command results will be directed.
     */
    public Call(String atomicCommand, String input, OutputStream output) {
        super(input, output);
        this.atomicCommand = atomicCommand;
    }

    /**
     * Accepts a visitor specific to `Call` commands.
     * @param visitor The CommandVisitor used to visit this command.
     * @throws IOException If an I/O error occurs while visiting the command.
     */
    public void accept(CommandVisitor visitor) throws IOException {
        visitor.visit(this);
    }

	public String getAtomicCommand() {
		return atomicCommand;
	}
}

/**
 * The `Pipe` class represents a command that pipes the output of one command to the input of another.
 */
class Pipe extends Command {

    private Command left;
    private Command right;

    /**
     * Constructs a `Pipe` object that pipes output from one command to another.
     * @param left The left-hand side command in the pipe.
     * @param right The right-hand side command in the pipe.
     * @param input The input string for the pipe command.
     * @param output The output stream where pipe command results will be directed.
     */
    public Pipe(Command left, Command right, String input, OutputStream output) {
        super(input, output);
        this.left = left;
        this.right = right;
    }

    /**
     * Accepts a visitor specific to `Pipe` commands.
     * @param visitor The CommandVisitor used to visit this command.
     * @throws IOException If an I/O error occurs while visiting the command.
     */
    public void accept(CommandVisitor visitor) throws IOException {
        visitor.visit(this);
    }

	public Command getLeft() {
		return left;
	}

	public Command getRight() {
		return right;
	}
}

/**
 * The `Seq` class represents a command that executes two commands sequentially.
 */
class Seq extends Command {

    private Command left;
    private Command right;

    /**
     * Constructs a `Seq` object that executes two commands sequentially.
     * @param left The left-hand side command in the sequence.
     * @param right The right-hand side command in the sequence.
     * @param input The input string for the seq command.
     * @param output The output stream where seq command results will be directed.
     */
    public Seq(Command left, Command right, String input, OutputStream output) {
        super(input, output);
        this.left = left;
        this.right = right;
    }

    /**
     * Accepts a visitor specific to `Seq` commands.
     * @param visitor The CommandVisitor used to visit this command.
     * @throws IOException If an I/O error occurs while visiting the command.
     */
    public void accept(CommandVisitor visitor) throws IOException {
        visitor.visit(this);
    }

	public Command getLeft() {
		return left;
	}

	public Command getRight() {
		return right;
	}
}
