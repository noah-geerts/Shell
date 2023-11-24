package uk.ac.ucl.shell;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public abstract class Command {
	public abstract void accept (CommandVisitor visitor) throws IOException;
	private OutputStream output;
	private String input;
	protected Command(String input, OutputStream output) {
		this.input = input;
		this.output = output;
	}
	
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

class Call extends Command {

	String atomicCommand;
	
	public Call(String atomicCommand, String input, OutputStream output) {
		super(input, output);
		this.atomicCommand = atomicCommand;
	}
	
	public void accept(CommandVisitor visitor) throws IOException {
		visitor.visit(this);
	}
	
}

class Pipe extends Command {
	Command left;
	Command right;
	
	public Pipe(Command left, Call right, String input, OutputStream output) {
		super(input, output);
		this.left = left;
		this.right = right;
	}
	
	public void accept(CommandVisitor visitor) throws IOException {
		visitor.visit(this);
	}
	
}

class Seq extends Command {
	Command left;
	Command right;
	public Seq(Command left, Command right, String input, OutputStream output) {
		super(input, output);
		this.left = left;
		this.right = right;
	}
	public void accept(CommandVisitor visitor) throws IOException {
		visitor.visit(this);
	}
	
}