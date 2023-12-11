package uk.ac.ucl.shell;

import java.io.IOException;

/**
 * The interface `CommandVisitor` defines the visit method for each of the sub-types of `Command`
 */
public interface CommandVisitor {
	void visit(Call call) throws IOException;
	void visit(Pipe pipe) throws IOException;
	void visit(Seq seq) throws IOException;
}
