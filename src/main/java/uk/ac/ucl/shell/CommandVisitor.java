package uk.ac.ucl.shell;

import java.io.IOException;

public interface CommandVisitor {
	public void visit(Call call) throws IOException;
	public void visit(Pipe pipe) throws IOException;
	public void visit(Seq seq) throws IOException;
}
