package uk.ac.ucl.shell;

import java.io.IOException;

/**
 * The interface `CommandVisitor` defines the visit method for each of the sub-types of `Command`
 */
public interface CommandVisitor {
	public void visit(Call call) throws IOException;
	public void visit(Pipe pipe) throws IOException;
	public void visit(Seq seq) throws IOException;
}
